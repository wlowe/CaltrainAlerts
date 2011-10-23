/**
 * 
 */
package com.lowetech.caltrainupdates.android;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.lowetech.caltrainupdates.android.Constants.TrainUpdates;

/**
 * @author nopayne
 *
 */
public class AlertsListFragment extends ListFragment
{
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            TrainUpdates._ID, // 0
            TrainUpdates.DATE, // 1
            TrainUpdates.TEXT
    };
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
//        setContentView(R.layout.main);
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
//        
        
        Intent intent = getActivity().getIntent();
        intent.setData(TrainUpdates.CONTENT_URI);
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = getActivity().managedQuery(intent.getData(), PROJECTION, null, null,
                TrainUpdates.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
        		getActivity(), 
        		R.layout.two_line_list_item, 
        		cursor,
                new String[] { TrainUpdates.DATE, TrainUpdates.TEXT }, 
                new int[] { R.id.date, R.id.updateText });
        setListAdapter(adapter);	
        
//        if (C2DMessaging.getRegistrationId(getApplicationContext()).length() == 0)
//        {
//        	C2DMessaging.register(getApplicationContext(), Constants.C2DM_SENDER);
//        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.alerts_list, container, false);
	}
}
