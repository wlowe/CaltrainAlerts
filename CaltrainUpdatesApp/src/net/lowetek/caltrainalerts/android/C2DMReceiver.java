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
		// TODO propagate these to the UI gracefully.

	}

	@Override
	protected void onMessage(Context context, Intent intent)
	{
		
		
		Bundle extras = intent.getExtras();
		
		String msgType = extras.getString("msgType");
		String regId = extras.getString("regId");
		String storedRegId = C2DMessaging.getRegistrationId(context);
//		Log.i(TAG, "msg keys: " + regId);
		
		if (storedRegId == null || storedRegId.isEmpty())
		{
			// I don't know if this can happen.  If so, just try to unregister and ignore the message.
			C2DMessaging.unregister(context);
			return;
		}
		else if (!storedRegId.equals(regId))
		{
			Log.i(TAG, "got mismatched regId");
			try
			{
				UpdatesServer.reRegisterClient(regId, storedRegId);
			}
			catch(IOException ex)
			{
				Log.e(TAG, "Unable to reregister: ", ex);
			}
			return; 
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
		Log.i(TAG, "unregistered client");
    }
	
	
	

}
