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
import java.util.Random;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.lowetek.caltrainalerts.data.C2DMSettings;
import net.lowetek.caltrainalerts.data.PMF;
import net.lowetek.caltrainalerts.data.UpdateClient;

import com.google.android.c2dm.server.C2DMRetryServlet;
import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * An admin debug client to send messages to a client.
 * @author nopayne
 *
 */
@SuppressWarnings("serial")
public class PingClientServlet extends HttpServlet
{
	protected static final String REG_ID_PARM = "regId";
	protected static final String MESSAGE_PARM = "msg";
	private static final Logger log = Logger.getLogger(PingClientServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException
	{
		String regId = req.getParameter(REG_ID_PARM);
		
		if (regId == null)
		{
			resp.setStatus(400);
			resp.getOutputStream().write("No registration ID specified".getBytes());
			log.severe("No registration ID specified");
			return;
		}
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		try
		{
			// Check for existing clients with the same registration id.
			Query query = pm.newQuery(UpdateClient.class);
			query.setFilter("registrationId == '" + regId + "'");
			query.setRange(0, 1);
			@SuppressWarnings("unchecked")
			List<UpdateClient> existingClient = (List<UpdateClient>)query.execute();
			
			if (existingClient.isEmpty())
			{
				// The client couldn't be found.  Abort.
				log.severe("Client doesn't exist: " + regId);
				return;
			}	
		}
		finally
		{
			pm.close();
		}
		
		String message = req.getParameter(MESSAGE_PARM);
		
		if (message == null)
		{
			message = "Ping!";
		}
						
		long timeMillis = System.currentTimeMillis();
		String collapseStr = Long.toString(timeMillis / C2DMSettings.COLLAPSE_FACTOR);
		
		
		com.google.appengine.api.taskqueue.Queue dmQueue = QueueFactory.getQueue("c2dm");
		TaskOptions url = 
			TaskOptions.Builder.withUrl(C2DMRetryServlet.URI)
			.param(C2DMessaging.PARAM_REGISTRATION_ID, regId)
			.param(C2DMessaging.PARAM_COLLAPSE_KEY, collapseStr)
			.param("data.msgType", "ping")
			.param("data.message", message)
			.param("data.regId", regId);	      
        
        // Task queue implements the exponential backoff
		Random random = new Random();
        long jitter = random.nextInt(C2DMSettings.MAX_JITTER_MSEC);
        url.countdownMillis(jitter);
        
        dmQueue.add(url);
								
	}
}
