package com.lowetek.caltrainupdates.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.lowetek.caltrainupdates.data.C2DMSettings;
import com.lowetek.caltrainupdates.data.DataStorage;
import com.lowetek.caltrainupdates.data.PMF;
import com.lowetek.caltrainupdates.data.TrainUpdate;
import com.lowetek.caltrainupdates.data.UpdateClient;

@SuppressWarnings("serial")
public class FetchUpdatesServlet extends HttpServlet 
{
	private static final Logger log = Logger.getLogger(FetchUpdatesServlet.class.getName());
	private static final Pattern timeStampPattern = Pattern.compile("T\\d\\d:\\d\\d\\z");

	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType("text/plain");
		String feedName = System.getProperty("com.lowetech.feed", "caltrain");
				

		
		
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
		
		long sinceId = DataStorage.getLatestUpdateId();

		Paging paging = null;
		
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
			
			DataStorage.addUpdates(newUpdates);
    		
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
			log.log(Level.WARNING, "Error getting tweets", e1);
		}
		

//		
//		
//		
//		try {
//			String urlStr = "http://api.twitter.com/1/statuses/user_timeline.json?screen_name=" + feedName + "&trim_user=1";
//			
//			if (sinceId >= 0)
//			{
//				urlStr += "&since_id=" + sinceId;
//			}
//            
//			URL url = new URL(urlStr);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//            String line;
//            
//
//            while ((line = reader.readLine()) != null) {
//                content.append(line);
//            }
//            reader.close();
//            
//           // resp.getWriter().print(content.toString());
//            
//
//        } catch (MalformedURLException e) {
//            // ...
//        } catch (IOException e) {
//            // ...
//        }
//        
        
        
//        try
//		{
//        	//resp.getWriter().println("\n\n===============\n\n");
//        	JSONArray newEntries = new JSONArray(content.toString());
//        	//resp.getWriter().println(newEntries.toString());
//        	int count = newEntries.length();
//        	SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
//        	
//        	List<TrainUpdate> newUpdates = new ArrayList<TrainUpdate>();
//            Pattern timeStampPattern = Pattern.compile("T\\d\\d:\\d\\d\\z");
//        	resp.getWriter().println("\n\nNew updates: ");
//        	for (int i = 0; i < count; i++)
//        	{
//        		JSONObject currEntry = newEntries.getJSONObject(i);
//        		long twitterId = currEntry.getLong("id");
//        		String text = currEntry.getString("text");
//        		text = timeStampPattern.matcher(text).replaceFirst("");
//        		String dateStr = currEntry.getString("created_at");
//        		Date date = dateFormat.parse(dateStr);
//        		TrainUpdate update = new TrainUpdate(twitterId, text, date);
//        		newUpdates.add(update);
//        		log.info("Added update: " + update.toString());
//        		resp.getWriter().println(update.toString());
//        		
//        	}
//        	
////        	pm.makePersistentAll(newUpdates);
//        	DataStorage.addUpdates(newUpdates);
//    		
//    		if (!newUpdates.isEmpty())
//    		{
//    			TrainUpdate latestUpdate = newUpdates.get(newUpdates.size() - 1);
//    			Date latestUpdateDate = latestUpdate.getDate();
//    			long lastestUpdateId = latestUpdate.getTwitterId();
//    			
//    			notifyClients(lastestUpdateId, latestUpdateDate/*, pm*/);
//    		}
//		}
//		catch (JSONException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		catch (ParseException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		finally
//		{
//			//pm.close();
//		}
			
	}

	private void notifyClients(long latestUpdateId, Date latestUpdateDate/*, PersistenceManager pm*/) 
	{
		long timeMillis = latestUpdateDate.getTime();
		//String timeStr = timeMillis + "";
		String collapseStr = Long.toString(timeMillis /*/ C2DMSettings.COLLAPSE_FACTOR*/);
		com.google.appengine.api.taskqueue.Queue dmQueue = QueueFactory.getQueue("c2dm");
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		Extent<UpdateClient> extent = null;						
		
		try
		{
			extent = pm.getExtent(UpdateClient.class);
				
			for (UpdateClient client : extent)
			{
				TaskOptions url = 
					TaskOptions.Builder.withUrl(C2DMRetryServlet.URI)
					.param(C2DMessaging.PARAM_REGISTRATION_ID, client.getRegistrationId())
					.param(C2DMessaging.PARAM_COLLAPSE_KEY, collapseStr)
					.param("data.msgType", "notify")					
					.param("data.twitterId", Long.toString(latestUpdateId));	      
	            
	            // Task queue implements the exponential backoff
	            long jitter = (int) Math.random() * C2DMSettings.MAX_JITTER_MSEC;
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
