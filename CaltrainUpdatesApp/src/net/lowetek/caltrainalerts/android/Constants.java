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

package net.lowetek.caltrainalerts.android;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;

public final class Constants
{
	public static final String AUTHORITY = "net.lowetek.caltrainalerts";
	
	public static final String C2DM_SENDER = "caltrainupdates@gmail.com";
		
	
	private static final Properties prefs = new Properties();
	private static boolean prefsLoaded = false;
	private static final String PREFS_SERVER_URL = "ServerURL";
	private static final String SERVER_URL_DEFAULT = "caltrainupdates.appspot.com";
	
	public static String getUpdatesServerUrl()
	{
		return getPrefs().getProperty(PREFS_SERVER_URL, SERVER_URL_DEFAULT);						
	}
	
	private static Properties getPrefs()
	{
		if (!prefsLoaded)
		{
			// Attempt to load debug prefs file.
			// This should fail in most cases.
			try
			{
				String mediaState = Environment.getExternalStorageState();
				
				if (mediaState.equals(Environment.MEDIA_MOUNTED) || mediaState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))
				{
					File baseStorageDir = Environment.getExternalStorageDirectory();
					File prefsFile = new File(baseStorageDir.getAbsolutePath() + "/Android/data/net.lowetek.caltrainalerts/files/debug.properties");
					
					if (prefsFile.exists() && prefsFile.canRead())
					{
						prefs.load(new FileInputStream(prefsFile));
											
					}
					
				}
			}
			catch(Exception ex)
			{}
			
			prefsLoaded = true;
		}
		return prefs;
	}
	
	
	private Constants() {}
	
	public static final class TrainUpdates implements BaseColumns
	{
		private TrainUpdates() {}
		
		/**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/updates");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of updates.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lowetech.update";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single update.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.lowetech.update";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "twitterId DESC";
        
        public static final String DATE = "timestamp";
        
        public static final String DATE_HEADER = "timestamp_header";
        
        //public static final String KEY = "key";
        
        public static final String TEXT = "text";
        
        public static final String TWITTER_ID = "twitterId";

	}
}
