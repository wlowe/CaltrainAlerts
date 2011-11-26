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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

/**
 * An admin servlet used for the OAuth setup with Twitter.
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
