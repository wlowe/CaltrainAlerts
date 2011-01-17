package com.lowetech.caltrainupdates.android;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;

public final class Constants
{
	public static final String AUTHORITY = "com.lowetech.caltrainupdates";
	
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
					File prefsFile = new File(baseStorageDir.getAbsolutePath() + "/Android/data/com.lowetech.caltrainupdates/files/debug.properties");
					
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
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        
        public static final String DATE = "date";
        
        //public static final String KEY = "key";
        
        public static final String TEXT = "text";
        
        public static final String TWITTER_ID = "twitterId";

	}
}
