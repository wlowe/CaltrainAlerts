package net.lowetek.caltrainalerts.android.activity;

import net.lowetek.caltrainalerts.android.C2DMReceiver;
import net.lowetek.caltrainalerts.android.Constants;
import net.lowetek.caltrainalerts.android.UpdatesServer;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.google.android.c2dm.C2DMessaging;
import net.lowetek.caltrainalerts.android.R;



public class Preferences extends PreferenceActivity 
{
	private class PreferenceChangeListener implements OnSharedPreferenceChangeListener
	{
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
		{
			String autoUpdateKey = Preferences.this.getString(R.string.autoUpdateKey);
			
			if (autoUpdateKey.equals(key))
			{
				SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
				boolean updateState = prefs.getBoolean(key, true);
				
				if (updateState)
				{
					C2DMessaging.register(getApplicationContext(), Constants.C2DM_SENDER);
				}
				else
				{
					C2DMessaging.unregister(getApplicationContext());
				}
			}
		}
	}
	
	private PreferenceChangeListener prefListener;
	
	public Preferences() 
	{
		prefListener = new PreferenceChangeListener();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    
	    
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		prefs.registerOnSharedPreferenceChangeListener(prefListener);
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		prefs.unregisterOnSharedPreferenceChangeListener(prefListener);
	}
    
}
