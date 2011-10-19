package com.lowetek.caltrainupdates.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NotifyClientServlet extends HttpServlet 
{
	public static final String REG_ID_PARM = "reg_id";
	public static final String DATE_PARM = "date";
	public static final String COLLAPSE_KEY_PARM = "collapse_key";
	public static final String URI = "/tasks/notifyclient";
	
	private static final Logger log = Logger.getLogger(NotifyClientServlet.class.getName());
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException 
	{
		//System.out.println(req.getParameterMap());
		String regId = req.getParameter(REG_ID_PARM);
		log.fine("Notifying client: " + regId);
		
		resp.setStatus(200);
	}
}
