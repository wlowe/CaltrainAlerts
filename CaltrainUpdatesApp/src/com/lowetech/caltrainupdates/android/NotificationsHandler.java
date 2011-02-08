/**
 * 
 */
package com.lowetech.caltrainupdates.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
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
		notification.icon = android.R.drawable.stat_notify_call_mute;
		notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
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
		notification.when = System.currentTimeMillis();
		Intent notificationIntent = new Intent(context, Main.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.tickerText = tickerText;
		notification.setLatestEventInfo(context, title, message, contentIntent);
		getNotificationManager(context).notify(ALERT_ID, notification);
	}
	
	public static void showNotification(CharSequence tickerText, CharSequence title, CharSequence message, Context context, long delay)
	{
		showNotification(tickerText, title, message, context);
		updatesHandler.postDelayed(cancelAlertRunable, delay);
	}
	
	public static void clearNotification(Context context)
	{
		getNotificationManager(context).cancel(ALERT_ID);
	}
}
