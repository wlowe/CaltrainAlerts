/**
 * 
 */
package com.lowetech.caltrainupdates.android;

import android.app.Notification;

/**
 * @author nopayne
 * A notification that is able to clear itself at a preset time.
 */
public class ExpiringNotification extends Notification
{
	public long expireAt = -1;
}
