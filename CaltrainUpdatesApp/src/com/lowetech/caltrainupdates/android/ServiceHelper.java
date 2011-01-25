/**
 * 
 */
package com.lowetech.caltrainupdates.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import junit.framework.Assert;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author nopayne
 *
 */
public class ServiceHelper
{
	private static final String TAG = "ServiceHelper";
	
	public final static int REFRESH_FINISHED_EVENT = 0;
	public final static int REGISTRATION_FINISHED_EVENT = 1;
	
	private final static ArrayList<ServerEventListener> eventListeners = new ArrayList<ServerEventListener>();
	private final static HashSet<Integer> pendingEvents = new HashSet<Integer>();
	
	public static void addListener(ServerEventListener newListener)
	{
		synchronized(eventListeners)
		{
			Assert.assertNotNull(newListener);		
			eventListeners.add(newListener);
		}
	}
	
	public static void removeListener(ServerEventListener oldListener)
	{
		synchronized(eventListeners)
		{
			Assert.assertNotNull(oldListener);
			eventListeners.remove(oldListener);
		}
	}
	
	public static boolean isEventPending(int eventId)
	{
		synchronized(pendingEvents)
		{
			return pendingEvents.contains(new Integer(eventId));
		}
	}
	
	public static void onServerEvent(int eventId, HashMap<String, Object> extras)
	{
		synchronized(eventListeners)
		{
			synchronized(pendingEvents)
			{
				pendingEvents.remove(new Integer(eventId));
				
				for (ServerEventListener listener : eventListeners)
				{
					listener.onServerEvent(eventId, extras);
				}
				
				//TODO: a way to cache events that occur when there's no listeners.
			}
			
		}
	}
	
	public static void fetchUpdates(Context context)
	{
		synchronized(pendingEvents)
		{
			Integer eventId = new Integer(REFRESH_FINISHED_EVENT);
			
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
