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





public class RegisterClientServlet extends HttpServlet 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String REG_ID_PARM = "regId";
	private static final Logger log = Logger.getLogger(RegisterClientServlet.class.getName());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException 
	{
		String regId = req.getParameter(REG_ID_PARM);
		
		if (regId == null)
		{
			resp.setStatus(400);
			resp.getOutputStream().write("No registration ID specified".getBytes());
			log.severe("No registration ID specified");
			return;
		}
		
		//TODO: sanitize regId here (empty string, etc)
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		try
		{
			// Check for existing clients with the same registration id.
			Query query = pm.newQuery(UpdateClient.class);
			query.setFilter("registrationId == '" + regId + "'");
			query.setRange(0, 1);
			List<UpdateClient> existingClient = (List<UpdateClient>)query.execute();
			
			if (existingClient.isEmpty())
			{
				// Create the new client.
				UpdateClient client = new UpdateClient(regId);
				pm.makePersistent(client);
				log.info("Client registered: " + regId);
			}
			else
			{
				log.info("Client already registered: " + regId);
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
