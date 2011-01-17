/**
 * 
 */
package com.lowetech.caltrainupdates;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * @author nopayne
 *
 */

@PersistenceCapable
public class TrainUpdate
{
	public TrainUpdate(long twitterId, String text, Date date)
	{
		this.twitterId = twitterId;
		this.text = text;
		this.date = date;
	}
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	@Persistent
	protected long twitterId;
	
	@Persistent
	protected String text;
	
	@Persistent
	protected Date date;
	
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
		return "id: " + twitterId + " date: " + date.toString() + " text: " + text;
	}

}
