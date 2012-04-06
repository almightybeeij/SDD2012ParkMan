package uco.sdd.parking;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity
{
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        String[] menuItems = getResources().getStringArray(R.array.main_mnu_items);
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main_list_item, menuItems));
        
        Bundle extras = getIntent().getExtras();
        String fName = extras.getString("firstName");
        String lName = extras.getString("lastName");
        
        TextView tv_Welcome = (TextView)findViewById(R.id.main_tvw_welcome);
        tv_Welcome.setText("Welcome Back " + fName + " " + lName + "!");
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
				this.setResult(((ParkingApplication)getApplication()).getResultCloseAll());
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