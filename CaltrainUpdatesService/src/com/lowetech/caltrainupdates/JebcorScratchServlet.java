package com.lowetech.caltrainupdates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class JebcorScratchServlet extends HttpServlet
{
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world");
		StringBuffer content = new StringBuffer();
		String feedName = System.getProperty("com.lowetech.feed", "caltrain");
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
//		long twitterId = 1;
//		String text = "text";
//		Date date = new Date();
//		TrainUpdate tUpdate = new TrainUpdate(twitterId, text, date);
//		pm.makePersistent(tUpdate);
//		pm.close();
//		
		pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(TrainUpdate.class);
		query.setOrdering("date DESC");
		query.setRange(0,1);
		//query.setRange(arg0, arg1)
		List<TrainUpdate> oldUpdates = (List<TrainUpdate>)query.execute();
		
//		resp.getWriter().println("Old updates:");
//		for (TrainUpdate update : oldUpdates)
//		{
//			resp.getWriter().println(update.toString());
//		}
		
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
        		resp.getWriter().println(update.toString());
        		
        	}
        	
        	pm.makePersistentAll(newUpdates);
        	
        	query = pm.newQuery(TrainUpdate.class);
        	//query.setOrdering("date DESC");
//        	DateTime dt;
        	Calendar cal = Calendar.getInstance();
        	cal.add(Calendar.DAY_OF_YEAR, -1);
        	Date cutOff = cal.getTime();
        	query.setFilter("date <= :date");
    		//DateTime date = new DateTime();
    		//Date cutOff = //new Date(date.minusHours(1));
        	//query.setRange(101, 5000);
    		query.deletePersistentAll(cutOff);
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
}
