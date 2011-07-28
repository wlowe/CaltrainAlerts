package com.lowetech.caltrainupdates;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class UpdatesServlet extends HttpServlet 
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException 
	{
		String sinceStr = req.getParameter("sinceId");
		long sinceId = -1;
		
		if (sinceStr != null)
		{
			sinceId = Long.parseLong(sinceStr);
		}
		
		resp.setContentType("application/json");
		String responseContent = TrainUpdatesStorage.getUpdatesSince(sinceId);
		resp.getWriter().println(responseContent);

	}
}
