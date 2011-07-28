/**
 * 
 */
package com.lowetech.caltrainupdates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import org.mortbay.log.Log;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONObject;

/**
 * @author nopayne
 *
 */
public class TrainUpdatesStorage
{
	private static Cache cache;
	private static String CACHED_UPDATES_KEY = "CachedUpdates";
	
	static
	{
		try 
		{
            cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        } 
		catch (CacheException e) 
		{
            // ...
        }

	}
	
	public static long getLatestUpdateId()
	{
		

		// Attempt to just pull the value out of the cache.
		// We assume that the latest updates will always live here.
		List<TrainUpdate> cachedUpdates = (List<TrainUpdate>)cache.get(CACHED_UPDATES_KEY);
		

		if (cachedUpdates != null && !cachedUpdates.isEmpty())
		{
			Log.info("Fetched cache with size of : " + cachedUpdates.size());
			return cachedUpdates.get(cachedUpdates.size() - 1).getTwitterId();
		}
		
		long sinceId = -1;		
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		
		try
		{
			Query query = pm.newQuery(TrainUpdate.class);
			query.setOrdering("date DESC");
			query.setRange(0,1);
	
			List<TrainUpdate> oldUpdates = (List<TrainUpdate>)query.execute();
			
			
			if (oldUpdates.size() > 0)
			{
				sinceId = oldUpdates.get(0).getTwitterId();
			}						
		}
		finally
		{
			pm.close();
		}
		
		return sinceId;
	}
	
	public static void addUpdates(List<TrainUpdate> newUpdates)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<TrainUpdate> cachedUpdates = (List<TrainUpdate>)cache.get(CACHED_UPDATES_KEY);
		
		if (cachedUpdates == null)
		{
			cachedUpdates = new ArrayList<TrainUpdate>();
		}
		
		Log.info("Fetched cache with size of : " + cachedUpdates.size());
		cachedUpdates.addAll(newUpdates);
		
		try
		{
			pm.makePersistentAll(cachedUpdates);
			cachedUpdates.clear();
			Log.info("Cached cleared");
		}
//		catch(DatastoreTimeoutException ex)
//		{
//			
//		}
		finally
		{
			cache.put(CACHED_UPDATES_KEY, cachedUpdates);
			Log.info("Updated cache to size of : " + cachedUpdates.size());
			pm.close();
		}
	}
	
	public static String getUpdatesSince(long sinceId)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		JSONArray resultArray = new JSONArray();
		
		try
		{
			Query query = pm.newQuery(TrainUpdate.class);
			query.setOrdering("twitterId ASC");
			
			if (sinceId >= 0)
			{
				query.setFilter("twitterId > " + sinceId);
			}
			
			List<TrainUpdate> updates = (List<TrainUpdate>)query.execute();									
			
			for (TrainUpdate update : updates)
			{
				JSONObject updateJson = update.getJSON();
				resultArray.put(updateJson);
			}
			
			List<TrainUpdate> cachedUpdates = (List<TrainUpdate>)cache.get(CACHED_UPDATES_KEY);
			Log.info("Fetched cache with size of : " + cachedUpdates.size());
			
			if (cachedUpdates != null)
			{
				for (TrainUpdate update : cachedUpdates)
				{
					if (update.getTwitterId() > sinceId)
					{
						JSONObject updateJson = update.getJSON();
						resultArray.put(updateJson);
					}					
				}
			}
		}		
		finally
		{
			pm.close();
		}
		
		return resultArray.toString();
	}
}
