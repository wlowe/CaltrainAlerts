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

import java.util.HashMap;

import net.lowetek.caltrainalerts.android.R.drawable;
import net.lowetek.caltrainalerts.android.R.layout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.c2dm.C2DMessaging;

import net.lowetek.caltrainalerts.android.Constants;
import net.lowetek.caltrainalerts.android.NotificationsHandler;
import net.lowetek.caltrainalerts.android.R;
import net.lowetek.caltrainalerts.android.ServerEventListener;
import net.lowetek.caltrainalerts.android.ServiceHelper;

public class Main extends FragmentActivity implements ServerEventListener 
{		
	private static final int REFRESH_MENU_ID = 1;
	private static final int PREFERENCES_MENU_ID = 2;
	
    private static final String USER_REFRESH_STATE = "userRefreshState";
    private boolean manualRefreshInProgress = false;
    
    private Handler serverEventHandler = new Handler()
    {
    	public void handleMessage(android.os.Message msg)
    	{
    		if (msg.what == ServiceHelper.REFRESH_FINISHED_EVENT)
    		{    			
    			setSpinnerState(false);
    			
    			if (!manualRefreshInProgress)
    			{
	    			String message = "New Alerts available";
	    			NotificationsHandler.showNotification(message, "Caltrain Alert", message, getApplicationContext(), 3000);
    			}
    			
    			manualRefreshInProgress = false;
    		}
    	}
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String doAutoUpdateKey = context.getString(R.string.autoUpdateKey);
		boolean doAutoUpdate = prefs.getBoolean(doAutoUpdateKey, true);
        
		// If we're not registered yet, register and grab updates.
        if (doAutoUpdate && C2DMessaging.getRegistrationId(getApplicationContext()).length() == 0)
        {
        	C2DMessaging.register(getApplicationContext(), Constants.C2DM_SENDER);
        	manualRefreshInProgress = true;
        	setSpinnerState(true);
			ServiceHelper.fetchUpdates(getApplicationContext());
        }
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
	        	manualRefreshInProgress = true;
				setSpinnerState(true);
				ServiceHelper.fetchUpdates(getApplicationContext());	
	            return true;
	            
	        case PREFERENCES_MENU_ID:
	        	Intent intent = new Intent(this, Preferences.class);
	        	startActivity(intent);
	        	return true;
	        	
	        case android.R.id.home: // User touched the app logo
	        	ListFragment alertsList = (ListFragment)getSupportFragmentManager().findFragmentById(R.id.alertsList);
	        	alertsList.getListView().smoothScrollToPosition(0);
	        	
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }

    }
    
    
    @Override
    protected void onResume()
    {
    	super.onResume();
    	ServiceHelper.addListener(this);
    	setSpinnerState(manualRefreshInProgress);
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	ServiceHelper.removeListener(this);
    	setSpinnerState(false);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {    
    	super.onSaveInstanceState(outState);
    	outState.putBoolean(USER_REFRESH_STATE, manualRefreshInProgress);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle state)
    {
    	super.onRestoreInstanceState(state);
    	manualRefreshInProgress = state.getBoolean(USER_REFRESH_STATE);
    }

	public void onServerEvent(int eventId, HashMap<String, Object> extras)
	{
		serverEventHandler.sendEmptyMessage(eventId);
	}
	
	private void setSpinnerState(boolean isActive)
	{
		//TODO: feedback for when refreshing...
	}
}
