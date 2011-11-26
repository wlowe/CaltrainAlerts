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

/**
 * Updates the registration of a client with this app server.
 * This is needed because sometimes the C2DM servers don't notify us that
 * a client has a received a new registration.  Yet, it will still forward messages
 * from the old registration to the client.  The end result is that the
 * client will receive duplicate messages (one for the old reg, one for the new).
 * To fix this, the client should call this servlet.
 * @author nopayne
 *
 */
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
			@SuppressWarnings("unchecked")
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
			@SuppressWarnings("unchecked")
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
