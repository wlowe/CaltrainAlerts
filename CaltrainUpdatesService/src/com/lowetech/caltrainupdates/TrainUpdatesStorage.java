/**
 * 
 */
package com.lowetech.caltrainupdates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;
import twitter4j.auth.AccessToken;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONObject;

/**
 * Manages storage and retrieval of TrainUpdate objects.
 * This will transparently deal with caching objects.
 * 
 * @author nopayne
 *
 */
public class TrainUpdatesStorage
{
	private static Cache cache;
	private static String CACHED_UPDATES_KEY = "CachedUpdates";
	private static String ACCESS_TOKEN_KEY = "AccessToken";
	
	private static final Logger log = Logger.getLogger(TrainUpdatesStorage.class.getName());
	
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
		@SuppressWarnings("unchecked")
		List<TrainUpdate> cachedUpdates = (List<TrainUpdate>)cache.get(CACHED_UPDATES_KEY);
		

		if (cachedUpdates != null && !cachedUpdates.isEmpty())
		{
			log.info("Fetched cache with size of : " + cachedUpdates.size());
			return cachedUpdates.get(cachedUpdates.size() - 1).getTwitterId();
		}
		
		long sinceId = -1;		
		PersistenceManager pm = PMF.get().getPersistenceManager();		
		
		try
		{
			Query query = pm.newQuery(TrainUpdate.class);
			query.setOrdering("date DESC");
			query.setRange(0,1);
	
			@SuppressWarnings("unchecked")
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
		@SuppressWarnings("unchecked")
		List<TrainUpdate> cachedUpdates = (List<TrainUpdate>)cache.get(CACHED_UPDATES_KEY);
		
		if (cachedUpdates == null)
		{
			cachedUpdates = new ArrayList<TrainUpdate>();
		}
		
		log.info("Fetched cache with size of : " + cachedUpdates.size());
		cachedUpdates.addAll(newUpdates);
		
		try
		{			
			pm.makePersistentAll(cachedUpdates);
			cachedUpdates.clear();
			log.info("Cache cleared");
		}
		catch(DatastoreTimeoutException ex)
		{
			log.info("Couldn't write to datastore.  caching instead");
		}
		finally
		{
			cache.put(CACHED_UPDATES_KEY, cachedUpdates);
			log.info("Updated cache to size of : " + cachedUpdates.size());
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
			
			@SuppressWarnings("unchecked")
			List<TrainUpdate> updates = (List<TrainUpdate>)query.execute();									
			
			for (TrainUpdate update : updates)
			{
				JSONObject updateJson = update.getJSON();
				resultArray.put(updateJson);
			}
			
			@SuppressWarnings("unchecked")
			List<TrainUpdate> cachedUpdates = (List<TrainUpdate>)cache.get(CACHED_UPDATES_KEY);
			
			
			if (cachedUpdates != null)
			{
				log.info("Fetched cache with size of : " + cachedUpdates.size());
				
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
	
	public static void setAccessToken(AccessToken accessToken)
	{
		StoredAccessToken storedToken = new StoredAccessToken(accessToken);

		// Add the token to the cache.
		cache.put(ACCESS_TOKEN_KEY, storedToken);
		
		// Now persist it from the datastore
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query delQuery = pm.newQuery(StoredAccessToken.class);
		
		try
		{
			delQuery.deletePersistentAll();
			pm.makePersistent(storedToken);
		}
		finally
		{
			delQuery.closeAll();
			pm.close();
		}				
	}
	
	public static AccessToken getAccessToken()
	{
		// Try to grab the token from the cache.
		StoredAccessToken storedToken = (StoredAccessToken)cache.get(ACCESS_TOKEN_KEY);
		
		if (storedToken != null)
		{
			return storedToken.getAccessToken();
		}
		
		// Now try to grab it from the datastore
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Extent<StoredAccessToken> extent = pm.getExtent(StoredAccessToken.class, false);
		
		try
		{
			Iterator<StoredAccessToken> itr = extent.iterator();
			storedToken = itr.next();
			
			// If the token was found, cache it and return.
			if (storedToken != null)
			{
				cache.put(ACCESS_TOKEN_KEY, storedToken);
				return storedToken.getAccessToken();
			}
		}
		finally
		{
			extent.closeAll();
			pm.close();
		}
		
		return null;
		
	}
}
