/**
 * 
 */
package com.lowetech.caltrainupdates.android;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lowetech.caltrainupdates.android.Constants.TrainUpdates;

/**
 * @author nopayne
 *
 */
public class UpdatesCursorAdapter extends SimpleCursorAdapter
{
	private static final DateFormat dateFormatter;
	private static final DateFormat timeFormatter;
	
	/**
	 * Keep a static Date object around so we don't have to create a new one in a loop.
	 */
	private static final Date rowDate = new Date();
	
	static
	{
		dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
		dateFormatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);
		timeFormatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
	}
	
	
	private static class UpdatesViewBinder implements SimpleCursorAdapter.ViewBinder
	{

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex)
		{
			if (view.getId() == R.id.updateText)
			{
				((TextView)view).setText(cursor.getString(columnIndex));				
			}
			else if (view.getId() == R.id.date)
			{
				rowDate.setTime(cursor.getLong(columnIndex) * 1000);
				((TextView)view).setText(timeFormatter.format(rowDate));
			}
			else if (view.getId() == R.id.dateHeader)
			{
				if (cursor.getInt(columnIndex) == 1)
				{
					Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
			        cal.set(Calendar.HOUR_OF_DAY, 0);
			        cal.set(Calendar.MINUTE, 0);
			        cal.set(Calendar.SECOND, 0);
			        long todayDateMillis = cal.getTimeInMillis();
			        cal.add(Calendar.DAY_OF_YEAR, 1);
			        long rowDateMillis = cursor.getLong(cursor.getColumnIndex(TrainUpdates.DATE)) * 1000;
			        long tomorrowDateMillis = cal.getTimeInMillis();
					rowDate.setTime(rowDateMillis);
					
					if (rowDateMillis >= todayDateMillis && rowDateMillis < tomorrowDateMillis)
					{
						((TextView)view).setText("Recent Updates");
					}
					else
					{					
						((TextView)view).setText(dateFormatter.format(rowDate));
						
					}
					
					view.setVisibility(View.VISIBLE);
				}
				else
				{
					view.setVisibility(View.GONE);
				}
				
			}

			
			return true;
		}
		
	}
	
	

	public UpdatesCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to)
	{
		super(context, layout, c, from, to);
		
		setViewBinder(new UpdatesViewBinder()); 				
		
	}
	
	

}
