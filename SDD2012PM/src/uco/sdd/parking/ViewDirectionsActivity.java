package uco.sdd.parking;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewDirectionsActivity extends ListActivity {

	private DirectionsAdapter arrayAdapter;
	private List<RouteStep> routeSteps;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.viewdirections_layout);
	    
	    routeSteps = ((ParkingApplication)getApplication()).getSteps();
	    
	    arrayAdapter = new DirectionsAdapter(this, R.layout.viewdirections_list_item, routeSteps);
	    
	    setListAdapter(arrayAdapter);
	}

	public class DirectionsAdapter extends ArrayAdapter<RouteStep> {
		 
	    int resource;
	    String response;
	    Context context;
	    
	    public DirectionsAdapter(Context context, int resource, List<RouteStep> items) {
	        
	    	super(context, resource, items);
	        this.resource=resource;
	    }
	 
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	        LinearLayout directionsView;
	        RouteStep currentStep = getItem(position);
	 
	        //Inflate the view
	        if(convertView == null) {
	        	
	            directionsView = new LinearLayout(getContext());
	            String inflater = Context.LAYOUT_INFLATER_SERVICE;
	            LayoutInflater vi;
	            vi = (LayoutInflater)getContext().getSystemService(inflater);
	            vi.inflate(resource, directionsView, true);
	        }
	        else
	        {
	            directionsView = (LinearLayout) convertView;
	        }
	        
	        //Get the text boxes from the viewdirections_list_item.xml file
	        TextView tvStepNumber = (TextView)directionsView.findViewById(R.id.viewdirections_lst_stepnumber);
	        TextView tvInstruction = (TextView)directionsView.findViewById(R.id.viewdirections_lst_step);
	        TextView tvDistance = (TextView)directionsView.findViewById(R.id.viewdirections_lst_distance);
	 
	        //Assign the appropriate data from our RouteStep object above
	        tvStepNumber.setText(Integer.toString(position + 1) + ".");
	        tvInstruction.setText(Html.fromHtml(currentStep.getInstructions().trim()));
	        tvDistance.setText(currentStep.getDistance());
	 
	        return directionsView;
	    }
	 
	}
}
