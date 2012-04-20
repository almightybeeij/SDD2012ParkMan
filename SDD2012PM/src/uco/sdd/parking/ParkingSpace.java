/*
 * ParkingSpace Class
 * 
 * Version 1.0.0
 * 
 * Author: Cory Wilson
 * Last Updated: 04/19/2012
 * Last Updated By: Cory Wilson
 * 
 * Represents a parking space that is managed by the system.
 * Holds data that is used to overlay a parking space onto
 * the ViewParkingMapActivity.
 */

package uco.sdd.parking;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

public class ParkingSpace {
	
	private int spaceId;
	private boolean isAvailable;
	private boolean isHandicap;
	private String clientEmail;
	
	private ArrayList<GeoPoint> corners;

	public ParkingSpace() {
		
		spaceId = 0;
		isAvailable = false;
		isHandicap = false;
		clientEmail = "";
		
		corners = new ArrayList<GeoPoint>();
	}

	public int getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(int spaceId) {
		this.spaceId = spaceId;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public boolean isHandicap() {
		return isHandicap;
	}

	public void setHandicap(boolean isHandicap) {
		this.isHandicap = isHandicap;
	}

	public String getClientEmail() {
		return clientEmail;
	}

	public void setClientEmail(String clientEmail) {
		this.clientEmail = clientEmail;
	}

	public ArrayList<GeoPoint> getCorners() {
		return corners;
	}

	public void setCorners(ArrayList<GeoPoint> corners) {
		this.corners = corners;
	}
	
	public void addCorner(String coordinates) {
		
		String[] coordinate;
		coordinate = coordinates.split(",");
		
		double lat = Double.parseDouble(coordinate[1]);
        double lng = Double.parseDouble(coordinate[0]);
 
        GeoPoint p = new GeoPoint(
            (int) (lat * 1E6), 
            (int) (lng * 1E6));
        
        corners.add(p);
	}

	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		
		result = prime * result + spaceId;
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		ParkingSpace other = (ParkingSpace) obj;
		
		if (spaceId != other.spaceId) {
			return false;
		}
		
		return true;
	}
	
}
