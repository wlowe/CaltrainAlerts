/**
 * 
 */
package com.lowetek.caltrainupdates.data;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import twitter4j.auth.AccessToken;

import com.google.appengine.api.datastore.Key;

/**
 * @author nopayne
 *
 */

@PersistenceCapable
public class StoredAccessToken implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	@Persistent
	protected String token;
	
	@Persistent
	protected String tokenSecret;
	
	public StoredAccessToken(AccessToken accessToken)
	{
		token = accessToken.getToken();
		tokenSecret = accessToken.getTokenSecret();
	}
	
	public AccessToken getAccessToken()
	{
		return new AccessToken(token, tokenSecret);
	}
}
