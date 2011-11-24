package net.lowetek.caltrainalerts.servlet;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lowetek.caltrainalerts.data.PMF;
import net.lowetek.caltrainalerts.data.TrainUpdate;


/**
 * 
 */

/**
 * @author nopayne
 *
 */
@SuppressWarnings("serial")
public class DeleteOldUpdatesServlet extends HttpServlet
{
	private static final Logger log = Logger.getLogger(DeleteOldUpdatesServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try
		{
			// TODO: This should really use the DataStorage class + clear the cache
			Query query = pm.newQuery(TrainUpdate.class);
			
			Calendar cal = Calendar.getInstance();
			log.info("Curr date" + cal.getTime().toString());
			cal.add(Calendar.DAY_OF_YEAR, -7);
			Date cutOff = cal.getTime();
			query.setFilter("date <= :date");
			
			log.info("Deleting updates older than: " + cutOff.toString());
			long numDeleted = query.deletePersistentAll(cutOff);			
			log.info("Deleted " + numDeleted + " stale updates");
		}
		finally
		{
			pm.close();
		}
		
	}
}
