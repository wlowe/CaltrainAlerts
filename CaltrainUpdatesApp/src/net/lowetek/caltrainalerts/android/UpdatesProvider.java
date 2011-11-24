/**
 * 
 */
package net.lowetek.caltrainalerts.android;

import java.util.Calendar;
import java.util.TimeZone;

import net.lowetek.caltrainalerts.android.Constants.TrainUpdates;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


/**
 * @author nopayne
 *
 */
public class UpdatesProvider extends ContentProvider
{
	private static final String TAG = "UpdatesProvider";
	private static final String DATABASE_NAME = "caltrain_updates.db";
	private static final int DATABASE_VERSION = 5;
	private static final String UPDATES_TABLE_NAME = "updates";
	
	private static final int UPDATE = 1;
	private static final int UPDATE_ID = 2;
	
	private static final int SECONDS_PER_DAY = 86400;
	
	private static final UriMatcher sUriMatcher;
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL("CREATE TABLE " + UPDATES_TABLE_NAME + " (" 
					+ TrainUpdates._ID + " INTEGER PRIMARY KEY," 
					+ TrainUpdates.TEXT + " TEXT," 
					+ TrainUpdates.DATE + " INTEGER,"
					+ TrainUpdates.DATE_HEADER + " INTEGER, "
					+ TrainUpdates.TWITTER_ID + " INTEGER" + ");");
						
			db.execSQL("CREATE TRIGGER insert_deleteOldUpdates AFTER  INSERT ON " + UPDATES_TABLE_NAME + " \n" +
		         "BEGIN \n" +
		         " DELETE FROM " + UPDATES_TABLE_NAME + " WHERE _id = (new._id - 100); \n" +
		         "END;");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + UPDATES_TABLE_NAME);
			onCreate(db);
		}
	}
	
	private DatabaseHelper dbHelper; 
	
	 static 
	 {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Constants.AUTHORITY, "updates", UPDATE);
		sUriMatcher.addURI(Constants.AUTHORITY, "updates/#", UPDATE_ID);	       
	 }
	
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs)
	{
		 SQLiteDatabase db = dbHelper.getWritableDatabase();
	        int count;
	        switch (sUriMatcher.match(uri)) {
	        case UPDATE:
	            count = db.delete(UPDATES_TABLE_NAME, where, whereArgs);
	            break;

	        case UPDATE_ID:
	            String updateId = uri.getPathSegments().get(1);
	            count = db.delete(UPDATES_TABLE_NAME, TrainUpdates._ID + "=" + updateId
	                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;

	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
	        }

	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri)
	{
		switch(sUriMatcher.match(uri))
		{
			case UPDATE:
				return TrainUpdates.CONTENT_TYPE;
				
			case UPDATE_ID:
				return TrainUpdates.CONTENT_ITEM_TYPE;
			
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	//TODO: single insert may be dead code.  consider removing.
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues initialValues)
	{
		throw new UnsupportedOperationException("no single insert!");
//		 // Validate the requested uri
//        if (sUriMatcher.match(uri) != UPDATE) {
//            throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//
//        ContentValues values;
//        if (initialValues != null) {
//            values = new ContentValues(initialValues);
//        } else {
//            values = new ContentValues();
//        }
//
//       //TODO: validate col data
//
//       
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        long rowId = db.insert(UPDATES_TABLE_NAME, TrainUpdates.TEXT, values);
//        
//        if (rowId > 0) {
//            Uri updateUri = ContentUris.withAppendedId(TrainUpdates.CONTENT_URI, rowId);
//            getContext().getContentResolver().notifyChange(updateUri, null);
//            return updateUri;
//        }
//
//        throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values)
	{
		 // Validate the requested uri
        if (sUriMatcher.match(uri) != UPDATE) 
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        DatabaseUtils.InsertHelper inserter = new DatabaseUtils.InsertHelper(db, UPDATES_TABLE_NAME);
        int numRows = 0;
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Long dayStart = cal.getTimeInMillis() / 1000;
        Long nextDayStart = dayStart + SECONDS_PER_DAY;
        
        db.beginTransaction();
        
        try
        {
        	for (ContentValues value : values)
        	{
        		inserter.insert(value);
        		numRows++;
        	}
        	
        	ContentValues newHeaderVals = new ContentValues();
        	newHeaderVals.put(TrainUpdates.DATE_HEADER, 0);
        	db.update(UPDATES_TABLE_NAME, newHeaderVals, null, null);
        	
        	newHeaderVals.put(TrainUpdates.DATE_HEADER, 1);
//        	int numUpdated = 0;
        	SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        	builder.setTables(UPDATES_TABLE_NAME);
        	String headerQuery = "SELECT " + TrainUpdates.TWITTER_ID + " FROM " + UPDATES_TABLE_NAME + " WHERE " + TrainUpdates.DATE + " >= ? AND " + TrainUpdates.DATE + " < ? ORDER BY " + TrainUpdates.TWITTER_ID + " DESC LIMIT 1";//builder.buildQuery(new String[] {TrainUpdates.TWITTER_ID}, TrainUpdates.DATE + " >= ?", null, null, "ASC", "LMIT 1");
        	long lastId = -1;
        	long currId = -1;
        	
//        	do
        	for (int i = 0; i < 7; i++)
        	{
        		lastId = currId;
        		Cursor c = db.rawQuery(headerQuery, new String[] {dayStart.toString(), nextDayStart.toString()});
        		try
        		{
        			if (c.getCount() > 0)
        			{
		        		c.moveToFirst();
		        		
		        		currId = c.getLong(0);
		        		Log.i(TAG, "Found header: " + currId);
		        		db.update(UPDATES_TABLE_NAME, newHeaderVals, TrainUpdates.TWITTER_ID + " = " + currId, null);
        			}
        		}
        		finally
        		{
        			c.close();
        		}
        		
        		nextDayStart = dayStart;
        		dayStart = dayStart - SECONDS_PER_DAY; // Subtract one day.
        		
        		
        	}
//        	} while (lastId != currId);
        	
        	
        	//TODO: additional stuff here to update headers.
        	db.setTransactionSuccessful();
        }
        finally
        {
        	db.endTransaction();
        	inserter.close();
        }
        
        if (numRows > 0)
        {
        	// Let any observers know that changes were made.
        	getContext().getContentResolver().notifyChange(uri, null);
        }
                        
		return numRows;                
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate()
	{
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder)
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch(sUriMatcher.match(uri))
		{
			case UPDATE:
				qb.setTables(UPDATES_TABLE_NAME);
				break;
				
			case UPDATE_ID:
				qb.setTables(UPDATES_TABLE_NAME);
				break;
			
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;

		if (TextUtils.isEmpty(sortOrder))
		{
			orderBy = Constants.TrainUpdates.DEFAULT_SORT_ORDER;
		}
		else
		{
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
