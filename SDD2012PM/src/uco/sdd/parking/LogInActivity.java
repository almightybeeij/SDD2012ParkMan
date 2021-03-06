/*
 * LogInActivity Class
 * 
 * Version 1.0.0
 * 
 * Author: Cory Wilson
 * Last Updated: 04/19/2012
 * Last Updated By: Cory Wilson
 * 
 * Represents the login screen for the parking management system.
 * This screen is the first to be displayed when the application
 * is started.  Accepts the user's e-mail account and password
 * as input and allows or disallows the user entry to the remaining
 * sections of the application.
 */

package uco.sdd.parking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uco.sdd.utility.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LogInActivity extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        
        this.setTitle(getResources().getString(R.string.app_name));
        ((ParkingApplication)getApplication()).setResultCloseAll(0);
        ((ParkingApplication)getApplication()).setResultClose(2);
        ((ParkingApplication)getApplication()).setResultUpdate(5);
    }
    
    public void logInOnClick(View view) {
    	
    	TextView tvError = (TextView)findViewById(R.id.login_txt_error);
    	EditText etEmail = (EditText)this.findViewById(R.id.login_txt_email);
    	EditText etPassword = (EditText)this.findViewById(R.id.login_txt_password);
    	
    	String email = etEmail.getText().toString();
    	String password = etPassword.getText().toString();
    	
    	if ((email.trim().length() == 0) || (password.trim().length() == 0)) {
    		
    		tvError.setText(getString(R.string.login_msg_required));
    		return;
    	}
    	
    	HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new LogInJSONArrayListener());
    	
    	dac.setStatement(getString(R.string.login_smt_authorize));
    	dac.setTypes(getString(R.string.login_smt_authorize_types));
    	dac.addNewBindVariable("email", email, false);
    	dac.addNewBindVariable("password", password, true);
    	
    	dac.executeSelect();
    }
    
    private class LogInJSONArrayListener implements GetJSONListener {
    	
	    public void onRemoteCallComplete(JSONArray jArray) {
	    	
	    	try	{
	    		
	    		TextView tv_test = (TextView)findViewById(R.id.login_txt_error);
	    		
	    		if (jArray != null) {
	    			
	    			if (jArray.length() > 0) {
	    				
				    	for(int index = 0; index < jArray.length(); index++) {
				    		
				        	JSONObject json_data = jArray.getJSONObject(index);
				        	
				        	String email = json_data.getString("email");
				        	String firstName = json_data.getString("firstName");
				        	String lastName = json_data.getString("lastName");
				        	
				        	Intent i = new Intent(getApplicationContext(), MainActivity.class);
					    	
					    	((ParkingApplication)getApplication()).setUserEmail(email);
					    	((ParkingApplication)getApplication()).setUserFirstName(firstName);
					    	((ParkingApplication)getApplication()).setUserLastName(lastName);
					    	
					    	startActivityForResult(i, ((ParkingApplication)getApplication()).getResultCloseAll());
					    	
					    	i = null;
				    	}
	    			}
	    			else
		    		{
		    			tv_test.setText(getString(R.string.login_msg_invalid));
		    		}
	    		}
	    	} catch (JSONException e) {
	    		
	    		e.printStackTrace();
	    	}
	    }
	    
	    public void onRemoteCallComplete(JSONObject jObject) {}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	int closeAll = ((ParkingApplication)getApplication()).getResultCloseAll();
    	int close = ((ParkingApplication)getApplication()).getResultClose();
    	
        if (resultCode == closeAll) {
        	
        	setResult(closeAll);
        	finish();
        }
        else if (resultCode == close) {
        	
        	setResult(close);
        	
        	EditText etEmail = (EditText)this.findViewById(R.id.login_txt_email);
        	EditText etPassword = (EditText)this.findViewById(R.id.login_txt_password);
        	
        	etEmail.setText("");
        	etPassword.setText("");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
