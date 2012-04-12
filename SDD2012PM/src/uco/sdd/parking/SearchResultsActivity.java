package uco.sdd.parking;

import java.util.ArrayList;
import java.util.Set;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

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
	    	
	    	if (count == 1)
	    		lotId = extras.getString(key);
	    	else
	    		parkingSpaces.add(extras.getString(key));
	    }
	    
	    setListAdapter(new ArrayAdapter<String>(this, R.layout.searchresults_list_item,
	    		R.id.searchresults_parkingspace, parkingSpaces));
	}

}
