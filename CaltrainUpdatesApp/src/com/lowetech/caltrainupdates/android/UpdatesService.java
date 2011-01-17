/**
 * 
 */
package com.lowetech.caltrainupdates.android;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import com.lowetech.caltrainupdates.android.Constants.TrainUpdates;

/**
 * @author nopayne
 *
 */
public class UpdatesService extends IntentService
{
	public static final String REFRESH_ACTION = "com.lowetech.caltrainupdates.android.refresh";
	public static final String EXTRA_LATEST_AVAILABLE_TWEET = "latestServerTwitterId";
	private static final String TAG = UpdatesService.class.getSimpleName();

	public UpdatesService()
	{
		super("UpdatesService");
		// TODO Auto-generated constructor stub
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
			String latestIdStr = intent.getStringExtra(EXTRA_LATEST_AVAILABLE_TWEET);
			long latestId = -1;
			
			if (latestIdStr != null)
			{
				latestId = Long.parseLong(latestIdStr);
			}
			
			refreshUpdates(latestId);
		}

	}
	
	private void refreshUpdates(long latestAvailableId)
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
							return;
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
			ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
            ContentProviderOperation.Builder builder;
			
			for (int i = 0; i < size; i++)
			{
				currObject = resultArray.getJSONObject(i);
				builder = ContentProviderOperation.newInsert(Constants.TrainUpdates.CONTENT_URI);
				builder.withValue(TrainUpdates.DATE, currObject.opt("date"));
				builder.withValue(TrainUpdates.TEXT, currObject.opt("text"));
				builder.withValue(TrainUpdates.TWITTER_ID, currObject.opt("twitterId"));
				operationList.add(builder.build());
			}

			resolver.applyBatch(Constants.AUTHORITY, operationList);
		}
		catch (IOException ex)
		{
			Log.e(TAG, "Error getting updates", ex);
		}
		catch (JSONException ex)
		{
			Log.e(TAG, "Error parsing updates", ex);
		}
		catch (RemoteException ex)
		{
			Log.e(TAG, "Error processing updates", ex);
		}
		catch (OperationApplicationException ex)
		{
			Log.e(TAG, "Error processing updates", ex);
		}
	}

}
