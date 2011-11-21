package com.lowetek.caltrainupdates.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.c2dm.server.C2DMRetryServlet;
import com.google.android.c2dm.server.C2DMessaging;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.lowetek.caltrainupdates.data.C2DMSettings;

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
		
		String message = req.getParameter(MESSAGE_PARM);
		
		if (message == null)
		{
			message = "Ping!";
		}
						
		long timeMillis = System.currentTimeMillis();
		//String timeStr = timeMillis + "";
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
        long jitter = (int) Math.random() * C2DMSettings.MAX_JITTER_MSEC;
        url.countdownMillis(jitter);
        
        dmQueue.add(url);
								
	}
}
