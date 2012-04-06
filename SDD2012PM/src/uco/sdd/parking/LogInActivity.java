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
        setContentView(R.layout.login);
        ((ParkingApplication)getApplication()).setResultCloseAll(0);
    }
    
    public void logIn(View view) {
    	
    	EditText et_email = (EditText)this.findViewById(R.id.login_txt_email);
    	EditText et_password = (EditText)this.findViewById(R.id.login_txt_password);
    	
    	String email = et_email.getText().toString();
    	String password = et_password.getText().toString();
    	
    	HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new LogInJSONArrayListener());
    	
    	dac.setStatement(getString(R.string.login_smt_authorize));
    	dac.setTypes(getString(R.string.login_smt_authorize_types));
    	dac.addNewBindVariable("email", email, false);
    	dac.addNewBindVariable("password", password, true);
    	
    	dac.executeSelect();
    }
    
    private class LogInJSONArrayListener implements GetJSONArrayListener {
    	
	    public void onRemoteCallComplete(JSONArray jArray) {
	    	
	    	try
	    	{	
	    		TextView tv_test = (TextView)findViewById(R.id.login_txt_test);
	    		
	    		if (jArray != null)
	    		{
	    			if (jArray.length() > 0)
	    			{
				    	for(int index = 0; index < jArray.length(); index++)
				    	{	
				        	JSONObject json_data = jArray.getJSONObject(index);
				        	
				        	String firstName = json_data.getString("firstName");
				        	String lastName = json_data.getString("lastName");
				        	
				        	tv_test.setText("Welcome back, " + firstName + " " + lastName + "!");
				        	
				        	Intent i = new Intent(getApplicationContext(), MainActivity.class);
					    	startActivityForResult(i, ((ParkingApplication)getApplication()).getResultCloseAll());
					    	i = null;
				    	}
	    			}
	    			else
		    		{
		    			tv_test.setText("Nice try. Better luck next time!");
		    		}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
	    }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	int closeAll = ((ParkingApplication)getApplication()).getResultCloseAll();
    	
        if (resultCode == closeAll)
        {
        	setResult(closeAll);
        	finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
