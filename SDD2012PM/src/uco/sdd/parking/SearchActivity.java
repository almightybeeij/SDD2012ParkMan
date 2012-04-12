package uco.sdd.parking;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uco.sdd.utility.*;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchActivity extends Activity {

	private String parkingLot;
	private String building;
	
	private ArrayList<String> buildings;
	private ArrayList<String> parkingLots;
	private ArrayList<String> parkingTypes;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.search_layout);
	    
	    buildings = new ArrayList<String>();
	    parkingLots = new ArrayList<String>();
	    parkingTypes = new ArrayList<String>();
	    
	    Spinner spinnerParkingLots = (Spinner)findViewById(R.id.search_spn_parkinglot);
	    spinnerParkingLots.setEnabled(false);
	    
	    HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new SearchBuildingJSONListener());
	    
	    dac.setStatement(getString(R.string.search_smt_building));
	    dac.setTypes(getString(R.string.search_smt_building_types));
    	dac.addNewBindVariable("One", "1", false);
    	
    	dac.executeSelect();
	}

	public void searchOnClick(View view) {
		
		HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new SearchParkingJSONListener());
	    
	    dac.setStatement(getString(R.string.search_smt_parking));
	    dac.setTypes(getString(R.string.search_smt_parking_types));
    	dac.addNewBindVariable("lotId", parkingLot, false);
    	dac.addNewBindVariable("available", "1", false);
    	
    	dac.executeSelect();		
	}
	
	public void selectParkingTypes(String building)
	{
		HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new SearchParkingTypeJSONListener());
	    
	    dac.setStatement(getString(R.string.search_smt_parkingtypes));
	    dac.setTypes(getString(R.string.search_smt_parkingtypes_types));
    	dac.addNewBindVariable("building", building, false);
    	
    	dac.executeSelect();
	}
	
	public void selectParkingLots(String building, boolean studentLot, boolean facultyLot)
	{
		int isStudent = studentLot ? 1 : 0;
		int isFaculty = facultyLot ? 1 : 0;
		
		HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new SearchParkingLotJSONListener());
	    
	    dac.setStatement(getString(R.string.search_smt_parkinglots));
	    dac.setTypes(getString(R.string.search_smt_parkinglots_types));
    	dac.addNewBindVariable("building", building, false);
    	dac.addNewBindVariable("studentLot", Integer.toString(isStudent), false);
    	dac.addNewBindVariable("facultyLot", Integer.toString(isFaculty), false);
    	
    	dac.executeSelect();
	}
	
	private void clearParkingLots()
	{
		parkingLots.clear();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
    			R.layout.search_spinner_item, R.id.search_spn_itemtext, parkingLots);
    	
    	Spinner spinnerParkingLots = (Spinner)findViewById(R.id.search_spn_parkinglot);
    	spinnerParkingLots.setAdapter(adapter);
    	spinnerParkingLots.setEnabled(false);
	}
	
	private class SearchBuildingJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			    	
	    	try
	    	{
	    		if (jArray != null)
	    		{
	    			if (jArray.length() > 0)
	    			{
	    				buildings.add("");
				    	for(int index = 0; index < jArray.length(); index++)
				    	{
				    		JSONObject json_data = jArray.getJSONObject(index);
				    		buildings.add(json_data.getString("name"));
				    	}
				    	
				    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
				    			R.layout.search_spinner_item, R.id.search_spn_itemtext, buildings);
				    	
				    	Spinner spinnerBuildings = (Spinner)findViewById(R.id.search_spn_building);
				    	spinnerBuildings.setAdapter(adapter);
				    	spinnerBuildings.setOnItemSelectedListener(new OnBuildingItemSelectedListener());
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
	}
	
	private class SearchParkingTypeJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			 
			try
	    	{
	    		if (jArray != null)
	    		{
	    			if (jArray.length() > 0)
	    			{
				    	for(int index = 0; index < jArray.length(); index++)
				    	{
				    		JSONObject json_data = jArray.getJSONObject(index);
				    		parkingTypes.add("");
				    		
				    		if (json_data.getInt("studentLots") > 0)
				    		{
				    			parkingTypes.add("Student");
				    		}
				    		if (json_data.getInt("facultyLots") > 0)
				    		{
				    			parkingTypes.add("Faculty");
				    		}
				    	}
				    	
				    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
				    			R.layout.search_spinner_item, R.id.search_spn_itemtext, parkingTypes);
				    	
				    	Spinner spinnerParkingTypes = (Spinner)findViewById(R.id.search_spn_parkingtype);
				    	spinnerParkingTypes.setAdapter(adapter);
				    	spinnerParkingTypes.setOnItemSelectedListener(new OnParkingTypeItemSelectedListener());
				    	spinnerParkingTypes.setEnabled(true);
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
	}
	
	private class SearchParkingLotJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			 
			try
	    	{
	    		if (jArray != null)
	    		{
	    			if (jArray.length() > 0)
	    			{
				    	for(int index = 0; index < jArray.length(); index++)
				    	{
				    		JSONObject json_data = jArray.getJSONObject(index);
				    		parkingLots.add("Parking Lot " + json_data.getString("lotid"));
				    	}
				    	
				    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
				    			R.layout.search_spinner_item, R.id.search_spn_itemtext, parkingLots);
				    	
				    	Spinner spinnerParkingLots = (Spinner)findViewById(R.id.search_spn_parkinglot);
				    	spinnerParkingLots.setAdapter(adapter);
				    	spinnerParkingLots.setOnItemSelectedListener(new OnParkingLotItemSelectedListener());
				    	spinnerParkingLots.setEnabled(true);
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
	}
	
	private class SearchParkingJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			    	
	    	try
	    	{
	    		if (jArray != null)
	    		{
	    			if (jArray.length() > 0)
	    			{
	    				String parkingAvailable = null;
	    				
				    	for(int index = 0; index < jArray.length(); index++)
				    	{
				    		JSONObject json_data = jArray.getJSONObject(index);
				    		parkingAvailable = parkingAvailable + json_data.getString("spaceId")+ " ";
				    	}
				    	TextView tvTest = (TextView)findViewById(R.id.search_txt_error);
				    	tvTest.setText(parkingAvailable);
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
	}
	
	public class OnBuildingItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id)
	    {
	    	String buildingValue = parent.getItemAtPosition(pos).toString();
	    	
	    	if (buildingValue.trim() != "")
	    	{
	    		building = buildingValue;
	    		selectParkingTypes(buildingValue);
	    	}
	    	else
	    	{
	    		clearParkingLots();
	    	}
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	      // Do nothing.
	    }
	}
	
	public class OnParkingTypeItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id)
	    {
	    	String parkingTypeValue = parent.getItemAtPosition(pos).toString();
	    	
	    	if (parkingTypeValue.trim() != "")
	    	{
	    		if (parkingTypeValue == "Student")
	    			selectParkingLots(building, true, false);
	    		else
	    			selectParkingLots(building, false, true);
	    	}
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	      // Do nothing.
	    }
	}
	
	public class OnParkingLotItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id)
	    {
	    	String parkingLotValue = parent.getItemAtPosition(pos).toString();
	    	
	    	if (parkingLotValue.trim() != "")
	    	{
	    		parkingLot = parkingLotValue.substring(12,
	    				parkingLotValue.length());
	    	}
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	      // Do nothing.
	    }
	}
}
