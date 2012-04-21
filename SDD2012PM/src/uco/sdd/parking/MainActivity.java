/*
 * MainActivity Class
 * 
 * Version 1.0.0
 * 
 * Author: Cory Wilson
 * Last Updated: 04/19/2012
 * Last Updated By: Cory Wilson
 * 
 * Represents the main menu screen for the parking management system.
 * This screen is displayed immediately following the login screen, 
 * after the user has successfully authenticated against the system.
 * Contains menu options allowing the user access to the primary
 * functions of the application.
 */

package uco.sdd.parking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uco.sdd.utility.GetJSONListener;
import uco.sdd.utility.HTTPDataAccess;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity {
	
	private String clientEmail;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        
        String[] menuItems = getResources().getStringArray(R.array.main_mnu_items);
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main_list_item, menuItems));
        
        clientEmail = ((ParkingApplication)getApplication()).getUserEmail();
    	String fName = ((ParkingApplication)getApplication()).getUserFirstName();
    	String lName = ((ParkingApplication)getApplication()).getUserLastName();
    
    	TextView tv_Welcome = (TextView)findViewById(R.id.main_tvw_welcome);
    	tv_Welcome.setText("Welcome Back " + fName + " " + lName + "!");
        
        if (clientEmail != null) {
        	
        	selectCurrentStatus(clientEmail);
        }
    }
    
	private void selectCurrentStatus(String email) {
		
		HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new CheckedInJSONArrayListener());
    	
    	dac.setStatement(getString(R.string.main_smt_checkedin));
    	dac.setTypes(getString(R.string.main_smt_checkedin_types));
    	dac.addNewBindVariable("email", email, false);
    	dac.setUsingProgress(false);
    	
    	dac.executeSelect();
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	int closeAll = ((ParkingApplication)getApplication()).getResultCloseAll();
    	int update = ((ParkingApplication)getApplication()).getResultUpdate();
    	
        if (resultCode == closeAll) {
        	
        	setResult(closeAll);
        	finish();
        }
        else if (resultCode == update) {
        	
        	selectCurrentStatus(clientEmail);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	Intent i = null;
		super.onListItemClick(l, v, position, id);
		
		switch (position) {

			case 0:
				i = new Intent(getApplicationContext(), ViewParkingMapActivity.class);
				startActivityForResult(i, ((ParkingApplication)getApplication()).getResultUpdate());
				i = null;
				break;
			case 1:
				i = new Intent(getApplicationContext(), SearchActivity.class);
				startActivity(i);
				i = null;
				break;
			case 2:
				this.setResult(((ParkingApplication)getApplication()).getResultClose());
				this.finish();
				break;
			case 3:
				this.setResult(((ParkingApplication)getApplication()).getResultCloseAll());
				this.finish();
				break;
		}
	}
    
    public class CheckedInJSONArrayListener implements GetJSONListener {
    	
	    public void onRemoteCallComplete(JSONArray jArray) {
	    	
	    	try	{
	    		
	    		TextView tv_checkedIn = (TextView)findViewById(R.id.main_tvw_checkedIn);
	    		
	    		if (jArray != null) {
	    			
	    			if (jArray.length() > 0) {
	    				
	    				JSONObject json_data = jArray.getJSONObject(0);
	    				
	    				String spaceId = json_data.getString("spaceid");
	    				String lotId = json_data.getString("parkinglot_lotid");
	    				
	    				boolean isStudent = ((ParkingApplication)getApplication()).BooleanFromInt(json_data.getInt("studentlot"));
	    				
	    				String type = isStudent ? "student" : "faculty";
	    				
	    				tv_checkedIn.setText("You are currently checked into space\n" +
	    					spaceId + " located in " + type + " lot " + lotId + ".");
	    			}
	    			else {
		    			
		    			tv_checkedIn.setText("You are not currently checked\ninto a parking space.");
		    		}
	    		}
	    		
	    	} catch (JSONException e) {
	    		
	    		e.printStackTrace();
	    	}
	    }
	    
	    public void onRemoteCallComplete(JSONObject jObject) {}
    }
}