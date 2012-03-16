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

import net.lowetek.caltrainalerts.android.activity.Main;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;

/**
 * Handles displaying and clearing notifications.
 * @author nopayne
 *
 */
public class NotificationsHandler
{
	private static final int ALERT_ID = 1;
	private static Notification notification = new Notification();
	private static NotificationManager notificationManager;
	private static Handler updatesHandler = new Handler();
	private static Runnable cancelAlertRunable = new Runnable()
	{
		
		public void run()
		{
			assert notificationManager != null;
			notificationManager.cancel(ALERT_ID);
		}
	};
	
	static
	{
		notification.icon = R.drawable.ic_notify_alert;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
	}
	
	private static NotificationManager getNotificationManager(Context context)
	{
		if (notificationManager == null)
		{
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		
		return notificationManager;
	}
	
	public static void showNotification(CharSequence tickerText, CharSequence title, CharSequence message, Context context)
	{
		updatesHandler.removeCallbacks(cancelAlertRunable);
		
		// Load the preferred ringtone
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String ringtoneKey = context.getString(R.string.ringtoneKey);
		String alertSound = prefs.getString(ringtoneKey, null);
		
		notification.sound = alertSound != null ? Uri.parse(alertSound) : null;
		notification.when = System.currentTimeMillis();
		Intent notificationIntent = new Intent(context, Main.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.tickerText = tickerText;
		notification.setLatestEventInfo(context, title, message, contentIntent);
		
		// Fire off the notification
		getNotificationManager(context).notify(ALERT_ID, notification);
	}
	
	public static void showNotification(CharSequence tickerText, CharSequence title, CharSequence message, Context context, long delay)
	{
		showNotification(tickerText, title, message, context);
		
		// Clear out the notification after a while.
		updatesHandler.postDelayed(cancelAlertRunable, delay);
	}
	
	public static void clearNotification(Context context)
	{
		getNotificationManager(context).cancel(ALERT_ID);
	}
}
