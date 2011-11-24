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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import net.lowetek.caltrainalerts.android.Constants.TrainUpdates;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import net.lowetek.caltrainalerts.android.R;

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
