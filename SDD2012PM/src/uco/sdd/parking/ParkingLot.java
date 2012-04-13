package uco.sdd.parking;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

public class ParkingLot {
	
	private int lotId;
	private boolean isStudent;
	private boolean isFaculty;
	
	private ArrayList<GeoPoint> coordinates;
	private ArrayList<GeoPoint> boundary;
	
	public ParkingLot()
	{
		coordinates = new ArrayList<GeoPoint>();
		boundary = new ArrayList<GeoPoint>();
		
		lotId = 0;
		isStudent = false;
		isFaculty = false;
	}

	public int getLotId() {
		return lotId;
	}

	public void setLotId(int lotId) {
		this.lotId = lotId;
	}

	public boolean isStudent() {
		return isStudent;
	}

	public void setStudent(boolean isStudent) {
		this.isStudent = isStudent;
	}

	public boolean isFaculty() {
		return isFaculty;
	}

	public void setFaculty(boolean isFaculty) {
		this.isFaculty = isFaculty;
	}

	public ArrayList<GeoPoint> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(ArrayList<GeoPoint> coordinates) {
		this.coordinates = coordinates;
	}

	public ArrayList<GeoPoint> getBoundary() {
		return boundary;
	}

	public void setBoundary(ArrayList<GeoPoint> boundary) {
		this.boundary = boundary;
	}
	
	public void addCoordinate(String latitude, String longitude)
	{
		double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);
 
        GeoPoint p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
        
        coordinates.add(p);
	}
	
	public void addBoundary(String latitude, String longitude)
	{
		double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);
 
        GeoPoint p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
        
        boundary.add(p);
	}
}