package com.lowetech.caltrainupdates.android.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.lowetech.caltrainupdates.android.R;

public class Preferences extends PreferenceActivity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	}
    
}
