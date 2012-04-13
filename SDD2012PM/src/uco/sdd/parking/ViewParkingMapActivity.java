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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;

public class ViewParkingMapActivity extends MapActivity {

	private MapView mapView;
	private MapController mc;
	private List<Overlay> mapOverlays;
	private List<ParkingLot> studentParkingLots;
	private List<ParkingLot> facultyParkingLots;
	private Projection projection;
	
	@Override
	protected boolean isRouteDisplayed() {
		
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.viewmap_layout);
	    
	    studentParkingLots = new ArrayList<ParkingLot>();
	    facultyParkingLots = new ArrayList<ParkingLot>();
	    
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
        
        selectParkingLotCoordinates(true, false);
        selectParkingLotCoordinates(false, true);
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
    	
    	dac.executeSelect();
	}
	
	public class ParkingLotStudentOverlay extends Overlay
	{
		private boolean isInitialized = false;
		
		private Paint fillPaint;
		private Paint strokePaint;
		private Path lotPath;
    	
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
	    		
	    		lotPath = new Path();
	    		
	    		Point coordInitial = new Point();
	    		Point coordPoint = new Point();
	    		
	    		for (ParkingLot lot : studentParkingLots)
	    		{
	    			int count = 0;
	    			
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
	    		}
			}
		}
	}
	
	public class ParkingLotFacultyOverlay extends Overlay
	{
		private boolean isInitialized = false;
		
		private Paint fillPaint;
		private Paint strokePaint;
		private Path lotPath;
    	
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
	    		
	    		lotPath = new Path();
	    		
	    		Point coordInitial = new Point();
	    		Point coordPoint = new Point();
	    		
	    		for (ParkingLot lot : facultyParkingLots)
	    		{
	    			int count = 0;
	    			
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
	    		}
			}
		}
	}
	
	private class StudentLotCoordinatesJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			    	
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
				    		coordinatePair = json_data.getString("coordinates");
				    		coordinates = coordinatePair.split(",");
				    		lot.addCoordinate(coordinates[1], coordinates[0]);
				    	}
	    				
	    				studentParkingLots.add(lot);
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
	}
	 
	private class FacultyLotCoordinatesJSONListener implements GetJSONListener
	{
		public void onRemoteCallComplete(JSONArray jArray) {
			    	
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
				    		coordinatePair = json_data.getString("coordinates");
				    		coordinates = coordinatePair.split(",");
				    		lot.addCoordinate(coordinates[1], coordinates[0]);
				    	}
	    				
	    				facultyParkingLots.add(lot);
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
	}
}
