package com.lowetek.caltrainupdates.servlet;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lowetek.caltrainupdates.data.PMF;
import com.lowetek.caltrainupdates.data.TrainUpdate;

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
			
			Query query = pm.newQuery(TrainUpdate.class);
			
			Calendar cal = Calendar.getInstance();
			log.info("Curr date" + cal.getTime().toString());
			cal.add(Calendar.DAY_OF_YEAR, -1);
			Date cutOff = cal.getTime();
			query.setFilter("date <= :date");
			
			log.info("Deleting older than: " + cutOff.toString());
			List<TrainUpdate> deletedUpdates = (List<TrainUpdate>)query.execute(cutOff);
			long numDeleted = deletedUpdates.size();
			
			for (TrainUpdate update : deletedUpdates)
			{
				log.info("Will delete" + update.toString());
			}
			
			//TODO: delete using this method
			//long numDeleted = query.deletePersistentAll(cutOff);
			
			log.info("Deleted " + numDeleted + " stale updates");
			
			pm.deletePersistentAll(deletedUpdates);
		}
		finally
		{
			pm.close();
		}
		
	}
}
