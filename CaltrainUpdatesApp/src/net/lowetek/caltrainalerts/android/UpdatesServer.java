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

package net.lowetek.caltrainalerts.android;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.net.http.AndroidHttpClient;
import android.util.Log;

/**
 * The interface for all communication with the remote app server.
 * @author nopayne
 *
 */
public class UpdatesServer
{
	private static final String TAG = UpdatesServer.class.getSimpleName();
	private static AndroidHttpClient httpClient = AndroidHttpClient.newInstance("CaltrainUpdates");
	private static final String SERVER;
	
	static
	{
		SERVER = Constants.getUpdatesServerUrl();
	}
		
	/**
	 * Fetch all new updates from the server.
	 * @param sinceId
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONArray fetchUpdates(String sinceId) throws IOException, JSONException
	{
		
		ArrayList<NameValuePair> qparams = new ArrayList<NameValuePair>();
		
		if (sinceId != null)
		{
			qparams.add(new BasicNameValuePair("sinceId", sinceId));
		}
		
		URI reqUri = null;

		try
		{
			reqUri = URIUtils.createURI("http", SERVER, -1, "/updates", URLEncodedUtils.format(qparams, "UTF-8"), null);
		}
		catch (URISyntaxException e)
		{
			Log.e(TAG, "Malformed URI", e);
			return null;//TODO:
		}
		
		HttpGet req = new HttpGet(reqUri);
		
		ResponseHandler<String> handler = new ResponseHandler<String>() 
		{
		    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException 
		    {
		        HttpEntity entity = response.getEntity();
		    
		        if (entity != null) 
		        {
		        	return EntityUtils.toString(entity);		            
		        }
		        else 
		        {
		            return null;
		        }
		    }
		};
		
		String content = httpClient.execute(req, handler);
		Log.i(TAG, content);
		return new JSONArray(content);		
	}
	
	/**
	 * Register a new client with the app server.
	 * @param regId
	 * @throws IOException
	 */
	public static void registerClient(String regId) throws IOException
	{
		if (regId == null)
		{
			throw new IllegalArgumentException("Must specify a regId");
		}
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("regId", regId));
	
		URI postUri = null;
		
		try
		{
			postUri = URIUtils.createURI("https", SERVER, -1, "/register", URLEncodedUtils.format(params, "UTF-8"), null);
		}
		catch (URISyntaxException ex)		
		{
			throw new IOException("Unable to build request URI");
		}
		
		HttpPost post = new HttpPost(postUri);
		HttpResponse response = httpClient.execute(post);
		StatusLine status = response.getStatusLine();
		
		if (status.getStatusCode() != 200)
		{
			throw new HttpResponseException(status.getStatusCode(), status.getReasonPhrase());
		}
	}

//	public static void unRegisterClient(String regId) throws IOException
//	{
//		if (regId == null)
//		{
//			throw new IllegalArgumentException("Must specify a regId");
//		}
//		
//		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//		
//		params.add(new BasicNameValuePair("regId", regId));
//	
//		URI postUri = null;
//		
//		try
//		{
//			postUri = URIUtils.createURI("https", SERVER, -1, "/unregister", URLEncodedUtils.format(params, "UTF-8"), null);
//		}
//		catch (URISyntaxException ex)		
//		{
//			throw new IOException("Unable to build request URI");
//		}
//		
//		HttpPost post = new HttpPost(postUri);
//		HttpResponse response = httpClient.execute(post);
//		StatusLine status = response.getStatusLine();
//		
//		if (status.getStatusCode() != 200)
//		{
//			throw new HttpResponseException(status.getStatusCode(), status.getReasonPhrase());
//		}
//	}
	
	/**
	 * Reregister with the app server.  
	 * This removes the old registeration and adds the new one if needed.
	 * @param oldRegId
	 * @param newRegId
	 * @throws IOException
	 */
	public static void reRegisterClient(String oldRegId, String newRegId) throws IOException
	{
		if (oldRegId == null || newRegId == null)
		{
			throw new IllegalArgumentException("Must specify old and new regIds");
		}
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("oldRegId", oldRegId));
		params.add(new BasicNameValuePair("newRegId", newRegId));
	
		URI postUri = null;
		
		try
		{
			postUri = URIUtils.createURI("https", SERVER, -1, "/reregister", URLEncodedUtils.format(params, "UTF-8"), null);
		}
		catch (URISyntaxException ex)		
		{
			throw new IOException("Unable to build request URI");
		}
		
		HttpPost post = new HttpPost(postUri);
		HttpResponse response = httpClient.execute(post);
		StatusLine status = response.getStatusLine();
		
		if (status.getStatusCode() != 200)
		{
			throw new HttpResponseException(status.getStatusCode(), status.getReasonPhrase());
		}	
	}
}
