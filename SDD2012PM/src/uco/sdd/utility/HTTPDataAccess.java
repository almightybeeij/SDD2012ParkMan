package uco.sdd.utility;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class HTTPDataAccess {
	
	private String url;
	
	private BasicNameValuePair statement;
	private BasicNameValuePair types;
	private ArrayList<NameValuePair> bindVariables;
	private ArrayList<NameValuePair> requestVariables;
	
	public HTTPDataAccess(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public BasicNameValuePair getStatement() {
		return statement;
	}

	public void setStatement(BasicNameValuePair statement) {
		this.statement = statement;
	}

	public BasicNameValuePair getTypes() {
		return types;
	}

	public void setTypes(BasicNameValuePair types) {
		this.types = types;
	}

	public ArrayList<NameValuePair> getBindVariables() {
		return bindVariables;
	}

	public void setBindVariables(ArrayList<NameValuePair> bindVariables) {
		this.bindVariables = bindVariables;
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

