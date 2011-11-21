package com.lowetech.caltrainupdates.android;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.google.android.c2dm.C2DMessaging;

//TODO: merge this class with UpdatesServer
public class C2DMReceiver extends C2DMBaseReceiver
{
	private static final String TAG = C2DMReceiver.class.getSimpleName();
	
	public C2DMReceiver() 
	{
        super(Constants.C2DM_SENDER);
    }

	@Override
	public void onError(Context context, String errorId)
	{
		Log.e(TAG, errorId);
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMessage(Context context, Intent intent)
	{
		
		
		Bundle extras = intent.getExtras();
		
		String msgType = extras.getString("msgType");
		String regId = extras.getString("regId");
//		Log.i(TAG, "msg keys: " + regId);
		
		if (!C2DMessaging.getRegistrationId(context).equals(regId))
		{
			Log.i(TAG, "got mismatched regId");
			// 
		}
		
		Log.i(TAG, "got C2DMessage " + msgType);
		
		if (msgType.equals("notify"))
		{			
			String twitterId = extras.getString("twitterId");
			
			if (twitterId != null)
			{		
				Intent serviceIntent = new Intent(UpdatesService.REFRESH_ACTION);
				serviceIntent.putExtra("latestServerTwitterId", twitterId);
				startService(serviceIntent);
			}
		}
		else if (msgType.equals("ping"))
		{
			UpdatesService.UpdatesResult result = new UpdatesService.UpdatesResult();
			result.numUpdates = 1;
			result.latestUpdateText = extras.getString("message");
			ServiceHelper.onNewUpdatesAvailable(result, getApplicationContext());
		}
		else
		{
			Log.e(TAG, "unknown message type: " + msgType);
		}
	}
	
	
	@Override
    public void onRegistered(Context context, String registrationId) throws IOException 
    {
		Log.i(TAG, "registering client...");
		UpdatesServer.registerClient(registrationId);		
    }

	@Override
    public void onUnregistered(Context context) 
    {
    }
	
	
	

}
