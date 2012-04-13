package uco.sdd.parking;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

public class ParkingSpace {
	
	private int spaceId;
	private boolean isAvailable;
	private boolean isHandicap;
	private String clientEmail;
	
	private ArrayList<GeoPoint> corners;

	public ParkingSpace()
	{
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
}
