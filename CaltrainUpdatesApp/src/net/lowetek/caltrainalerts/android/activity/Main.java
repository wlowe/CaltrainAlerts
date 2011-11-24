package net.lowetek.caltrainalerts.android.activity;

import java.util.HashMap;

import net.lowetek.caltrainalerts.android.R.drawable;
import net.lowetek.caltrainalerts.android.R.layout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
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
        
        //TODO: need to check settings before registering
        if (C2DMessaging.getRegistrationId(getApplicationContext()).length() == 0)
        {
        	C2DMessaging.register(getApplicationContext(), Constants.C2DM_SENDER);
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
