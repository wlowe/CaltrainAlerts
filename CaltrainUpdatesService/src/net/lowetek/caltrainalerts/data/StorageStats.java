/**
 * 
 */
package net.lowetek.caltrainalerts.data;

import java.io.Serializable;

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
public class StorageStats implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	@Persistent
	protected long latestUpdateId;
	
	public StorageStats()
	{
		latestUpdateId = -1;
	}

	public long getLatestUpdateId()
	{
		return latestUpdateId;
	}

	public void setLatestUpdateId(long latestUpdateId)
	{
		this.latestUpdateId = latestUpdateId;
	}
}
