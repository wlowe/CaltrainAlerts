package com.lowetech.caltrainupdates;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PingClientServlet extends HttpServlet
{
	protected static final String REG_ID_PARM = "regId";
	private static final Logger log = Logger.getLogger(PingClientServlet.class.getName());
	private static final String C2DM_URL = "https://android.apis.google.com/c2dm/send";
	
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
		
		
	}
}
