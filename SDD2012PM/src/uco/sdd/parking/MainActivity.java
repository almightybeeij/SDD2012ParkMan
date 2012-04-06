package uco.sdd.parking;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        String[] menuItems = getResources().getStringArray(R.array.main_mnu_items);
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main_list_item, menuItems));
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	
		super.onListItemClick(l, v, position, id);
		
		switch (position) {

			case 0:
				launchActivity("SearchActivity");
				break;
			case 1:
				launchActivity("SearchActivity");
				break;
			case 2:
				this.finish();
				break;
		}
	}
    
    private void launchActivity(String activityName) {
    	
    	try {
			Class<?> cl = Class.forName("uco.sdd.parking." + activityName);
			Intent intent = new Intent(this, cl);
			startActivity(intent);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
}