/**
 * 
 */
package com.lowetech.caltrainupdates;

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
public class TrainUpdate
{
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
		return "id: " + twitterId + " date: " + getPrettyDate() + " text: " + text;
	}
	
	public JSONObject getJSON()
	{
		JSONObject result = new JSONObject();
		try
		{
			result.put("text", text);
			result.put("twitterId", twitterId);
			result.put("date", getPrettyDate());
		}
		catch (JSONException e)
		{
			log.log(Level.WARNING, "Unable to create JSON object", e);
		}
		
		
		return result;
	}
	
	private String getPrettyDate()
	{
		return dateFormatter.format(date);
	}

}
