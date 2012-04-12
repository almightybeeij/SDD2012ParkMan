package uco.sdd.parking;

import java.util.List;

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
	private Projection projection;
	
	@Override
	protected boolean isRouteDisplayed() {
		
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.viewmap_layout);
	    
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
	}
	
	public class ParkingLotStudentOverlay extends Overlay
	{
		private boolean isInitialized = false;
		
		private Paint mPaint;
		private Path mPath;
		
		private GeoPoint gP1;
    	private GeoPoint gP2;
    	private GeoPoint gP3;
    	private GeoPoint gP4;
    	
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
            
            gP1 = new GeoPoint((int)(35.654023 * 1E6),(int)(-97.473898 * 1E6));
            gP2 = new GeoPoint((int)(35.654071 * 1E6),(int)(-97.473902 * 1E6));
            gP3 = new GeoPoint((int)(35.654071 * 1E6),(int)(-97.473931 * 1E6));
            gP4 = new GeoPoint((int)(35.654023 * 1E6),(int)(-97.473926 * 1E6));
		}
		
		public void draw(Canvas canvas, MapView mapv, boolean shadow) {
    		
			super.draw(canvas, mapv, shadow);
    		
    		if (!isInitialized) {
    			this.init();
    			this.isInitialized = true;
    		}
    		
    		Point p1 = new Point();
            Point p2 = new Point();
            Point p3 = new Point();
            Point p4 = new Point();
            
            mPath = new Path();
            
    		projection.toPixels(gP1, p1);
            projection.toPixels(gP2, p2);
            projection.toPixels(gP3, p3);
            projection.toPixels(gP4, p4);

            mPath.moveTo(p1.x, p1.y);
            mPath.lineTo(p2.x,p2.y);
            mPath.lineTo(p3.x, p3.y);
            mPath.lineTo(p4.x, p4.y);
            
    		canvas.drawPath(mPath, mPaint);
		}
	}
}
