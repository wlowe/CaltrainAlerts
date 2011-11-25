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

package net.lowetek.caltrainalerts.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

/**
 * @author nopayne
 *
 */

@PersistenceCapable
public class TrainUpdate implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	@Persistent
	protected long twitterId;
	
	@Persistent
	protected String text;
	
	@Persistent
	protected Date date;
	
	private static final DateFormat dateFormatter;
	private static final Logger log = Logger.getLogger(TrainUpdate.class.getName());
	
	static
	{
		dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
	}
	
	
	public TrainUpdate(long twitterId, String text, Date date)
	{
		this.twitterId = twitterId;
		this.text = text;
		this.date = date;
	}
	
	public long getTwitterId()
	{
		return twitterId;
	}

	public String getText()
	{
		return text;
	}

	public Date getDate()
	{
		return date;
	}
	
	@Override
	public String toString()
	{
		return "id: " + twitterId + " date: " + getPrettyDate(date) + " text: " + text;
	}
	
	public JSONObject getJSON()
	{
		JSONObject result = new JSONObject();
		try
		{
			result.put("text", text);
			result.put("twitterId", twitterId);
			result.put("date", date.getTime() / 1000); // Get the UNIX time
		}
		catch (JSONException e)
		{
			log.log(Level.WARNING, "Unable to create JSON object", e);
		}
		
		
		return result;
	}
	
	private static String getPrettyDate(Date date)
	{
		synchronized(dateFormatter)
		{
			return dateFormatter.format(date);
		}
	}

}
