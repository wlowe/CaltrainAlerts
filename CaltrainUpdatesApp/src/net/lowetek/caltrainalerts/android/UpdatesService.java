/**
 * 
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

	
	public static class UpdatesResult
	{
		int numUpdates;
		String latestUpdateText;
	}

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
			UpdatesResult result = null;
			
			try
			{
				String latestIdStr = intent.getStringExtra(EXTRA_LATEST_AVAILABLE_TWEET);
				long latestId = -1;
				
				if (latestIdStr != null)
				{
					latestId = Long.parseLong(latestIdStr);
				}
				
				result = refreshUpdates(latestId);
				

			}
			finally
			{				
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

	}
	
	private UpdatesResult refreshUpdates(long latestAvailableId)
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
					
					
//					currObject = resultArray.getJSONObject(i);
//					builder = ContentProviderOperation.newInsert(Constants.TrainUpdates.CONTENT_URI);
//					builder.withValue(TrainUpdates.DATE, currObject.opt("date"));
//					builder.withValue(TrainUpdates.TEXT, currObject.opt("text"));
//					builder.withValue(TrainUpdates.TWITTER_ID, currObject.opt("twitterId"));
//					operationList.add(builder.build());
					
					
				}
	
//				resolver.applyBatch(Constants.AUTHORITY, operationList);
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
		}
		catch (JSONException ex)
		{
			Log.e(TAG, "Error parsing updates", ex);
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
