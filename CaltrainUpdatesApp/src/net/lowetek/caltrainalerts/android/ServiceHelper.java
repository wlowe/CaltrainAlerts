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

import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.Assert;
import net.lowetek.caltrainalerts.android.UpdatesService.UpdatesResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


/**
 * A helper class to interact with {@link UpdatesService}.
 * Clients can register with this class to learn about various
 * service events.
 * @author nopayne
 *
 */
public class ServiceHelper
{
	private static final String TAG = "ServiceHelper";
	
	// Event types
	public final static int REFRESH_FINISHED_EVENT = 0;
	public final static int REGISTRATION_FINISHED_EVENT = 1;
	public final static int SERVICE_ERROR_EVENT = 2;
	
	// Keys for items added to the extras bundle
	public final static String ERROR_MSG_KEY = "ErrorMsgKey";
	
	private final static ArrayList<ServerEventListener> eventListeners = new ArrayList<ServerEventListener>();
	private final static HashSet<Integer> pendingEvents = new HashSet<Integer>();
	
	/**
	 * Registers a class to listen for server events.
	 * @param newListener
	 */
	public static void addListener(ServerEventListener newListener)
	{
		synchronized(eventListeners)
		{
			Assert.assertNotNull(newListener);		
			eventListeners.add(newListener);
		}
	}
	
	/**
	 * Unregisters a class to listen for server events.
	 * @param oldListener
	 */
	public static void removeListener(ServerEventListener oldListener)
	{
		synchronized(eventListeners)
		{
			Assert.assertNotNull(oldListener);
			eventListeners.remove(oldListener);
		}
	}
	
	/**
	 * Determines if the operation related to an event is still pending.
	 * @param eventId
	 * @return
	 */
	public static boolean isEventPending(int eventId)
	{
		synchronized(pendingEvents)
		{
			return pendingEvents.contains(Integer.valueOf(eventId));
		}
	}
	
	/**
	 * Called when a server event occurs.
	 * @param eventId
	 * @param extras
	 */
	public static void onServerEvent(int eventId, Bundle extras)
	{
		// TODO: move server related code into its own package.
		// Make this method package access only.
		synchronized(eventListeners)
		{
			synchronized(pendingEvents)
			{
				pendingEvents.remove(Integer.valueOf(eventId));
				
				for (ServerEventListener listener : eventListeners)
				{
					listener.onServerEvent(eventId, extras);
				}
				
				//TODO: a way to cache events that occur when there's no listeners.
			}
			
		}
	}
	
	public static void onNewUpdatesAvailable(UpdatesResult result, Context context)
	{
		assert result != null && result.numUpdates > 0;
		synchronized(eventListeners)
		{
			if (eventListeners.isEmpty())
			{
				String title = "Caltrain Alert";
				if (result.numUpdates == 1)
				{
					NotificationsHandler.showNotification(result.latestUpdateText, title, result.latestUpdateText, context);
				}
				else
				{
					String message = result.numUpdates + " new alerts";
					NotificationsHandler.showNotification(message, title, message, context);
				}				
			}
			else
			{
				onServerEvent(REFRESH_FINISHED_EVENT, null);
			}
			
		}
	}
	
	public static void fetchUpdates(Context context)
	{
		synchronized(pendingEvents)
		{
			Integer eventId = Integer.valueOf(REFRESH_FINISHED_EVENT);
			
			// No point in firing off a duplicate update command.
			if (pendingEvents.contains(eventId))
			{
				Log.w(TAG, "Attempted to do update update while previous is already running");
				return;
			}
			
			pendingEvents.add(eventId);
		}
		
		Intent serviceIntent = new Intent(UpdatesService.REFRESH_ACTION);
		context.startService(serviceIntent);
	}
	
	//TODO: register
}
