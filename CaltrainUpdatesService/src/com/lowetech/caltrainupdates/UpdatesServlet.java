package com.lowetech.caltrainupdates;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONObject;

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
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try
		{
			Query query = pm.newQuery(TrainUpdate.class);
			query.setOrdering("twitterId ASC");
			
			if (sinceId >= 0)
			{
				query.setFilter("twitterId > " + sinceId);
			}
			
			List<TrainUpdate> updates = (List<TrainUpdate>)query.execute();
			JSONArray resultArray = new JSONArray();
			
			for (TrainUpdate update : updates)
			{
				JSONObject updateJson = update.getJSON();
				resultArray.put(updateJson);
			}
			
			resp.setContentType("application/json");
			resp.getWriter().println(resultArray.toString());
		}
		finally
		{
			pm.close();
		}
	}
}
