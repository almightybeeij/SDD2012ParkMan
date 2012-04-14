package uco.sdd.parking;

import java.util.ArrayList;

import android.graphics.Path;

import com.google.android.maps.GeoPoint;

public class ParkingLot {
	
	private int lotId;
	private boolean isStudent;
	private boolean isFaculty;
	private String parkingType;
	
	private ArrayList<GeoPoint> coordinates;
	private ArrayList<GeoPoint> boundaries;
	private ArrayList<ParkingSpace> parkingSpaces;
	private Path lotPath;
	
	public ParkingLot()
	{
		coordinates = new ArrayList<GeoPoint>();
		boundaries = new ArrayList<GeoPoint>();
		parkingSpaces = new ArrayList<ParkingSpace>();
		lotPath = new Path();
		
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

	public String getParkingType() {
		return parkingType;
	}

	public void setParkingType(String parkingType) {
		this.parkingType = parkingType;
	}

	public ArrayList<GeoPoint> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(ArrayList<GeoPoint> coordinates) {
		this.coordinates = coordinates;
	}

	public ArrayList<GeoPoint> getBoundaries() {
		return boundaries;
	}

	public void setBoundaries(ArrayList<GeoPoint> boundary) {
		this.boundaries = boundary;
	}
	
	public ArrayList<ParkingSpace> getParkingSpaces() {
		return parkingSpaces;
	}

	public void setParkingSpaces(ArrayList<ParkingSpace> parkingSpaces) {
		this.parkingSpaces = parkingSpaces;
	}

	public Path getLotPath() {
		return lotPath;
	}

	public void setLotPath(Path lotPath) {
		this.lotPath = lotPath;
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
        
        boundaries.add(p);
	}

	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		result = prime * result + lotId;
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		ParkingLot other = (ParkingLot) obj;
		
		if (lotId != other.lotId)
			return false;
		
		return true;
	}
	
	public void addBoundary(String coordinates)
	{
		String[] coordinate;
		coordinate = coordinates.split(",");
		
		double lat = Double.parseDouble(coordinate[1]);
        double lng = Double.parseDouble(coordinate[0]);
 
        GeoPoint p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
        
        boundaries.add(p);
	}
	
	public void clearPath()
	{
		this.lotPath = new Path();
	}
}
