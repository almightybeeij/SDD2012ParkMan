package uco.sdd.parking;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uco.sdd.utility.GetJSONListener;
import uco.sdd.utility.HTTPDataAccess;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ViewParkingMapActivity extends MapActivity {

	private MapView mapView;
	private MapController mc;
	private List<Overlay> mapOverlays;
	private List<ParkingLot> studentParkingLots;
	private List<ParkingLot> facultyParkingLots;
	private Projection projection;
	private TextView tvDialog;
	
	private LocationManager locationManager;
	private String locationProvider;
	private myLocationListener locationListener;
	
	@Override
	protected boolean isRouteDisplayed() {
		
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.viewmap_layout);
	    
	    Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    
	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	    locationListener = new myLocationListener();
	    locationProvider = locationManager.getBestProvider(criteria, true);
	    locationManager.requestLocationUpdates(locationProvider, 1000, 0, locationListener);
	    
	    studentParkingLots = new ArrayList<ParkingLot>();
	    facultyParkingLots = new ArrayList<ParkingLot>();
	    
	    mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        projection = mapView.getProjection();
	    mc = mapView.getController();
	    
	    mapOverlays = mapView.getOverlays();  
	    mapOverlays.add(new ParkingLotStudentOverlay());
	    mapOverlays.add(new ParkingLotFacultyOverlay());
	    //mapOverlays.add(new ParkingSpaceStudentOverlay());
	    
        String coordinates[] = {"35.654108", "-97.473863"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
 
        GeoPoint p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
 
        mc.animateTo(p);
        mc.setZoom(19); 
        
        selectParkingLotCoordinates(true, false);
        selectParkingLotCoordinates(false, true);
        selectDirections("35.652937,-97.478342", "35.6589,-97.469459");
	}
	
	@Override
	public void onDestroy()
	{
		locationManager.removeUpdates(locationListener);
		super.onDestroy();
	}
	
	public void mapButtonOnClick(View view)
	{
		switch(view.getId())
		{
			case R.id.viewparking_btn_sat:
			{
				mapView.setStreetView(false);
				mapView.setSatellite(true);
				break;
			}
			case R.id.viewparking_btn_str:
			{
				mapView.setSatellite(false);
				mapView.setStreetView(true);
			}
		}
	}
	
	public void getDirectionsOnClick(View view)
	{
		Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		
		if (lastKnownLocation != null)
		{
			tvDialog.setText(Double.toString(lastKnownLocation.getLatitude()) +
					", " + Double.toString(lastKnownLocation.getLongitude()));
			
			GeoPoint p = new GeoPoint((int) (lastKnownLocation.getLatitude() * 1E6),
					(int) (lastKnownLocation.getLongitude() * 1E6));
		 
		    mc.animateTo(p);
		}
	}
	
	public void selectDirections(String origin, String destination)
	{
		HTTPDataAccess dac = new HTTPDataAccess(this,
    			"http://maps.googleapis.com/maps/api/directions/json?origin=35.652937,-97.478342&destination=35.6589,-97.469459&sensor=false"
, new GetDirectionsJSONListener());
	    
		dac.setUsingStatement(false);
	    
    	//dac.addNewBindVariable("origin", origin, false);
    	//dac.addNewBindVariable("destination", destination, false);
    	//dac.addNewBindVariable("sensor", "false", false);
    	
    	dac.executeSelect();
	}
	
	public void selectParkingLotCoordinates(boolean studentLot, boolean facultyLot)
	{
		int isStudent = studentLot ? 1 : 0;
		int isFaculty = facultyLot ? 1 : 0;
		
		GetJSONListener listener;
		
		listener = studentLot ? new StudentLotCoordinatesJSONListener() : new FacultyLotCoordinatesJSONListener();
		
		HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), listener);
	    
	    dac.setStatement(getString(R.string.viewparking_smt_lotcoord));
	    dac.setTypes(getString(R.string.viewparking_smt_lotcoord_types));
    	dac.addNewBindVariable("studentLot", Integer.toString(isStudent), false);
    	dac.addNewBindVariable("facultyLot", Integer.toString(isFaculty), false);
    	dac.setUsingProgress(false);
    	
    	dac.executeSelect();
	}
	
	public void selectParkingSpaces(int lotId, GetJSONListener listener)
	{
		HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), listener);
	    
	    dac.setStatement(getString(R.string.viewparking_smt_spacecoord));
	    dac.setTypes(getString(R.string.viewparking_smt_spacecoord_types));
    	dac.addNewBindVariable("lotId", Integer.toString(lotId), false);
    	dac.setUsingProgress(false);
    	
    	dac.executeSelect();
	}
	
	public void showParkingLotDialog(ParkingLot lot)
	{
		LayoutInflater inflater = getLayoutInflater();
		
		View dialoglayout = inflater.inflate(R.layout.viewmap_dialog_layout, (ViewGroup) getCurrentFocus());
		
		tvDialog = (TextView)dialoglayout.findViewById(R.id.viewmap_dlg_test);
		tvDialog.setText("Type: " + lot.getParkingType());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Parking Lot " + Integer.toString(lot.getLotId()));
		builder.setView(dialoglayout);
		builder.show();
	}
	
	public class ParkingLotStudentOverlay extends Overlay
	{
		private boolean isInitialized = false;
		
		private Paint fillPaint;
		private Paint strokePaint;
		
		private void init()
		{
			strokePaint = new Paint();
			strokePaint.setAntiAlias(true);
            strokePaint.setColor(getResources().getColor(R.color.crimson));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeWidth(2);
            strokePaint.setAlpha(100);
            
            fillPaint = new Paint();
			fillPaint.setAntiAlias(true);
            fillPaint.setColor(Color.RED);
            fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            fillPaint.setStrokeJoin(Paint.Join.ROUND);
            fillPaint.setStrokeCap(Paint.Cap.ROUND);
            fillPaint.setStrokeWidth(1);
            fillPaint.setAlpha(60);
		}
		
		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
    		
			super.draw(canvas, mapv, shadow);
    		
			if (shadow == false)
			{
	    		if (!isInitialized)
	    		{
	    			this.init();
	    			this.isInitialized = true;
	    		}
	    		
	    		Point coordInitial = new Point();
	    		Point coordPoint = new Point();
	    		
	    		for (ParkingLot lot : studentParkingLots)
	    		{
	    			int count = 0;
	    			
	    			lot.clearPath();
	    			Path lotPath = lot.getLotPath();
	    			GeoPoint initial = lot.getCoordinates().get(0);
	    			projection.toPixels(initial, coordInitial);
	    			lotPath.moveTo(coordInitial.x, coordInitial.y);
	    			
	    			for (GeoPoint coordinate : lot.getCoordinates())
	    			{
	    				count++;
	    				
	    				projection.toPixels(coordinate, coordPoint);
	    				
	    				if (count > 1)
	    				{
	    					lotPath.lineTo(coordPoint.x, coordPoint.y);
	    				}
	    			}
	    			
	    			lotPath.lineTo(coordInitial.x, coordInitial.y);
	    			lotPath.close();
	    			
	    			canvas.drawPath(lotPath, strokePaint);            
	                canvas.drawPath(lotPath, fillPaint);
	                canvas.drawText(Integer.toString(lot.getLotId()),
	                		coordPoint.x + 10, coordPoint.y + 20, strokePaint);
	    		}
			}
		}
		
		@Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) 
        {   
            if (event.getAction() == 1)
            {      
            	for (ParkingLot lot : studentParkingLots)
            	{
            		Path lotPath = lot.getLotPath();
            		
	            	if (lotPath != null)
	            	{
	            		RectF lotRect = new RectF();
	            		lotPath.computeBounds(lotRect, false);
	                    
	            		if (lotRect.contains(event.getX(), event.getY()))
	            		{
	            			showParkingLotDialog(lot);
	            			
	            			break;
	            		}                    
	            	}
            	}
            } 
            mapView.invalidate();
            return false;
        }
	}
	
	public class ParkingSpaceStudentOverlay extends Overlay
	{
		private boolean isInitialized = false;
		
		private Paint fillPaint;
		private Paint strokePaint;
    	private Path spacePath;
    	
		private void init()
		{
			strokePaint = new Paint();
			strokePaint.setAntiAlias(false);
            strokePaint.setColor(getResources().getColor(R.color.black));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeWidth(1);
            strokePaint.setAlpha(40);
            
            fillPaint = new Paint();
			fillPaint.setAntiAlias(false);
            fillPaint.setColor(Color.RED);
            fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            fillPaint.setStrokeJoin(Paint.Join.ROUND);
            fillPaint.setStrokeCap(Paint.Cap.ROUND);
            fillPaint.setStrokeWidth(1);
            fillPaint.setAlpha(60);
		}
		
		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
    		
			super.draw(canvas, mapv, shadow);
    		
			if (shadow == false)
			{
	    		if (!isInitialized)
	    		{
	    			this.init();
	    			this.isInitialized = true;
	    		}
	    		
	    		spacePath = new Path();
	    		
	    		Point coordInitial = new Point();
	    		Point coordPoint = new Point();
	    		
	    		for (ParkingLot lot : studentParkingLots)
	    		{
	    			for (ParkingSpace space : lot.getParkingSpaces())
	                {
	                	int count = 0;
	                	
	                	GeoPoint initial = space.getCorners().get(0);
		    			projection.toPixels(initial, coordInitial);
		    			spacePath.moveTo(coordInitial.x, coordInitial.y);
		    			
		    			for (GeoPoint corner : space.getCorners())
		    			{
		    				count++;
		    				
		    				projection.toPixels(corner, coordPoint);
		    				
		    				if (count > 1)
		    				{
		    					spacePath.lineTo(coordPoint.x, coordPoint.y);
		    				}
		    			}
		    			
		    			spacePath.lineTo(coordInitial.x, coordInitial.y);
		    			spacePath.close();
		    			
		    			canvas.drawPath(spacePath, strokePaint);            
		                //canvas.drawPath(spacePath, fillPaint);
	                }
	    		}
			}
		}
	}
	
	public class ParkingLotFacultyOverlay extends Overlay
	{
		private boolean isInitialized = false;
		
		private Paint fillPaint;
		private Paint strokePaint;
		
		private void init()
		{
			strokePaint = new Paint();
			strokePaint.setAntiAlias(true);
            strokePaint.setColor(getResources().getColor(R.color.navy));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeWidth(2);
            strokePaint.setAlpha(100);
            
            fillPaint = new Paint();
			fillPaint.setAntiAlias(true);
            fillPaint.setColor(Color.BLUE);
            fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            fillPaint.setStrokeJoin(Paint.Join.ROUND);
            fillPaint.setStrokeCap(Paint.Cap.ROUND);
            fillPaint.setStrokeWidth(1);
            fillPaint.setAlpha(60);
		}
		
		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
    		
			super.draw(canvas, mapv, shadow);
    		
			if (shadow == false)
			{
	    		if (!isInitialized)
	    		{
	    			this.init();
	    			this.isInitialized = true;
	    		}
	    		
	    		Point coordInitial = new Point();
	    		Point coordPoint = new Point();
	    		
	    		for (ParkingLot lot : facultyParkingLots)
	    		{
	    			int count = 0;
	    			
	    			lot.clearPath();
	    			Path lotPath = lot.getLotPath();
	    			GeoPoint initial = lot.getCoordinates().get(0);
	    			projection.toPixels(initial, coordInitial);
	    			lotPath.moveTo(coordInitial.x, coordInitial.y);
	    			
	    			for (GeoPoint coordinate : lot.getCoordinates())
	    			{
	    				count++;
	    				
	    				projection.toPixels(coordinate, coordPoint);
	    				
	    				if (count > 1)
	    				{
	    					lotPath.lineTo(coordPoint.x, coordPoint.y);
	    				}
	    			}
	    			
	    			lotPath.lineTo(coordInitial.x, coordInitial.y);
	    			lotPath.close();
	    			
	    			canvas.drawPath(lotPath, strokePaint);            
	                canvas.drawPath(lotPath, fillPaint);
	                canvas.drawText(Integer.toString(lot.getLotId()), coordPoint.x + 10, coordPoint.y - 15, strokePaint); 
	    		}
			}
		}
		
		@Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) 
        {   
			if (event.getAction() == 1)
            {      
            	for (ParkingLot lot : facultyParkingLots)
            	{
            		Path lotPath = lot.getLotPath();
            		
	            	if (lotPath != null)
	            	{
	            		RectF lotRect = new RectF();
	            		lotPath.computeBounds(lotRect, false);
	                    
	            		if (lotRect.contains(event.getX(), event.getY()))
	            		{
	            			showParkingLotDialog(lot);
	            			break;
	            		}                    
	            	}
            	}
            }                            
            return false;
        }   
	}
	
	private class StudentLotCoordinatesJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			    	
			int lotId = 0;
			String coordinatePair;
			String[] coordinates;
			ParkingLot lot;
			
	    	try
	    	{
	    		if (jArray != null)
	    		{
	    			if (jArray.length() > 0)
	    			{
	    				lot = new ParkingLot();
	    				
	    				for(int index = 0; index < jArray.length(); index++)
				    	{
				    		JSONObject json_data = jArray.getJSONObject(index);
				    		
				    		if (index == 0) lotId = json_data.getInt("lotid");
				    		coordinatePair = json_data.getString("coordinates");
				    		coordinates = coordinatePair.split(",");
				    		lot.addCoordinate(coordinates[1], coordinates[0]);
				    		lot.addBoundary(json_data.getString("boundary1"));
				    		lot.addBoundary(json_data.getString("boundary2"));
				    		lot.addBoundary(json_data.getString("boundary3"));
				    		lot.addBoundary(json_data.getString("boundary4"));
				    		lot.setParkingType("Student");
				    	}
	    				
	    				lot.setLotId(lotId);
	    				studentParkingLots.add(lot);
	    				mapView.postInvalidate();
	    				
	    				for (ParkingLot studentLot : studentParkingLots)
	    				{
	    					selectParkingSpaces(studentLot.getLotId(), new StudentSpacesJSONListener());
	    				}
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
		
		public void onRemoteCallComplete(JSONObject jObject) {}
	}
	 
	private class FacultyLotCoordinatesJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			    	
			int lotId = 0;
			String coordinatePair;
			String[] coordinates;
			ParkingLot lot;
			
	    	try
	    	{
	    		if (jArray != null)
	    		{
	    			if (jArray.length() > 0)
	    			{
	    				lot = new ParkingLot();
	    				
	    				for(int index = 0; index < jArray.length(); index++)
				    	{
				    		JSONObject json_data = jArray.getJSONObject(index);
				    		
				    		if (index == 0) lotId = json_data.getInt("lotid");
				    		coordinatePair = json_data.getString("coordinates");
				    		coordinates = coordinatePair.split(",");
				    		lot.addCoordinate(coordinates[1], coordinates[0]);
				    		lot.addBoundary(json_data.getString("boundary1"));
				    		lot.addBoundary(json_data.getString("boundary2"));
				    		lot.addBoundary(json_data.getString("boundary3"));
				    		lot.addBoundary(json_data.getString("boundary4"));
				    		lot.setParkingType("Faculty");
				    	}
	    				
	    				lot.setLotId(lotId);
	    				facultyParkingLots.add(lot);
	    				mapView.postInvalidate();
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
		
		public void onRemoteCallComplete(JSONObject jObject) {}
	}
	
	private class StudentSpacesJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			    
			ParkingLot lotLookFor;
			ParkingLot lotMatch;
			ParkingSpace space;
			JSONObject json_data;
			
			try
	    	{
	    		if (jArray != null)
	    		{
	    			if (jArray.length() > 0)
	    			{
	    				lotLookFor = new ParkingLot();
	    				lotMatch = new ParkingLot();
	    				
	    				json_data = jArray.getJSONObject(0);
	    				
	    				int lotId = json_data.getInt("ParkingLot_lotId");
			    		lotLookFor.setLotId(lotId);
			    		
			    		int match = studentParkingLots.indexOf(lotLookFor);
			    		
			    		if (match != -1)
			    		{
			    			lotMatch = studentParkingLots.get(match);
			    			
			    			for(int index = 0; index < jArray.length(); index++)
					    	{
					    		json_data = jArray.getJSONObject(index);
					    		
					    		space = new ParkingSpace();
					    		space.setSpaceId(json_data.getInt("spaceId"));
					    		space.setClientEmail(json_data.getString("Client_email"));
					    		space.addCorner(json_data.getString("corner1"));
					    		space.addCorner(json_data.getString("corner2"));
					    		space.addCorner(json_data.getString("corner3"));
					    		space.addCorner(json_data.getString("corner4"));
					    		space.setAvailable(BooleanFromString(json_data.getString("available")));
					    		space.setHandicap(BooleanFromString(json_data.getString("handicap")));
					    		
					    		lotMatch.getParkingSpaces().add(space);
					    	}
			    		}
	    				
	    				mapView.postInvalidate();
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
		
		public void onRemoteCallComplete(JSONObject jObject) {}
	}
	
	private class FacultySpacesJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			    
			ParkingSpace space;
			
			try
	    	{
	    		if (jArray != null)
	    		{
	    			if (jArray.length() > 0)
	    			{
	    				space = new ParkingSpace();
	    				
	    				for(int index = 0; index < jArray.length(); index++)
				    	{
				    		JSONObject json_data = jArray.getJSONObject(index);
				    		
				    		
				    	}
	    				
	    				mapView.postInvalidate();
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
		
		public void onRemoteCallComplete(JSONObject jObject) {}
	}
	
	private class GetDirectionsJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {}
		
		public void onRemoteCallComplete(JSONObject jObject) {
			
			try
	    	{
	    		if (jObject != null)
	    		{
	    			if (jObject.length() > 0)
	    			{
			    		String status = jObject.getString("status");
			    		String test = status;
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
	}
	
	public class myLocationListener implements LocationListener
	{    
		public void onLocationChanged(Location location)
	    {
	      // Called when a new location is found by the network location provider.
	      //makeUseOfNewLocation(location);
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}
	}
	  
	private boolean BooleanFromString(String value)
	{
		if (value == "1")
			return true;
		else
			return false;
	}
}
