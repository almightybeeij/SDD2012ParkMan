/*
 * ViewParkingMapActivity Class
 * 
 * Version 1.0.0
 * 
 * Author: Cory Wilson
 * Last Updated: 04/19/2012
 * Last Updated By: Cory Wilson
 * 
 * Represents the screen used to display the parking map.
 * Coordinates are pulled from the database and the data
 * is used to overlay parking lots and parking spaces
 * onto the MapView. Also allows the user to check in or
 * out of a parking space as well as get directions to a
 * specific parking lot. 
 */

package uco.sdd.parking;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class ViewParkingMapActivity extends MapActivity {
	
	private String locationProvider;
	private String viewSpaceId;
	private String viewLotId;
	private boolean viewSpace;
	
	private MapView mapView;
	private MapController mc;
	private List<Overlay> mapOverlays;
	private List<ParkingLot> studentParkingLots;
	private List<ParkingLot> facultyParkingLots;
	private ArrayList<String> parkingSpaces;
	private ArrayList<String> viewSpaces;
	private ParkingLot selectedLot;
	private Projection projection;
	private Route directions;
	private AlertDialog dialog;
	private LocationManager locationManager;
	private MyLocationListener locationListener;
	
	@Override
	protected boolean isRouteDisplayed() {
		
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.viewmap_layout);
	    
	    viewSpace = false;
	    Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    
	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	    locationListener = new MyLocationListener();
	    locationProvider = locationManager.getBestProvider(criteria, true);
	    locationManager.requestLocationUpdates(locationProvider, 1000, 0, locationListener);
	    
	    studentParkingLots = new ArrayList<ParkingLot>();
	    facultyParkingLots = new ArrayList<ParkingLot>();
	    viewSpaces = new ArrayList<String>();
	    
	    directions = new Route();
	    dialog = new AlertDialog.Builder(this).create();
	    
	    mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        projection = mapView.getProjection();
	    mc = mapView.getController();
	    
	    mapOverlays = mapView.getOverlays();  
	    mapOverlays.add(new ParkingLotStudentOverlay());
	    mapOverlays.add(new ParkingLotFacultyOverlay());
	    
        String coordinates[] = {"35.654108", "-97.473863"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
 
        GeoPoint p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
 
        mc.animateTo(p);
        mc.setZoom(19); 
        
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
        	viewSpace = true;
        	viewSpaceId = extras.getString("spaceId");
        	viewLotId = extras.getString("lotId");
        	viewSpaces.add(extras.getString("space1"));
        	viewSpaces.add(extras.getString("space2"));
        	viewSpaces.add(extras.getString("space3"));
        	viewSpaces.add(extras.getString("space4"));
        }
        
        selectParkingLotCoordinates(true, false);
        selectParkingLotCoordinates(false, true);
	}
	
	@Override
	public void onBackPressed()	{
		
		this.setResult(((ParkingApplication)getApplication()).getResultUpdate());
		super.onBackPressed();
	}
	
	@Override
	public void onDestroy() {
		
		locationManager.removeUpdates(locationListener);
		super.onDestroy();
	}
	
	public void mapButtonOnClick(View view)	{
		
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
	
	public void resetOverlays()	{
		
		mapOverlays.clear();
		
		mapOverlays.add(new ParkingLotStudentOverlay());
	    mapOverlays.add(new ParkingLotFacultyOverlay());
	    
	    for (ParkingLot lot : studentParkingLots)
	    {
	    	addStudentParkingSpaces(lot);
	    }
	    
	    for (ParkingLot lot : facultyParkingLots)
	    {
	    	addFacultyParkingSpaces(lot);
	    }
	    
	    mapView.invalidate();
	}
	
	public void selectDirections(String origin, String destination)
	{
		HTTPDataAccess dac = new HTTPDataAccess(this,
    			"http://maps.googleapis.com/maps/api/directions/json?" +
    			"origin=" + origin + "&destination=" + destination + "&sensor=false",
    			new GetDirectionsJSONListener());
	    
		dac.setUsingStatement(false);
	    dac.setUsingEncoding(true);
	    dac.setEncoding(HTTP.UTF_8);
    	
    	dac.executeSelectSingle();
	}
	
	public void addDirections()
	{
		int size = directions.getSteps().size();
		
		resetOverlays();
		
		for (int index = 0; index < size; index++)
		{
			int polySize = directions.getSteps().get(index).getPolyPoints().size();
			
			if (polySize > 0)
			{
				mapOverlays.add(new DirectionPathOverlay(directions.getSteps().get(index).getStartLocation(),
					directions.getSteps().get(index).getPolyPoints().get(0)));
				
				for (int polyPoint = 0; polyPoint < polySize; polyPoint++)
				{
					if (polyPoint + 1 == polySize)
					{
						mapOverlays.add(new DirectionPathOverlay(directions.getSteps().get(index).getPolyPoints().get(polyPoint),
							directions.getSteps().get(index).getEndLocation()));
					}
					else
					{
						mapOverlays.add(new DirectionPathOverlay(directions.getSteps().get(index).getPolyPoints().get(polyPoint),
								directions.getSteps().get(index).getPolyPoints().get(polyPoint + 1)));
					}
				}
			}
			else
			{
				mapOverlays.add(new DirectionPathOverlay(directions.getSteps().get(index).getStartLocation(),
					directions.getSteps().get(index).getEndLocation()));
			}
		}
		
		ArrayList<GeoPoint> bounds = new ArrayList<GeoPoint>();
		bounds.add(directions.getBoundsNE());
		bounds.add(directions.getBoundsSW());
		
		zoomInBounds(bounds);
		mapView.invalidate();
	}
	
	public void addStudentParkingSpaces(ParkingLot lot)
	{
		int matchLotId = -1;
		
		if (viewLotId != null)
			matchLotId = Integer.parseInt(viewLotId);
		
		if (lot.getLotId() == matchLotId)
		{
			int matchId = Integer.parseInt(viewSpaceId);
						
			for (ParkingSpace space : lot.getParkingSpaces())
			{
				if (space.getSpaceId() != matchId)
				{
					mapOverlays.add(new StudentParkingSpaceOverlay(space.getCorners().get(0), space.getCorners().get(1),
						space.getCorners().get(2), space.getCorners().get(3), space.isAvailable()));
				}
				else
				{
					mapOverlays.add(new ViewParkingSpaceOverlay(space.getCorners().get(0), space.getCorners().get(1),
							space.getCorners().get(2), space.getCorners().get(3)));
				}
			}
		}
		else
		{
			for (ParkingSpace space : lot.getParkingSpaces())
			{
				mapOverlays.add(new StudentParkingSpaceOverlay(space.getCorners().get(0), space.getCorners().get(1),
						space.getCorners().get(2), space.getCorners().get(3), space.isAvailable()));
			}
		}
	}
	
	public void addFacultyParkingSpaces(ParkingLot lot)
	{
		int matchLotId = -1;
		
		if (viewLotId != null)
			matchLotId = Integer.parseInt(viewLotId);
		
		if (lot.getLotId() == matchLotId)
		{
			int matchId = Integer.parseInt(viewSpaceId);
						
			for (ParkingSpace space : lot.getParkingSpaces())
			{
				if (space.getSpaceId() != matchId)
				{
					mapOverlays.add(new FacultyParkingSpaceOverlay(space.getCorners().get(0), space.getCorners().get(1),
						space.getCorners().get(2), space.getCorners().get(3), space.isAvailable()));
				}
				else
				{
					mapOverlays.add(new ViewParkingSpaceOverlay(space.getCorners().get(0), space.getCorners().get(1),
							space.getCorners().get(2), space.getCorners().get(3)));
				}
			}
		}
		else
		{
			for (ParkingSpace space : lot.getParkingSpaces())
			{
				mapOverlays.add(new FacultyParkingSpaceOverlay(space.getCorners().get(0), space.getCorners().get(1),
						space.getCorners().get(2), space.getCorners().get(3), space.isAvailable()));
			}
		}
	}
	
	public void zoomInBounds(ArrayList<GeoPoint> bounds) {

	    int minLat = Integer.MAX_VALUE;
	    int minLong = Integer.MAX_VALUE;
	    int maxLat = Integer.MIN_VALUE;
	    int maxLong = Integer.MIN_VALUE;

	    for (GeoPoint point : bounds) {
	        minLat = Math.min(point.getLatitudeE6(), minLat);
	        minLong = Math.min(point.getLongitudeE6(), minLong);
	        maxLat = Math.max(point.getLatitudeE6(), maxLat);
	        maxLong = Math.max(point.getLongitudeE6(), maxLong);
	    }

	    mc.zoomToSpan(Math.abs(minLat - maxLat), Math.abs(minLong - maxLong));
	    mc.animateTo(new GeoPoint((maxLat + minLat) / 2,
	        (maxLong + minLong) / 2));
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
		selectedLot = lot;
		parkingSpaces = new ArrayList<String>();
		
		LayoutInflater inflater = getLayoutInflater();		
		View dialoglayout = inflater.inflate(R.layout.viewmap_dialog_layout, (ViewGroup) getCurrentFocus());

		dialog.setView(dialoglayout);
		
		if (lot.isStudent()) {
			dialog.setTitle("Student Lot " + Integer.toString(lot.getLotId()));
		}
		else {
			dialog.setTitle("Faculty Lot " + Integer.toString(lot.getLotId()));
		}
			
		if (!viewSpace) {
			
			int count = 0;
			for (ParkingSpace space : lot.getParkingSpaces()) {
				
				if (count <= 10) {
				
					parkingSpaces.add(Integer.toString(space.getSpaceId()));
				}
				count++;
			}
		}
		else {
			
			parkingSpaces.add(viewSpaceId);
			for (String spaceId : viewSpaces)
			{
				parkingSpaces.add(spaceId);
			}
		}
			
		ArrayAdapter<String> adapterSpaces = new ArrayAdapter<String>(getApplicationContext(),
	    		R.layout.viewmap_spinner_item, R.id.viewparking_spn_itemtext, parkingSpaces);
	
		Spinner spinnerParkingSpaces = (Spinner)dialoglayout.findViewById(R.id.viewparking_spn_space);
    	spinnerParkingSpaces.setAdapter(adapterSpaces);
				
		Button btnCheckInOut = (Button)dialoglayout.findViewById(R.id.viewparking_id_checkin);
		btnCheckInOut.setOnClickListener(new CheckInOutOnClickListener());
		
		Button btnViewInMap = (Button)dialoglayout.findViewById(R.id.viewparking_id_viewspace);
		btnViewInMap.setOnClickListener(new ViewInMapOnClickListener());
		
		Button btnGetDirections = (Button)dialoglayout.findViewById(R.id.viewparking_id_getdirections);
		btnGetDirections.setOnClickListener(new GetDirectionsOnClickListener());
		
		dialog.show();
	}
	
	public class CheckInOutOnClickListener implements OnClickListener
	{
		public void onClick(View view)
		{
			dialog.getLayoutInflater().inflate(R.layout.viewmap_dialog_layout, (ViewGroup) getCurrentFocus());
			
			Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
			
			if (lastKnownLocation != null)
			{
				selectDirections(Double.toString(lastKnownLocation.getLatitude()) +
						"," + Double.toString(lastKnownLocation.getLongitude()), selectedLot.getDirectionTo());
			}
			
			dialog.dismiss();
		}
	}
	
	public class ViewInMapOnClickListener implements OnClickListener
	{
		public void onClick(View view)
		{
			Spinner spinnerParkingSpaces = (Spinner)dialog.findViewById(R.id.viewparking_spn_space);
			
			String selectedSpaceId = (String)spinnerParkingSpaces.getSelectedItem();
			viewSpaceId = selectedSpaceId;
			viewLotId = Integer.toString(selectedLot.getLotId());
			
			resetOverlays();
			dialog.dismiss();
		}
	}
	
	public class GetDirectionsOnClickListener implements OnClickListener
	{
		public void onClick(View view)
		{
			Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
			
			if (lastKnownLocation != null)
			{
				//selectDirections("35.335293,-97.489929", selectedLot.getDirectionTo());
				
				selectDirections(Double.toString(lastKnownLocation.getLatitude()) +
						"," + Double.toString(lastKnownLocation.getLongitude()), selectedLot.getDirectionTo());
			}
			
			dialog.dismiss();
		}
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
	    			
	    			projection.toPixels(lot.getCoordinates().get(3), coordPoint);
	    			
	    			canvas.drawPath(lotPath, strokePaint);            
	                canvas.drawPath(lotPath, fillPaint);
	                canvas.drawText(Integer.toString(lot.getLotId()),
	                		coordPoint.x + 10, coordPoint.y - 10, strokePaint);
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
	
	public class StudentParkingSpaceOverlay extends Overlay
	{
		private boolean isInitialized = false;
		private boolean isAvailable = false;
		
		private GeoPoint gp1;
	    private GeoPoint gp2;
	    private GeoPoint gp3;
	    private GeoPoint gp4;
	    
		private Paint fillPaint;
		private Paint strokePaint;
    	private Path spacePath;
    	
    	public StudentParkingSpaceOverlay(GeoPoint corner1, GeoPoint corner2,
    			GeoPoint corner3, GeoPoint corner4, boolean available)
    	{
    		gp1 = corner1;
    		gp2 = corner2;
    		gp3 = corner3;
    		gp4 = corner4;
    		isAvailable = available;
    	}
    	
		private void init()
		{
			strokePaint = new Paint();
			strokePaint.setAntiAlias(true);
            strokePaint.setColor(getResources().getColor(R.color.black));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeWidth(1);
            strokePaint.setAlpha(40);
            
            fillPaint = new Paint();
            
            if (isAvailable) {
            	fillPaint.setColor(Color.RED);
            }
            else {
            	fillPaint.setColor(Color.BLACK);
            }
            
			fillPaint.setAntiAlias(false);
            fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            fillPaint.setStrokeJoin(Paint.Join.ROUND);
            fillPaint.setStrokeCap(Paint.Cap.ROUND);
            fillPaint.setStrokeWidth(1);
            fillPaint.setAlpha(130);	
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
	    		
	    		projection.toPixels(gp1, coordInitial);
    			spacePath.moveTo(coordInitial.x, coordInitial.y);
    			
    			projection.toPixels(gp2, coordPoint);
    			spacePath.lineTo(coordPoint.x, coordPoint.y);
    			
    			projection.toPixels(gp3, coordPoint);
    			spacePath.lineTo(coordPoint.x, coordPoint.y);
    			
    			projection.toPixels(gp4, coordPoint);
    			spacePath.lineTo(coordPoint.x, coordPoint.y);
    			spacePath.lineTo(coordInitial.x, coordInitial.y);
    			
    			spacePath.close();
    			
    			canvas.drawPath(spacePath, strokePaint);            
                canvas.drawPath(spacePath, fillPaint);
			}
		}
	}

	public class FacultyParkingSpaceOverlay extends Overlay
	{
		private boolean isInitialized = false;
		private boolean isAvailable = false;
		
		private GeoPoint gp1;
	    private GeoPoint gp2;
	    private GeoPoint gp3;
	    private GeoPoint gp4;
	    
		private Paint fillPaint;
		private Paint strokePaint;
    	private Path spacePath;
    	
    	public FacultyParkingSpaceOverlay(GeoPoint corner1, GeoPoint corner2,
    			GeoPoint corner3, GeoPoint corner4, boolean available)
    	{
    		gp1 = corner1;
    		gp2 = corner2;
    		gp3 = corner3;
    		gp4 = corner4;
    		isAvailable = available;
    	}
    	
		private void init()
		{
			strokePaint = new Paint();
			strokePaint.setAntiAlias(true);
            strokePaint.setColor(getResources().getColor(R.color.navy));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeWidth(1);
            strokePaint.setAlpha(40);
            
            fillPaint = new Paint();
            
            if (isAvailable) {
            	fillPaint.setColor(Color.BLUE);
            }
            else {
            	fillPaint.setColor(Color.BLACK);
            }
            
			fillPaint.setAntiAlias(false);
            fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            fillPaint.setStrokeJoin(Paint.Join.ROUND);
            fillPaint.setStrokeCap(Paint.Cap.ROUND);
            fillPaint.setStrokeWidth(1);
            fillPaint.setAlpha(130);
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
	    		
	    		projection.toPixels(gp1, coordInitial);
    			spacePath.moveTo(coordInitial.x, coordInitial.y);
    			
    			projection.toPixels(gp2, coordPoint);
    			spacePath.lineTo(coordPoint.x, coordPoint.y);
    			
    			projection.toPixels(gp3, coordPoint);
    			spacePath.lineTo(coordPoint.x, coordPoint.y);
    			
    			projection.toPixels(gp4, coordPoint);
    			spacePath.lineTo(coordPoint.x, coordPoint.y);
    			spacePath.lineTo(coordInitial.x, coordInitial.y);
    			
    			spacePath.close();
    			
    			canvas.drawPath(spacePath, strokePaint);            
                canvas.drawPath(spacePath, fillPaint);
			}
		}
	}
	
	public class ViewParkingSpaceOverlay extends Overlay
	{
		private boolean isInitialized = false;
		
		private GeoPoint gp1;
	    private GeoPoint gp2;
	    private GeoPoint gp3;
	    private GeoPoint gp4;
	    
		private Paint fillPaint;
		private Paint strokePaint;
    	private Path spacePath;
    	
    	public ViewParkingSpaceOverlay(GeoPoint corner1, GeoPoint corner2,
    			GeoPoint corner3, GeoPoint corner4)
    	{
    		gp1 = corner1;
    		gp2 = corner2;
    		gp3 = corner3;
    		gp4 = corner4;
    	}
    	
		private void init()
		{
			strokePaint = new Paint();
			strokePaint.setAntiAlias(true);
            strokePaint.setColor(getResources().getColor(R.color.uco_yellow));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeWidth(1);
            strokePaint.setAlpha(200);
            
            fillPaint = new Paint();
            fillPaint.setColor(getResources().getColor(R.color.uco_yellow));            
			fillPaint.setAntiAlias(false);
            fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            fillPaint.setStrokeJoin(Paint.Join.ROUND);
            fillPaint.setStrokeCap(Paint.Cap.ROUND);
            fillPaint.setStrokeWidth(1);
            fillPaint.setAlpha(160);	
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
	    		
	    		projection.toPixels(gp1, coordInitial);
    			spacePath.moveTo(coordInitial.x, coordInitial.y);
    			
    			projection.toPixels(gp2, coordPoint);
    			spacePath.lineTo(coordPoint.x, coordPoint.y);
    			
    			projection.toPixels(gp3, coordPoint);
    			spacePath.lineTo(coordPoint.x, coordPoint.y);
    			
    			projection.toPixels(gp4, coordPoint);
    			spacePath.lineTo(coordPoint.x, coordPoint.y);
    			spacePath.lineTo(coordInitial.x, coordInitial.y);
    			
    			spacePath.close();
    			
    			canvas.drawPath(spacePath, strokePaint);            
                canvas.drawPath(spacePath, fillPaint);
			}
		}
	}
	
	public class ParkingLotFacultyOverlay extends Overlay {
		
		private boolean isInitialized = false;
		
		private Paint fillPaint;
		private Paint strokePaint;
		
		private void init() {
			
			strokePaint = new Paint();
			strokePaint.setAntiAlias(true);
            strokePaint.setColor(getResources().getColor(R.color.navy));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeWidth(2);
            strokePaint.setAlpha(100);
            
            fillPaint = new Paint();
			fillPaint.setAntiAlias(false);
            fillPaint.setColor(Color.BLUE);
            fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            fillPaint.setStrokeJoin(Paint.Join.ROUND);
            fillPaint.setStrokeCap(Paint.Cap.ROUND);
            fillPaint.setStrokeWidth(1);
            fillPaint.setAlpha(60);
		}
		
		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
    		
			super.draw(canvas, mapv, shadow);
    		
			if (shadow == false) {
				
	    		if (!isInitialized)	{
	    			
	    			this.init();
	    			this.isInitialized = true;
	    		}
	    		
	    		Point coordInitial = new Point();
	    		Point coordPoint = new Point();
	    		
	    		for (ParkingLot lot : facultyParkingLots) {
	    			
	    			int count = 0;
	    			
	    			lot.clearPath();
	    			Path lotPath = lot.getLotPath();
	    			GeoPoint initial = lot.getCoordinates().get(0);
	    			projection.toPixels(initial, coordInitial);
	    			lotPath.moveTo(coordInitial.x, coordInitial.y);
	    			
	    			for (GeoPoint coordinate : lot.getCoordinates()) {
	    				
	    				count++;
	    				
	    				projection.toPixels(coordinate, coordPoint);
	    				
	    				if (count > 1) {
	    					
	    					lotPath.lineTo(coordPoint.x, coordPoint.y);
	    				}
	    			}
	    			
	    			lotPath.lineTo(coordInitial.x, coordInitial.y);
	    			lotPath.close();
	    			
	    			projection.toPixels(lot.getCoordinates().get(2), coordPoint);
	    			
	    			canvas.drawPath(lotPath, strokePaint);            
	                canvas.drawPath(lotPath, fillPaint);
	                canvas.drawText(Integer.toString(lot.getLotId()), coordPoint.x + 10, coordPoint.y - 10, strokePaint); 
	    		}
			}
		}
		
		@Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			
			if (event.getAction() == 1) {
				
            	for (ParkingLot lot : facultyParkingLots) {
            		
            		Path lotPath = lot.getLotPath();
            		
	            	if (lotPath != null) {
	            		
	            		RectF lotRect = new RectF();
	            		lotPath.computeBounds(lotRect, false);
	                    
	            		if (lotRect.contains(event.getX(), event.getY())) {
	            			
	            			showParkingLotDialog(lot);
	            			break;
	            		}                    
	            	}
            	}
            }                            
            return false;
        }   
	}
	
	public class DirectionPathOverlay extends Overlay {
	    
		private boolean isInitialized = false;
		
		private GeoPoint gp1;
	    private GeoPoint gp2;
	    private Paint strokePaint;
	    
	    public DirectionPathOverlay(GeoPoint gp1, GeoPoint gp2) {
	    	
	        this.gp1 = gp1;
	        this.gp2 = gp2;
	    }

	    private void init() {
	    	
			strokePaint = new Paint();
			strokePaint.setAntiAlias(true);
            strokePaint.setColor(getResources().getColor(R.color.brightblue));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeWidth(3);
		}
	    
	    @Override
	    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	        
	        if (shadow == false) {
	        	
	        	if (!isInitialized)	{
	        		
	    			this.init();
	    			this.isInitialized = true;
	    		}
	        	
	            Point point = new Point();
	            projection.toPixels(gp1, point);
	            
	            Point point2 = new Point();
	            projection.toPixels(gp2, point2);
	            
	            canvas.drawLine((float) point.x, (float) point.y, (float) point2.x,
	                    (float) point2.y, strokePaint);
	        }
	        
	        return super.draw(canvas, mapView, shadow, when);
	    }

	    @Override
	    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	        
	        super.draw(canvas, mapView, shadow);
	    }
	}
	
	private class StudentLotCoordinatesJSONListener implements GetJSONListener {
		
		public void onRemoteCallComplete(JSONArray jArray) {
			    	
			int lotId = 0;
			String coordinatePair;
			String[] coordinates;
			ParkingLot lot;
			
	    	try	{
	    		
	    		if (jArray != null)	{
	    			
	    			if (jArray.length() > 0) {
	    				
	    				studentParkingLots = new ArrayList<ParkingLot>();
	    				lot = new ParkingLot();
	    				
	    				for(int index = 0; index < jArray.length(); index++) {
	    					
				    		JSONObject json_data = jArray.getJSONObject(index);
				    		
				    		if (index == 0) lotId = json_data.getInt("lotid");
				    		coordinatePair = json_data.getString("coordinates");
				    		coordinates = coordinatePair.split(",");
				    		lot.addCoordinate(coordinates[1], coordinates[0]);
				    		lot.addBoundary(json_data.getString("boundary1"));
				    		lot.addBoundary(json_data.getString("boundary2"));
				    		lot.addBoundary(json_data.getString("boundary3"));
				    		lot.addBoundary(json_data.getString("boundary4"));
				    		
				    		coordinatePair = json_data.getString("directionTo");
				    		coordinates = coordinatePair.split(",");
				    		lot.setDirectionTo(coordinates[1] + "," + coordinates[0]);
				    		lot.setStudent(true);
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
	 
	private class FacultyLotCoordinatesJSONListener implements GetJSONListener {
		
		public void onRemoteCallComplete(JSONArray jArray) {
			    	
			int lotId = 0;
			String coordinatePair;
			String[] coordinates;
			ParkingLot lot;
			
	    	try {
	    		
	    		if (jArray != null) {
	    			
	    			if (jArray.length() > 0) {
	    				
	    				facultyParkingLots = new ArrayList<ParkingLot>();
	    				lot = new ParkingLot();
	    				
	    				for(int index = 0; index < jArray.length(); index++) {
	    					
				    		JSONObject json_data = jArray.getJSONObject(index);
				    		
				    		if (index == 0) lotId = json_data.getInt("lotid");
				    		coordinatePair = json_data.getString("coordinates");
				    		coordinates = coordinatePair.split(",");
				    		lot.addCoordinate(coordinates[1], coordinates[0]);
				    		lot.addBoundary(json_data.getString("boundary1"));
				    		lot.addBoundary(json_data.getString("boundary2"));
				    		lot.addBoundary(json_data.getString("boundary3"));
				    		lot.addBoundary(json_data.getString("boundary4"));
				    		
				    		coordinatePair = json_data.getString("directionTo");
				    		coordinates = coordinatePair.split(",");
				    		lot.setDirectionTo(coordinates[1] + "," + coordinates[0]);
				    		lot.setFaculty(true);
				    		lot.setParkingType("Faculty");
				    	}
	    				
	    				lot.setLotId(lotId);
	    				facultyParkingLots.add(lot);
	    				mapView.postInvalidate();
	    				
	    				for (ParkingLot facultyLot : facultyParkingLots)
	    				{
	    					selectParkingSpaces(facultyLot.getLotId(), new FacultySpacesJSONListener());
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
	
	private class StudentSpacesJSONListener implements GetJSONListener {
		
		public void onRemoteCallComplete(JSONArray jArray) {
			    
			ParkingLot lotLookFor;
			ParkingLot lotMatch;
			ParkingSpace space;
			JSONObject json_data;
			
			try	{
				
	    		if (jArray != null) {
	    			
	    			if (jArray.length() > 0) {
	    				
	    				lotLookFor = new ParkingLot();
	    				lotMatch = new ParkingLot();
	    				
	    				json_data = jArray.getJSONObject(0);
	    				
	    				int lotId = json_data.getInt("ParkingLot_lotId");
			    		lotLookFor.setLotId(lotId);
			    		
			    		int match = studentParkingLots.indexOf(lotLookFor);
			    		
			    		if (match != -1) {
			    			
			    			lotMatch = studentParkingLots.get(match);
			    			
			    			for(int index = 0; index < jArray.length(); index++) {
			    				
					    		json_data = jArray.getJSONObject(index);
					    		
					    		space = new ParkingSpace();
					    		space.setSpaceId(json_data.getInt("spaceId"));
					    		space.setClientEmail(json_data.getString("Client_email"));
					    		space.addCorner(json_data.getString("corner1"));
					    		space.addCorner(json_data.getString("corner2"));
					    		space.addCorner(json_data.getString("corner3"));
					    		space.addCorner(json_data.getString("corner4"));
					    		space.setAvailable(BooleanFromInt(json_data.getInt("available")));
					    		space.setHandicap(BooleanFromInt(json_data.getInt("handicap")));
					    		
					    		lotMatch.getParkingSpaces().add(space);
					    	}
			    			
			    			addStudentParkingSpaces(lotMatch);
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
	
	private class FacultySpacesJSONListener implements GetJSONListener {
		
		public void onRemoteCallComplete(JSONArray jArray) {
			    
			ParkingLot lotLookFor;
			ParkingLot lotMatch;
			ParkingSpace space;
			JSONObject json_data;
			
			try	{
				
	    		if (jArray != null)	{
	    			
	    			if (jArray.length() > 0) {
	    				
	    				lotLookFor = new ParkingLot();
	    				lotMatch = new ParkingLot();
	    				
	    				json_data = jArray.getJSONObject(0);
	    				
	    				int lotId = json_data.getInt("ParkingLot_lotId");
			    		lotLookFor.setLotId(lotId);
			    		
			    		int match = facultyParkingLots.indexOf(lotLookFor);
			    		
			    		if (match != -1) {
			    			
			    			lotMatch = facultyParkingLots.get(match);
			    			
			    			for(int index = 0; index < jArray.length(); index++) {
			    				
					    		json_data = jArray.getJSONObject(index);
					    		
					    		space = new ParkingSpace();
					    		space.setSpaceId(json_data.getInt("spaceId"));
					    		space.setClientEmail(json_data.getString("Client_email"));
					    		space.addCorner(json_data.getString("corner1"));
					    		space.addCorner(json_data.getString("corner2"));
					    		space.addCorner(json_data.getString("corner3"));
					    		space.addCorner(json_data.getString("corner4"));
					    		space.setAvailable(BooleanFromInt(json_data.getInt("available")));
					    		space.setHandicap(BooleanFromInt(json_data.getInt("handicap")));
					    		
					    		lotMatch.getParkingSpaces().add(space);
					    	}
			    			
			    			addFacultyParkingSpaces(lotMatch);
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
	
	private class GetDirectionsJSONListener implements GetJSONListener {
		
		public void onRemoteCallComplete(JSONArray jArray) {}
		
		public void onRemoteCallComplete(JSONObject jObject) {
			
			double lat;
			double lng;
			String polyLine;
			
			RouteStep step;
			JSONArray jArray = new JSONArray();
			JSONArray jLegs = new JSONArray();
			JSONArray jSteps = new JSONArray();
			
			JSONObject jRoute;
			JSONObject jLeg;
			JSONObject jStep;
			JSONObject jBounds;
			
			try {
				
	    		if (jObject != null) {
	    			
	    			if (jObject.length() > 0) {
	    				
	    				jArray = jObject.getJSONArray("routes");
	    				jRoute = jArray.getJSONObject(0);
	    				
	    				jLegs = jRoute.getJSONArray("legs");
	    				jLeg = jLegs.getJSONObject(0);
	    				jSteps = jLeg.getJSONArray("steps");
	    				jBounds = jRoute.getJSONObject("bounds");
	    				
			    		directions.setDistance(jLeg.getJSONObject("distance").getString("text"));
			    		directions.setDuration(jLeg.getJSONObject("duration").getString("text"));
			    		directions.setStartAddress(jLeg.getString("start_address"));
			    		directions.setEndAddress(jLeg.getString("end_address"));
			    		
			    		lat = Double.parseDouble(jBounds.getJSONObject("northeast").getString("lat"));
			            lng = Double.parseDouble(jBounds.getJSONObject("northeast").getString("lng"));
			    		directions.setBoundsNE(new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6)));
			    		
			    		lat = Double.parseDouble(jBounds.getJSONObject("southwest").getString("lat"));
			            lng = Double.parseDouble(jBounds.getJSONObject("southwest").getString("lng"));
			    		directions.setBoundsSW(new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6)));
			    		
			    		lat = Double.parseDouble(jLeg.getJSONObject("start_location").getString("lat"));
			            lng = Double.parseDouble(jLeg.getJSONObject("start_location").getString("lng"));
			    		directions.setStartLocation(new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6)));
			    		
			    		lat = Double.parseDouble(jLeg.getJSONObject("end_location").getString("lat"));
			            lng = Double.parseDouble(jLeg.getJSONObject("end_location").getString("lng"));
			    		directions.setEndLocation(new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6)));
			    		
			    		for(int index = 0; index < jSteps.length(); index++) {
			    			
			    			polyLine = "";
				    		jStep = jSteps.getJSONObject(index);
				    		
				    		step = new RouteStep();
				    		step.setDistance(jStep.getJSONObject("distance").getString("text"));
				    		step.setDuration(jStep.getJSONObject("duration").getString("text"));
				    		step.setInstructions(jStep.getString("html_instructions"));
				    		step.setTravelMode(jStep.getString("travel_mode"));
				    		
				    		lat = Double.parseDouble(jStep.getJSONObject("start_location").getString("lat"));
				    		lng = Double.parseDouble(jStep.getJSONObject("start_location").getString("lng"));
				    		step.setStartLocation(new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6)));
				    		
				    		lat = Double.parseDouble(jStep.getJSONObject("end_location").getString("lat"));
				    		lng = Double.parseDouble(jStep.getJSONObject("end_location").getString("lng"));
				    		step.setEndLocation(new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6)));
				    		
				    		polyLine = jStep.getJSONObject("polyline").getString("points");
				    		step.setPolyPoints(decodePoly(polyLine));
				    		
				    		directions.getSteps().add(step);
				    	}
			    		
			    		addDirections();
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		
	    		e.printStackTrace();
	    	}
		}
	}
	
	private List<GeoPoint> decodePoly(String encoded) {

	    List<GeoPoint> poly = new ArrayList<GeoPoint>();
	    
	    int index = 0;
	    int len = encoded.length();
	    int lat = 0;
	    int lng = 0;

	    while (index < len) {
	    	
	        int b;
	        int shift = 0;
	        int result = 0;
	        
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),
	             (int) (((double) lng / 1E5) * 1E6));
	        
	        poly.add(p);
	    }

	    return poly;
	}
	
	public class MyLocationListener implements LocationListener	{
		
		public void onLocationChanged(Location location) {}

	    public void onStatusChanged(String provider, int status, Bundle extras) {}

	    public void onProviderEnabled(String provider) {}

	    public void onProviderDisabled(String provider) {}
	}
	  
	private boolean BooleanFromInt(int value) {
		
		return (value == 1 ? true : false);
	}
}
