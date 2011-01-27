package com.lowetech.caltrainupdates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.c2dm.server.C2DMRetryServlet;
import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class FetchUpdatesServlet extends HttpServlet 
{
	private static final Logger log = Logger.getLogger(FetchUpdatesServlet.class.getName());
	private static final int collapseFactor = 1000 * 60 * 5;
	public static final int C2DM_MAX_JITTER_MSEC = 3000;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world");
		StringBuffer content = new StringBuffer();
		String feedName = System.getProperty("com.lowetech.feed", "caltrain");
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		

		Query query = pm.newQuery(TrainUpdate.class);
		query.setOrdering("date DESC");
		query.setRange(0,1);

		List<TrainUpdate> oldUpdates = (List<TrainUpdate>)query.execute();
		
		long sinceId = -1;
		if (oldUpdates.size() > 0)
		{
			sinceId = oldUpdates.get(0).getTwitterId();
		}
		
		
		try {
			String urlStr = "http://api.twitter.com/1/statuses/user_timeline.json?screen_name=" + feedName + "&trim_user=1";
			
			if (sinceId >= 0)
			{
				urlStr += "&since_id=" + sinceId;
			}
            
			URL url = new URL(urlStr);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            

            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            

        } catch (MalformedURLException e) {
            // ...
        } catch (IOException e) {
            // ...
        }
        
        
        
        try
		{
        	JSONArray newEntries = new JSONArray(content.toString());
        	int count = newEntries.length();
        	SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        	
        	List<TrainUpdate> newUpdates = new ArrayList<TrainUpdate>();
        	resp.getWriter().println("\n\nNew updates: ");
        	for (int i = 0; i < count; i++)
        	{
        		JSONObject currEntry = newEntries.getJSONObject(i);
        		long twitterId = currEntry.getLong("id_str");
        		String text = currEntry.getString("text");
        		String dateStr = currEntry.getString("created_at");
        		Date date = dateFormat.parse(dateStr);
        		TrainUpdate update = new TrainUpdate(twitterId, text, date);
        		newUpdates.add(update);
        		log.info("Added update: " + update.toString());
        		
        	}
        	
        	pm.makePersistentAll(newUpdates);        	
    		
    		if (!newUpdates.isEmpty())
    		{
    			TrainUpdate latestUpdate = newUpdates.get(newUpdates.size() - 1);
    			Date latestUpdateDate = latestUpdate.getDate();
    			long lastestUpdateId = latestUpdate.getTwitterId();
    			
    			notifyClients(lastestUpdateId, latestUpdateDate, pm);
    		}
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			pm.close();
		}
			
	}

	private void notifyClients(long latestUpdateId, Date latestUpdateDate, PersistenceManager pm) 
	{
		long timeMillis = latestUpdateDate.getTime();
		//String timeStr = timeMillis + "";
		String collapseStr = Long.toString(timeMillis / collapseFactor);
		
		Extent<UpdateClient> extent = pm.getExtent(UpdateClient.class);
		
		
		com.google.appengine.api.taskqueue.Queue dmQueue = QueueFactory.getQueue("c2dm");
		
		try
		{
				
			for (UpdateClient client : extent)
			{
				TaskOptions url = 
					TaskOptions.Builder.withUrl(C2DMRetryServlet.URI)
					.param(C2DMessaging.PARAM_REGISTRATION_ID, client.getRegistrationId())
					.param(C2DMessaging.PARAM_COLLAPSE_KEY, collapseStr)
					.param("data.twitterId", Long.toString(latestUpdateId));	      
	            
	            // Task queue implements the exponential backoff
	            long jitter = (int) Math.random() * C2DM_MAX_JITTER_MSEC;
	            url.countdownMillis(jitter);
	            
	            dmQueue.add(url);
			
			}
		}
		finally
		{
			extent.closeAll();
		}
		
		
	}
}
