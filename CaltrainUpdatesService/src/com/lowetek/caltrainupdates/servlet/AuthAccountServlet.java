/**
 * 
 */
package com.lowetek.caltrainupdates.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

/**
 * @author nopayne
 *
 */
public class AuthAccountServlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2184586432247960196L;

	protected void doGet(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp) throws javax.servlet.ServletException ,java.io.IOException 
	{
		
		try
		{
			Twitter twitter = new TwitterFactory().getInstance();
//			twitter.setOAuthConsumer(System.getProperty("twitter4j.oauth.consumerKey"), System.getProperty("twitter4j.oauth.consumerSecret"));
			req.getSession().setAttribute("twitter", twitter);	
			StringBuffer callbackUrl = req.getRequestURL();
			int slashIndex = callbackUrl.lastIndexOf("/");
			callbackUrl.replace(slashIndex, callbackUrl.length(), "/callback");
			
			RequestToken requestToken = twitter.getOAuthRequestToken(callbackUrl.toString());
			req.getSession().setAttribute("requestToken", requestToken);
			resp.sendRedirect(requestToken.getAuthenticationURL());
		}
		catch (TwitterException e)
		{
			throw new ServletException(e);
		}
		 
	}
	
	
	
}
