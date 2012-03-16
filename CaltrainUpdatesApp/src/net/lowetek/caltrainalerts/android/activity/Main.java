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

package net.lowetek.caltrainalerts.android.activity;

import net.lowetek.caltrainalerts.android.Constants;
import net.lowetek.caltrainalerts.android.NotificationsHandler;
import net.lowetek.caltrainalerts.android.R;
import net.lowetek.caltrainalerts.android.ServerEventListener;
import net.lowetek.caltrainalerts.android.ServiceHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.c2dm.C2DMessaging;

/**
 * The main Activity for this application.
 * @author nopayne
 *
 */
public class Main extends SherlockFragmentActivity implements ServerEventListener 
{		
	// Action bar item IDs
	private static final int REFRESH_MENU_ID = 1;
	private static final int PREFERENCES_MENU_ID = 2;
	
	/**
	 * A key used to save the state of manual refresh.
	 */
    private static final String USER_REFRESH_STATE_KEY = "userRefreshState";
    private boolean manualRefreshInProgress = false;      
    
    /**
     * Updates the UI in response to server events.
     */
    private Handler serverEventHandler = new Handler()
    {
    	public void handleMessage(android.os.Message msg)
    	{
    		if (msg.what == ServiceHelper.REFRESH_FINISHED_EVENT)
    		{    			
    			setSupportProgressBarIndeterminateVisibility(false);
    			
    			if (!manualRefreshInProgress)
    			{
	    			String message = "New Alerts available";
	    			NotificationsHandler.showNotification(message, "Caltrain Alert", message, getApplicationContext(), 3000);
    			}
    			
    			manualRefreshInProgress = false;
    		}
    		else if (msg.what == ServiceHelper.SERVICE_ERROR_EVENT)
    		{
    			setSupportProgressBarIndeterminateVisibility(false);
    			String errorMessage = msg.getData().getString(ServiceHelper.ERROR_MSG_KEY);
    			
    			if (errorMessage == null)
    			{
    				errorMessage = "Unknown error";
    			}
    			
    			Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
    			
    		}
    	}
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(com.actionbarsherlock.view.Window.FEATURE_INDETERMINATE_PROGRESS);
        setSupportProgressBarIndeterminate(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setContentView(R.layout.main);                
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {    	    
    	MenuItem refresh = menu.add(Menu.NONE, REFRESH_MENU_ID, Menu.NONE, "");
    	refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	refresh.setIcon(R.drawable.ic_refresh);  
    	
    	MenuItem preferences = menu.add(Menu.NONE, PREFERENCES_MENU_ID, Menu.NONE, "");
    	preferences.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    	preferences.setIcon(R.drawable.ic_preferences);
    	
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) 
        {
	        case REFRESH_MENU_ID:
	        	
	        	Context context = getApplicationContext();
	        	manualRefreshInProgress = true;
	        	setSupportProgressBarIndeterminateVisibility(true);
				ServiceHelper.fetchUpdates(context);	
				
				return true;
	            
	        case PREFERENCES_MENU_ID:
	        	Intent intent = new Intent(this, Preferences.class);
	        	startActivity(intent);
	        	return true;
	        	
	        case android.R.id.home: // User touched the app logo
	        	ListFragment alertsList = (ListFragment)getSupportFragmentManager().findFragmentById(R.id.alertsList);
	        	ListView view = alertsList.getListView();

	        	// Scroll to the top of the list 
	        	view.smoothScrollToPosition(0);
	        	
	        	// For some reason smoothScroll doesn't always get the job done.
	        	// As a workaround, we attach a scroll listener to keep the scroll going until we reach the top.
	        	// At that point, remove the listener.
	        	view.setOnScrollListener(new OnScrollListener()
				{					
					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState)
					{
						if (scrollState == SCROLL_STATE_IDLE)
						{
							if (view.getFirstVisiblePosition() == 0)
							{
								view.setOnScrollListener(null);
							}
							else
							{
								view.smoothScrollToPosition(0);
							}
						}						
					}
					
					@Override
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
							int totalItemCount)
					{}
				});
	        	
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }

    }
    
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	
    	Context context = getApplicationContext();
    	
    	// Initialize user preference defaults if needed.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String firstRunKey = context.getString(R.string.firstRunKey);
        final String doAutoUpdateKey = context.getString(R.string.autoUpdateKey);        
        
        if (prefs.getBoolean(firstRunKey, true))
        {
        	Editor prefsEdit = prefs.edit();        	        	
        	prefsEdit.putBoolean(firstRunKey, false);
        	
        	// We must check if each of these preferences is set individually.  
        	// This is needed because some users won't have the first run flag set.
        	// It would be bad to overwrite their preferences.
        	
        	if (!prefs.contains(doAutoUpdateKey))
        	{
        		prefsEdit.putBoolean(doAutoUpdateKey, true);
        	}        	
        
        	final String ringtoneKey = context.getString(R.string.ringtoneKey);
        	
        	if (!prefs.contains(ringtoneKey))
        	{
	        	String defaultAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString();
	        	prefsEdit.putString(ringtoneKey, defaultAlert);
        	}
        	
        	prefsEdit.commit();
        }
        
		
		boolean doAutoUpdate = prefs.getBoolean(doAutoUpdateKey, true);
        
		// If we're not registered with the app server yet, register and grab updates.
        if (doAutoUpdate && C2DMessaging.getRegistrationId(getApplicationContext()).length() == 0)
        {
        	C2DMessaging.register(getApplicationContext(), Constants.C2DM_SENDER);
        	manualRefreshInProgress = true;
        	setSupportProgressBarIndeterminateVisibility(true);
			ServiceHelper.fetchUpdates(getApplicationContext());
        }
    	
    	ServiceHelper.addListener(this);
    	setSupportProgressBarIndeterminateVisibility(manualRefreshInProgress);
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	ServiceHelper.removeListener(this);
    	setSupportProgressBarIndeterminateVisibility(false);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {    
    	super.onSaveInstanceState(outState);
    	outState.putBoolean(USER_REFRESH_STATE_KEY, manualRefreshInProgress);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle state)
    {
    	super.onRestoreInstanceState(state);
    	manualRefreshInProgress = state.getBoolean(USER_REFRESH_STATE_KEY);
    }

	public void onServerEvent(int eventId, Bundle extras)
	{
		Message msg = new Message();
		msg.what = eventId;
		msg.setData(extras);
		serverEventHandler.sendMessage(msg);
	}
}
