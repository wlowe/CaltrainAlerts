package com.lowetech.caltrainupdates.android;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Constants
{
	public static final String AUTHORITY = "com.lowetech.caltrainupdates";
	
	public static final String C2DM_SENDER = "caltrainupdates@gmail.com";
	
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
