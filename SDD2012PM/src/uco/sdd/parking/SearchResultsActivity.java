/*
 * SearchResultsActivity Class
 * 
 * Version 1.0.0
 * 
 * Author: Cory Wilson
 * Last Updated: 04/19/2012
 * Last Updated By: Cory Wilson
 * 
 * Represents the screen used to display the search results
 * returned from the SearchActivity class.  Allows the user
 * to select a single result or space and view it using the
 * ViewParkingMapActivity.
 */

package uco.sdd.parking;

import java.util.ArrayList;
import java.util.Set;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SearchResultsActivity extends ListActivity {

	private String lotId;
	private ArrayList<String> parkingSpaces;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.searchresults_layout);
	    
	    parkingSpaces = new ArrayList<String>();
	    
	    Set<String> keys = getIntent().getExtras().keySet();
	    Bundle extras = getIntent().getExtras();
	    
	    int count = 0;
	    for (String key : keys)
	    {
	    	count++;
	    	
	    	if (count == 1) {
	    		lotId = extras.getString(key);
	    	}
	    	else {
	    		parkingSpaces.add(extras.getString(key));
	    	}
	    }
	    
	    setListAdapter(new ArrayAdapter<String>(this, R.layout.searchresults_list_item,
	    	R.id.searchresults_parkingspace, parkingSpaces));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	
    	Intent i = null;
		super.onListItemClick(l, v, position, id);
				
		LinearLayout spaceLayout = (LinearLayout)v;
		TextView tvSpace = (TextView)spaceLayout.findViewById(R.id.searchresults_parkingspace);
		String space = tvSpace.getText().toString();
		String[] spaceElements = space.split(" ");
		
		i = new Intent(getApplicationContext(), ViewParkingMapActivity.class);
		i.putExtra("lotId", lotId);
		i.putExtra("spaceId", spaceElements[1]);
		
		startActivity(i);
		i = null;
	}
}
