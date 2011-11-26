/*
 * Copyright (c) 2011, Walter Lowe
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the author nor the names of contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.lowetek.caltrainalerts.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lowetek.caltrainalerts.data.C2DMSettings;
import net.lowetek.caltrainalerts.data.DataStorage;
import net.lowetek.caltrainalerts.data.PMF;
import net.lowetek.caltrainalerts.data.TrainUpdate;
import net.lowetek.caltrainalerts.data.UpdateClient;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.google.android.c2dm.server.C2DMRetryServlet;
import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * An admin servlet to fetch new updates from Twitter
 * @author nopayne
 *
 */
@SuppressWarnings("serial")
public class FetchUpdatesServlet extends HttpServlet 
{
	private static final Logger log = Logger.getLogger(FetchUpdatesServlet.class.getName());
	private static final Pattern timeStampPattern = Pattern.compile("T\\d\\d:\\d\\d\\z");
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType("text/plain");
		String feedName = System.getProperty("net.lowetek.feed", "caltrain");
				
		// Attempt to initialize an authenticated Twitter client.
		// If this fails, fall back to an anonymous Twitter client.
		// It's ideal to authenticate because the rate limits increase when doing so.
		Twitter twitter = null;
		
		try
		{
			AccessToken accessToken = DataStorage.getAccessToken();
			twitter = new TwitterFactory().getInstance(accessToken);
		}
		catch (Exception e)
		{
			log.warning("Unable to authorize with Twitter");
			twitter = new TwitterFactory().getInstance();
		}
		
		// If possible, we'd only like to do an incremental update.
		// Load the id of the last tweet received.
		long sinceId = DataStorage.getLatestUpdateId();		

		Paging paging = null;
		
		// Pull 200 tweets at a time.
		if (sinceId >= 0)
		{
			paging = new Paging(1, 200, sinceId);
		}
		else
		{
			paging = new Paging(1, 200);
		}
		
		try
		{
			ResponseList<Status> statuses = twitter.getUserTimeline(feedName, paging);
			List<TrainUpdate> newUpdates = new ArrayList<TrainUpdate>();            
        	resp.getWriter().println("\n\nNew updates: ");
			
        	// Convert the retrieved statuses into TrainUpdate objects.
			for (Status status : statuses)
			{
				long twitterId = status.getId();
				String text = timeStampPattern.matcher(status.getText()).replaceFirst("");
				Date date = status.getCreatedAt();
				
				TrainUpdate update = new TrainUpdate(twitterId, text, date);
        		newUpdates.add(update);
        		log.info("Added update: " + update.toString());
        		resp.getWriter().println(update.toString());
			}
			
			// Persist the new updates.
			DataStorage.addUpdates(newUpdates);
    		
			// Send notifications to all clients regarding new updates.
    		if (!newUpdates.isEmpty())
    		{
    			TrainUpdate latestUpdate = newUpdates.get(newUpdates.size() - 1);
    			Date latestUpdateDate = latestUpdate.getDate();
    			long lastestUpdateId = latestUpdate.getTwitterId();
    			
    			notifyClients(lastestUpdateId, latestUpdateDate);
    		}
			
		}
		catch (TwitterException e1)
		{
			log.log(Level.SEVERE, "Error getting tweets", e1);
		}
			
	}

	/**
	 * Notifies all clients that new updates are available.
	 * @param latestUpdateId
	 * @param latestUpdateDate
	 */
	private void notifyClients(long latestUpdateId, Date latestUpdateDate/*, PersistenceManager pm*/) 
	{
		Random random = new Random();
		long timeMillis = latestUpdateDate.getTime();
		String collapseStr = Long.toString(timeMillis);
		com.google.appengine.api.taskqueue.Queue dmQueue = QueueFactory.getQueue("c2dm");
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Extent<UpdateClient> extent = null;						
		
		try
		{
			extent = pm.getExtent(UpdateClient.class);
				
			// Iterate through each client and queue up a C2DM request for each.
			// It'd be nice if we could just fire off a batch requese for all but
			// C2DM doesn't support this feature. 
			for (UpdateClient client : extent)
			{
				TaskOptions url = 
					TaskOptions.Builder.withUrl(C2DMRetryServlet.URI)
					.param(C2DMessaging.PARAM_REGISTRATION_ID, client.getRegistrationId())
					.param(C2DMessaging.PARAM_COLLAPSE_KEY, collapseStr)
					.param("data.msgType", "notify")					
					.param("data.twitterId", Long.toString(latestUpdateId));	      
	            
	            // Task queue implements the exponential backoff		
				long jitter = random.nextInt(C2DMSettings.MAX_JITTER_MSEC);
	            url.countdownMillis(jitter);
	            
	            dmQueue.add(url);
			}
		}
		finally
		{
			if (extent != null)
			{
				extent.closeAll();
			}
			
			pm.close();
		}
	}
}
