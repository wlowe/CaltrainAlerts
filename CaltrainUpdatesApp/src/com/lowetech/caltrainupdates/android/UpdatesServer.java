package com.lowetech.caltrainupdates.android;

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

public class UpdatesServer
{
	private static final String TAG = UpdatesServer.class.getSimpleName();
	private static AndroidHttpClient httpClient = AndroidHttpClient.newInstance("CaltrainUpdates");
	private static final String SERVER;
	
	static
	{
		SERVER = Constants.getUpdatesServerUrl();
	}
		
	
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

	
	
}
