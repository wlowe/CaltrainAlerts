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
import net.lowetek.caltrainalerts.android.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.google.android.c2dm.C2DMessaging;

/**
 * The User Preferences Activity.
 * @author nopayne
 *
 */
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
				// If the user changed the auto-update setting then we must
				// Update our registration.
				// TODO: consider deferring this until the user exits the activity.
				// Make sure to do it asynchronously.
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
