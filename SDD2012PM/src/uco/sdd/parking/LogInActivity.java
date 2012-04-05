package uco.sdd.parking;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import uco.sdd.utility.*;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LogInActivity extends Activity implements GetJSONArrayListener {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }
    
    public void logIn(View view) {
    	
    	EditText et_email = (EditText)this.findViewById(R.id.login_txt_email);
    	EditText et_password = (EditText)this.findViewById(R.id.login_txt_password);
    	
    	String email = et_email.getText().toString();
    	String password = et_password.getText().toString();
    	
    	HTTPDataAccess dac = new HTTPDataAccess(getString(R.string.url_select), this);
    	
    	dac.setStatement("select * from client where email = ? and password = ?");
    	dac.setTypes("ss");
    	dac.addNewBindVariable("email", email, false);
    	dac.addNewBindVariable("password", password, true);
    	dac.executeSelect();
    }
    
    public void onRemoteCallComplete(JSONArray jArray)
    {
    	
    }
}
