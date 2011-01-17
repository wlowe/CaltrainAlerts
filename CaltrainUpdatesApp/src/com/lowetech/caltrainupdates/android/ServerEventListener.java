/**
 * 
 */
package com.lowetech.caltrainupdates.android;

import java.util.HashMap;

/**
 * A listener for content events that come from the server.
 * 
 * @author nopayne
 *
 */
public interface ServerEventListener
{
	public void onServerEvent(int eventId, HashMap<String,Object> extras);
}
