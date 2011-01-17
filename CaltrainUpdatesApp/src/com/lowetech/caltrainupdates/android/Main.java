package com.lowetech.caltrainupdates.android;

import java.io.IOException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

import com.google.android.c2dm.C2DMessaging;
import com.lowetech.caltrainupdates.android.Constants.TrainUpdates;

public class Main extends ListActivity 
{
	
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            TrainUpdates._ID, // 0
            TrainUpdates.DATE, // 1
            TrainUpdates.TEXT
    };
	
	private static class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {
	     protected Long doInBackground(URL... urls) 
	     {

	    	 JSONArray result;
	 		try
	 		{
	 			result = UpdatesServer.fetchUpdates(null);
	 			System.out.println(result);
	 		}
	 		catch (IOException e)
	 		{
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}
	 		catch (JSONException e)
	 		{
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 		}
	    	 
	         return 0L;
	     }

	     protected void onProgressUpdate(Integer... progress) {
//	         setProgressPercent(progress[0]);
	     }

	     protected void onPostExecute(Long result) {
//	         showDialog("Downloaded " + result + " bytes");
	     }
	 }

	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button buttonRefresh = (Button) findViewById(R.id.Button01);
        
        buttonRefresh.setOnClickListener(new OnClickListener()
		{
			
			public void onClick(View v)
			{
				Intent serviceIntent = new Intent(UpdatesService.REFRESH_ACTION);
				startService(serviceIntent);
				
			}
		});
        
        Button buttonRegister = (Button)findViewById(R.id.buttonRegister);
        
        buttonRegister.setOnClickListener(new OnClickListener()
		{
			
			public void onClick(View v)
			{
				C2DMessaging.register(getApplicationContext(), Constants.C2DM_SENDER);
//				Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
//		        registrationIntent.setPackage("com.google.android.gsf");
//		        registrationIntent.putExtra("app",
//		                PendingIntent.getBroadcast(Main.this, 0, new Intent(), 0));
//		        registrationIntent.putExtra("sender", "jumpnoteapp@gmail.com");//Constants.C2DM_SENDER);
//		        ComponentName serviceName = Main.this.startService(registrationIntent);
//		        Log.i("C2DMessaging", serviceName.flattenToString());
				
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
}