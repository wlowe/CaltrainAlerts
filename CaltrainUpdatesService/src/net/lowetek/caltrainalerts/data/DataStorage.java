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

package net.lowetek.caltrainalerts.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
public class DataStorage
{
	private static Cache cache;

	// Keys for various objects stored in the cache.
	private static final String CACHED_UPDATES_KEY = "CachedUpdates";
	private static final String ACCESS_TOKEN_KEY = "AccessToken";
	private static final String CACHED_QUERIES_KEY = "CachedQueries";
	private static final String STORAGE_STATS_KEY = "StorageStats";
	
	private static final Logger log = Logger.getLogger(DataStorage.class.getName());
	
	static
	{
		try 
		{
            cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        } 
		catch (CacheException e) 
		{
            log.log(Level.SEVERE, "Unable to initialize cache.", e);
        }

	}

	/**
	 * Gets the ID of the last pulled train update.
	 * @return the ID or -1 if there aren't any stored IDs.
	 */
	public static long getLatestUpdateId()
	{
		StorageStats stats = getStorageStats();
		
		if (stats != null)
		{
			return stats.getLatestUpdateId();
		}
		
		//TODO: will stats ever be null?
		
		
		//TODO:  We should store the latest ID instead of querying for it.
		// That way we don't run into a situation where the data store gets cleared and we end up pulling 200 updates.

		// Attempt to just pull the value out of the cache.
		// We assume that the latest updates will always live here.
		@SuppressWarnings("unchecked")
		List<TrainUpdate> cachedUpdates = (List<TrainUpdate>)cache.get(CACHED_UPDATES_KEY);
		

		if (cachedUpdates != null && !cachedUpdates.isEmpty())
		{
			log.info("Fetched cache with size of : " + cachedUpdates.size());
			return cachedUpdates.get(cachedUpdates.size() - 1).getTwitterId();
		}
		
		// If no cached updates were found, we must query the datastore.
		
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
	
	/**
	 * Adds new updates to storage.
	 * @param newUpdates
	 */
	public static void addUpdates(List<TrainUpdate> newUpdates)
	{
		if (newUpdates.isEmpty())
		{
			return;
		}
		
		// Add the new updates to the list of cached results.
		@SuppressWarnings("unchecked")
		List<TrainUpdate> cachedUpdates = (List<TrainUpdate>)cache.get(CACHED_UPDATES_KEY);
		
		if (cachedUpdates == null)
		{
			cachedUpdates = new ArrayList<TrainUpdate>();
		}
		
		log.info("Fetched cache with size of : " + cachedUpdates.size());
		cachedUpdates.addAll(newUpdates);
		// Update the storage stats
		StorageStats stats = getStorageStats();
		stats.setLatestUpdateId(newUpdates.get(0).getTwitterId());
		
		// Attempt to persist the updates to the data store.
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		try
		{			
			pm.makePersistentAll(cachedUpdates);
			pm.makePersistent(stats);
			cachedUpdates.clear();
			log.info("Cache cleared");
		}
		catch(DatastoreTimeoutException ex)
		{
			log.info("Couldn't write to datastore.  caching instead");
		}
		finally
		{
			// No matter what happens, store the new cached results.
			cache.remove(CACHED_QUERIES_KEY);
			cache.put(CACHED_UPDATES_KEY, cachedUpdates);
			cache.put(STORAGE_STATS_KEY, stats);
			log.info("Updated cache to size of : " + cachedUpdates.size());
			pm.close();
		}
	}
	
	public static String getUpdatesSince(long sinceId)
	{
		// First try to pull the response from the cache.
		@SuppressWarnings("unchecked")
		Map<Long, String> cachedQueries = (Map<Long, String>)cache.get(CACHED_QUERIES_KEY);
		Long sinceIdObj = Long.valueOf(sinceId);
		
		if (cachedQueries == null)
		{
			cachedQueries = new HashMap<Long, String>();
		}		
		else if (cachedQueries.containsKey(sinceIdObj))
		{
			log.info("Found query in the cache: " + sinceId);
			return cachedQueries.get(sinceIdObj);
		}	
				
		// If we haven't cached this response, we must query for it.
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
		
			// Append any updates that are stored in the cache to this result.
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
		
		// Finally cache the response.
		String result = resultArray.toString();
		cachedQueries.put(sinceIdObj, result);
		cache.put(CACHED_QUERIES_KEY, cachedQueries);
		
		return result;
	}
	
	/**
	 * Saves the access token required by twitter.
	 * @param accessToken
	 */
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
	
	/**
	 * Retrieves the access token used for interacting with Twitter.
	 * @return
	 */
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
	
	protected static StorageStats getStorageStats()
	{
		// Try to grab the token from the cache.
		StorageStats stats = (StorageStats)cache.get(STORAGE_STATS_KEY);
		
		if (stats != null)
		{
			return stats;
		}
		
		// Now try to grab it from the datastore
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Extent<StorageStats> extent = pm.getExtent(StorageStats.class, false);
		
		try
		{
			Iterator<StorageStats> itr = extent.iterator();
			
			if (itr.hasNext())
			{
				stats = itr.next();
			}			
			else
			{
				stats = new StorageStats();
				pm.makePersistent(stats);
			}									
			
			return stats;
		}
		finally
		{
			extent.closeAll();
			pm.close();
			
			cache.put(STORAGE_STATS_KEY, stats);
		}				
	}
}
