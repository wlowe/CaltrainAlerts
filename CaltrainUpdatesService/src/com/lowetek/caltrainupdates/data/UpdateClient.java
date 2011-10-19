package com.lowetek.caltrainupdates.data;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class UpdateClient
{
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

	@Persistent
	private String registrationId;
	
	@Persistent
	private Date lastSeen;
	
	@Persistent
	private int numConnectionFailures;
	
	public UpdateClient(String registrationId)
	{
		this.registrationId = registrationId;
	}

	public String getRegistrationId() {
		return registrationId;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public int getNumConnectionFailures() {
		return numConnectionFailures;
	}

	public void setNumConnectionFailures(int numConnectionFailures) {
		this.numConnectionFailures = numConnectionFailures;
	}

	
}
