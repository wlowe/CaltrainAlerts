/**
 * 
 */
package com.lowetek.caltrainupdates.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.lowetek.caltrainupdates.data.DataStorage;

/**
 * @author nopayne
 * 
 */
public class CallbackServlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7096882550829519704L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException
	{
		Twitter twitter = (Twitter) req.getSession().getAttribute("twitter");
		RequestToken requestToken = (RequestToken) req.getSession().getAttribute("requestToken");
		String verifier = req.getParameter("oauth_verifier");

		try
		{
			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
			DataStorage.setAccessToken(accessToken);
			req.getSession().removeAttribute("requestToken");
		}
		catch (TwitterException e)
		{
			throw new ServletException(e);
		}
		catch (DatastoreTimeoutException e)
		{
			throw new ServletException("Unable to set access token.  Please retry", e);
		}
		
		resp.sendRedirect(req.getContextPath() + "/");
	}
}
