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

package net.lowetek.caltrainalerts.android;

import java.io.IOException;

import net.lowetek.caltrainalerts.android.Constants.TrainUpdates;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;


/**
 * @author nopayne
 *
 */
public class UpdatesService extends IntentService
{
	public static final String REFRESH_ACTION = "net.lowetek.caltrainalerts.android.refresh";
	public static final String EXTRA_LATEST_AVAILABLE_TWEET = "latestServerTwitterId";
	private static final String TAG = UpdatesService.class.getSimpleName();

	/**
	 * An exception to signify errors occuring in this service.
	 * @author nopayne
	 *
	 */
	private static class UpdatesServiceException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UpdatesServiceException(Throwable ex) 
		{
			super(ex);
		}
	}
	
	public static class UpdatesResult
	{
		int numUpdates;
		String latestUpdateText;
	}

	public UpdatesService()
	{
		super("UpdatesService");
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{
		String action = intent.getAction();
		
		if (REFRESH_ACTION.equals(action))
		{
			UpdatesResult result = null;
			
			
				String latestIdStr = intent.getStringExtra(EXTRA_LATEST_AVAILABLE_TWEET);
				long latestId = -1;
				
				if (latestIdStr != null)
				{
					latestId = Long.parseLong(latestIdStr);
				}
				
				try 
				{
					result = refreshUpdates(latestId);
				} 
				catch (UpdatesServiceException e) 
				{
					Bundle extras = new Bundle();
					extras.putString(ServiceHelper.ERROR_MSG_KEY, "Unable to fetch new alerts.");
					ServiceHelper.onServerEvent(ServiceHelper.SERVICE_ERROR_EVENT, extras);
				}
						
			if (result != null && result.numUpdates > 0)
			{
				ServiceHelper.onNewUpdatesAvailable(result, getApplicationContext());
			}
			else
			{
				ServiceHelper.onServerEvent(ServiceHelper.REFRESH_FINISHED_EVENT, null);
			}
		}

	}
	
	private UpdatesResult refreshUpdates(long latestAvailableId) throws UpdatesServiceException
	{
		try
		{
			String[] PROJECTION = new String[] {
		            TrainUpdates._ID, // 0
		            TrainUpdates.TWITTER_ID
		    };
			
			ContentResolver resolver = getContentResolver();
			Cursor cursor = resolver.query(
	                TrainUpdates.CONTENT_URI,
	                PROJECTION,    // Which columns to return.
	                null,          // WHERE clause.
	                null,          // WHERE clause value substitution
	                TrainUpdates._ID + " DESC");   // Sort order.
			
			String latestTwitterId = null;
			try
			{
				if (cursor != null && cursor.moveToFirst())
				{
					latestTwitterId = cursor.getString(1);
					
					// Check if we're already more up to date than the latest available on the server.
					// If so, no need to refresh.
					if (latestAvailableId >= 0)
					{
						long latestLocalId = Long.parseLong(latestTwitterId);
					
						if (latestLocalId > latestAvailableId)
						{
							return null;
						}
					}
						
				}
			}
			finally
			{
				if (cursor != null)
				{
					cursor.close();
				}
			}
			
			JSONArray resultArray = UpdatesServer.fetchUpdates(latestTwitterId);
			JSONObject currObject;
			
			int size = resultArray.length();
			
			if (size > 0)
			{
//				ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
//	            ContentProviderOperation.Builder builder;
				ContentValues[] values = new ContentValues[size];
				
				for (int i = 0; i < size; i++)
				{
					currObject = resultArray.getJSONObject(i);
					values[i] = new ContentValues();
					values[i].put(TrainUpdates.DATE, (Integer)currObject.opt("date"));
					values[i].put(TrainUpdates.TEXT, (String)currObject.opt("text"));
					values[i].put(TrainUpdates.TWITTER_ID, (Long)currObject.opt("twitterId"));		
				}
	
				resolver.bulkInsert(Constants.TrainUpdates.CONTENT_URI, values);
				UpdatesResult result = new UpdatesResult();
				result.numUpdates = size;
				result.latestUpdateText = resultArray.getJSONObject(size - 1).optString("text");
				return result;
			}
		}
		catch (IOException ex)
		{
			Log.e(TAG, "Error getting updates", ex);
			throw new UpdatesServiceException(ex);
		}
		catch (JSONException ex)
		{
			Log.e(TAG, "Error parsing updates", ex);
			throw new UpdatesServiceException(ex);
		}
//		catch (RemoteException ex)
//		{
//			Log.e(TAG, "Error processing updates", ex);
//		}
//		catch (OperationApplicationException ex)
//		{
//			Log.e(TAG, "Error processing updates", ex);
//		}
		
		return null;
	}

	
}
