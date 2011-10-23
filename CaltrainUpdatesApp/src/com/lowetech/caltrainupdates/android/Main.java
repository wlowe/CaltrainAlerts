package com.lowetech.caltrainupdates.android;

import java.util.HashMap;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.c2dm.C2DMessaging;

public class Main extends FragmentActivity implements ServerEventListener 
{		
	
//	/**
//     * The columns we are interested in from the database
//     */
//    private static final String[] PROJECTION = new String[] {
//            TrainUpdates._ID, // 0
//            TrainUpdates.DATE, // 1
//            TrainUpdates.TEXT
//    };
    
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
//        final ImageButton buttonRefresh = (ImageButton) findViewById(R.id.buttonRefresh);
//        
//        buttonRefresh.setOnClickListener(new OnClickListener()
//		{
//			
//			public void onClick(View v)
//			{
//				manualRefreshInProgress = true;
//				setSpinnerState(true);
//				ServiceHelper.fetchUpdates(getApplicationContext());												
//			}
//		});
//        
//        Button buttonRegister = (Button)findViewById(R.id.buttonRegister);
//        
//        buttonRegister.setOnClickListener(new OnClickListener()
//		{
//			
//			public void onClick(View v)
//			{
//				
//				//C2DMessaging.register(getApplicationContext(), Constants.C2DM_SENDER);
//				NotificationsHandler.showNotification("ticker", "title", "message", getApplicationContext(), 3000);
//			}
//		});
        
        
//        Intent intent = getIntent();
//        intent.setData(TrainUpdates.CONTENT_URI);
//        
//        // Perform a managed query. The Activity will handle closing and requerying the cursor
//        // when needed.
//        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,
//                TrainUpdates.DEFAULT_SORT_ORDER);
//
//        // Used to map notes entries from the database to views
//        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
//        		this, 
//        		R.layout.two_line_list_item, 
//        		cursor,
//                new String[] { TrainUpdates.DATE, TrainUpdates.TEXT }, 
//                new int[] { R.id.date, R.id.updateText });
//        setListAdapter(adapter);	
        
        if (C2DMessaging.getRegistrationId(getApplicationContext()).length() == 0)
        {
        	C2DMessaging.register(getApplicationContext(), Constants.C2DM_SENDER);
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
		ImageButton buttonRefresh = (ImageButton) findViewById(R.id.buttonRefresh);
		ImageView imageRefresh = (ImageView) findViewById(R.id.imageRefresh);
		
		if (isActive)
		{			
			imageRefresh.setVisibility(View.VISIBLE);
			buttonRefresh.setVisibility(View.GONE);
			Animation rotate = AnimationUtils.loadAnimation(Main.this, R.anim.rotate);
			imageRefresh.startAnimation(rotate);
		}
		else
		{			
			imageRefresh.clearAnimation();
			imageRefresh.setVisibility(View.GONE);			
			buttonRefresh.setVisibility(View.VISIBLE);
		}
	}
}