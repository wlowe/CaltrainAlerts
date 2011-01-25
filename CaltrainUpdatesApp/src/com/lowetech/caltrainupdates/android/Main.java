package com.lowetech.caltrainupdates.android;

import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import com.google.android.c2dm.C2DMessaging;
import com.lowetech.caltrainupdates.android.Constants.TrainUpdates;

public class Main extends ListActivity implements ServerEventListener 
{		
	
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            TrainUpdates._ID, // 0
            TrainUpdates.DATE, // 1
            TrainUpdates.TEXT
    };
    
    private Handler serverEventHandler = new Handler()
    {
    	public void handleMessage(android.os.Message msg)
    	{
    		if (msg.what == ServiceHelper.REFRESH_FINISHED_EVENT)
    		{
    			setSpinnerState(false);		
    		}
    	}
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final ImageButton buttonRefresh = (ImageButton) findViewById(R.id.buttonRefresh);
        
        buttonRefresh.setOnClickListener(new OnClickListener()
		{
			
			public void onClick(View v)
			{
				ServiceHelper.fetchUpdates(getApplicationContext());				
				setSpinnerState(true);				
			}
		});
        
        Button buttonRegister = (Button)findViewById(R.id.buttonRegister);
        
        buttonRegister.setOnClickListener(new OnClickListener()
		{
			
			public void onClick(View v)
			{
				C2DMessaging.register(getApplicationContext(), Constants.C2DM_SENDER);				
			}
		});
        
        
        Intent intent = getIntent();
        intent.setData(TrainUpdates.CONTENT_URI);
        
     // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,
                TrainUpdates.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, cursor,
                new String[] { TrainUpdates.DATE, TrainUpdates.TEXT }, new int[] { android.R.id.text1, android.R.id.text2 });
        setListAdapter(adapter);
        
    	
    }
    
    @Override
    protected void onStart()
    { 
    	super.onStart();    	
    	ServiceHelper.addListener(this);
    	boolean spinnerState = ServiceHelper.isEventPending(ServiceHelper.REFRESH_FINISHED_EVENT);
    	setSpinnerState(spinnerState);
    }
    
    @Override
    protected void onStop()
    {
    	super.onStop();    	
    	ServiceHelper.removeListener(this);
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