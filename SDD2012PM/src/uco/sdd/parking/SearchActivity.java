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

public class SearchActivity extends Activity {

	private ArrayList<String> buildings;
	private ArrayList<String> parkingLots;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.search_layout);
	    
	    buildings = new ArrayList<String>();
	    parkingLots = new ArrayList<String>();
	    
	    HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new SearchBuildingJSONListener());
	    
	    dac.setStatement(getString(R.string.search_smt_building));
	    dac.setTypes(getString(R.string.search_smt_building_types));
    	dac.addNewBindVariable("One", "1", false);
    	
    	dac.executeSelect();
	}

	public void searchOnClick(View view) {
		
	}
	
	public void selectParkingLots(String building)
	{
		HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new SearchParkingLotJSONListener());
	    
	    dac.setStatement(getString(R.string.search_smt_parkinglots));
	    dac.setTypes(getString(R.string.search_smt_parkinglots_types));
    	dac.addNewBindVariable("building", building, false);
    	
    	dac.executeSelect();
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
				    	spinnerBuildings.setOnItemSelectedListener(new MyOnItemSelectedListener());
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
				    		parkingLots.add(json_data.getString("LOTID"));
				    	}
				    	
				    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
				    			R.layout.search_spinner_item, R.id.search_spn_itemtext, parkingLots);
				    	
				    	Spinner spinnerParkingLots = (Spinner)findViewById(R.id.search_spn_parkinglot);
				    	spinnerParkingLots.setAdapter(adapter);
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
	}
	
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id)
	    {
	    	String buildingValue = parent.getItemAtPosition(pos).toString();
	    	
	    	if (buildingValue.trim() != "")
	    	{
	    		selectParkingLots(buildingValue);
	    	}
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	      // Do nothing.
	    }
	}
}
