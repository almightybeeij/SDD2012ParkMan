package uco.sdd.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class HTTPDataAccess {
	
	private String url;
	private String statement;
	private String types;
	
	private ArrayList<NameValuePair> bindVariables;
	
	private GetJSONArrayListener getJSONListener;
	private ProgressDialog progressDialog;
	private Context currentContext;
	
	public HTTPDataAccess(String url, GetJSONArrayListener listener) {
		
		this.url = url;
		this.getJSONListener = listener;
		this.bindVariables = new ArrayList<NameValuePair>();
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	public ArrayList<NameValuePair> getBindVariables() {
		return bindVariables;
	}

	public void setBindVariables(ArrayList<NameValuePair> bindVariables) {
		this.bindVariables = bindVariables;
	}

	public void addNewBindVariable(String name, String value, boolean isHashed) {
		
		try {
			if (isHashed) {
				bindVariables.add(new BasicNameValuePair(name, this.computeHash(value)));
			}
			else {
				bindVariables.add(new BasicNameValuePair(name, value));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void clearBindVariables()
	{
		this.bindVariables.clear();
	}
	
	public void executeSelect()
	{
		new ExecuteSelectTask().execute(this.url);
	}
	
	public class ExecuteSelectTask extends AsyncTask<String, Void, JSONArray>
	{
		protected JSONArray doInBackground(String... urls)
		{
			return executeSelect(urls[0]);
		}
		
		private JSONArray executeSelect(String url)
		{
			HttpClient httpclient = new DefaultHttpClient();
    	    HttpPost httppost = new HttpPost(url);
    	    
			try
			{
    	    	ArrayList<NameValuePair> requestVariables = buildRequestVariables();
    	    	httppost.setEntity(new UrlEncodedFormEntity(requestVariables));
    	    	
    	    	HttpResponse response = httpclient.execute(httppost);
        	    HttpEntity entity = response.getEntity();
        	    
        	    if (entity != null)
        	    {
        	    	InputStream istream = entity.getContent();
        	    	String result = convertStreamToString(istream);
        	    	
        	    	JSONArray jArray = new JSONArray(result);
        	    	
        	    	return jArray;
        	    }				
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
	    public void onPreExecute()
		{
	        progressDialog = new ProgressDialog(currentContext);
	        progressDialog.setMessage("Loading..Please wait..");
	        progressDialog.setCancelable(false);
	        progressDialog.setIndeterminate(true);
	        progressDialog.show();
	    }
		
		protected void onPostExecute(JSONArray jArray)
		{
			getJSONListener.onRemoteCallComplete(jArray);
		}
	}
	
	private static String convertStreamToString(InputStream is)
	{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
	
	public ArrayList<NameValuePair> buildRequestVariables() {
		
		String encrypted;
		MCrypt mcrypt;
		ArrayList<NameValuePair> requestVariables = new ArrayList<NameValuePair>();
		
		try
		{
			mcrypt = new MCrypt();
			encrypted = MCrypt.bytesToHex(mcrypt.encrypt(""));
			
			requestVariables.add(new BasicNameValuePair("stmt", encrypted));
			requestVariables.add(new BasicNameValuePair("types", types));
			
			for (int index = 0; index < bindVariables.size(); index++)
			{
				requestVariables.add(bindVariables.get(index));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return requestVariables;
	}
	
	public String computeHash(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.reset();

        byte[] byteData = digest.digest(input.getBytes("UTF-8"));
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < byteData.length; i++)
        {
        	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}

