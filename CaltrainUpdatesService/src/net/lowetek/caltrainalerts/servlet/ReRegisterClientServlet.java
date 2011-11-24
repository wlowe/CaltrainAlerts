package net.lowetek.caltrainalerts.servlet;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lowetek.caltrainalerts.data.PMF;
import net.lowetek.caltrainalerts.data.UpdateClient;





public class ReRegisterClientServlet extends HttpServlet 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String OLD_REG_ID_PARM = "oldRegId";
	protected static final String NEW_REG_ID_PARM = "newRegId";
	private static final Logger log = Logger.getLogger(ReRegisterClientServlet.class.getName());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException 
	{
		String oldRegId = req.getParameter(OLD_REG_ID_PARM);
		String newRegId = req.getParameter(NEW_REG_ID_PARM);
		
		if (oldRegId == null)
		{
			resp.setStatus(400);
			resp.getOutputStream().write("No old registration ID specified".getBytes());
			log.severe("No old registration ID specified");
			return;
		}
		
		if (newRegId == null)
		{
			resp.setStatus(400);
			resp.getOutputStream().write("No new registration ID specified".getBytes());
			log.severe("No new registration ID specified");
			return;
		}
		
		//TODO: sanitize regId here (empty string, etc)
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		try
		{
			// Check for existing clients with the same registration id.
			Query query = pm.newQuery(UpdateClient.class);
			query.setFilter("registrationId == '" + oldRegId + "'");
			query.setRange(0, 1);
			List<UpdateClient> existingClient = (List<UpdateClient>)query.execute();
			
			if (existingClient.isEmpty())
			{
				log.info("Old client not found: " + oldRegId);
				return;
			}
			else
			{
				pm.deletePersistentAll(existingClient);				
			}
			
			query = pm.newQuery(UpdateClient.class);
			query.setFilter("registrationId == '" + newRegId + "'");
			query.setRange(0, 1);
			List<UpdateClient> newClient = (List<UpdateClient>)query.execute();
				
			if(newClient.isEmpty())
			{
				// Create the new client.
				UpdateClient client = new UpdateClient(newRegId);
				pm.makePersistent(client);
				log.info("Client registered: " + newRegId);
			}
			else
			{
				log.info("Client already registered");
			}
			
			resp.setStatus(200);
			resp.getOutputStream().write("OK".getBytes());
			
		}
		finally
		{
			pm.close();
		}
	}
}
