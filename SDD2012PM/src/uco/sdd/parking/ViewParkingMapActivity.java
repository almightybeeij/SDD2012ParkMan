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
	private List<ParkingLot> parkingLots;
	private Projection projection;
	
	@Override
	protected boolean isRouteDisplayed() {
		
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.viewmap_layout);
	    
	    parkingLots = new ArrayList<ParkingLot>();
	    
	    mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        projection = mapView.getProjection();
	    mc = mapView.getController();
	    
	    mapOverlays = mapView.getOverlays();  
	    mapOverlays.add(new ParkingLotStudentOverlay());
	    
        String coordinates[] = {"35.654108", "-97.473863"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
 
        GeoPoint p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
 
        mc.animateTo(p);
        mc.setZoom(19); 
        
        HTTPDataAccess dac = new HTTPDataAccess(this,
    			getString(R.string.url_select), new StudentLotCoordinatesJSONListener());
	    
	    dac.setStatement(getString(R.string.viewparking_smt_lotstudent));
	    dac.setTypes(getString(R.string.viewparking_smt_lotstudent_types));
    	dac.addNewBindVariable("One", "1", false);
    	
    	dac.executeSelect();
	}
	
	public class ParkingLotStudentOverlay extends Overlay
	{
		private boolean isInitialized = false;
		
		private Paint mPaint;
		private Path mPath;
    	
		private void init()
		{
			mPaint = new Paint();
            mPaint.setDither(true);
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(2);
            mPaint.setAlpha(200);
		}
		
		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
    		
			super.draw(canvas, mapv, shadow);
    		
    		if (!isInitialized) {
    			this.init();
    			this.isInitialized = true;
    		}
    		
    		mPath = new Path();
    		Point coordPoint = new Point();
    		
    		for (ParkingLot lot : parkingLots)
    		{
    			int count = 0;
    			
    			GeoPoint initial = lot.getCoordinates().get(0);
    			projection.toPixels(initial, coordPoint);
    			mPath.moveTo(coordPoint.x, coordPoint.y);
    			
    			for (GeoPoint coordinate : lot.getCoordinates())
    			{
    				count++;
    				
    				projection.toPixels(coordinate, coordPoint);
    				
    				if (count > 1)
    				{
    					mPath.lineTo(coordPoint.x, coordPoint.y);
    					mPath.moveTo(coordPoint.x, coordPoint.y);
    				}
    			}
    		}
    		
    		canvas.drawPath(mPath, mPaint);
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
				    		lot.addCoordinate(coordinates[0], coordinates[1]);
				    	}
	    				
	    				parkingLots.add(lot);
	    			}
	    		}
	    	}
	    	catch (JSONException e)	{
	    		e.printStackTrace();
	    	}
		}
	}
	    			
}
