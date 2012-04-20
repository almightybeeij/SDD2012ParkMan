/*
 * RouteStep Class
 * 
 * Version 1.0.0
 * 
 * Author: Cory Wilson
 * Last Updated: 04/19/2012
 * Last Updated By: Cory Wilson
 * 
 * Represents a route step or direction step returned by the
 * Google Directions API. Data is used to draw or overlay the
 * directions onto a MapView.
 */

package uco.sdd.parking;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;

public class RouteStep {

	private String instructions;
	private String distance;
	private String duration;
	private String travelMode;
	
	private List<GeoPoint> polyPoints;
	private GeoPoint startLocation;
	private GeoPoint endLocation;
	
	public RouteStep() {
		polyPoints = new ArrayList<GeoPoint>();
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getTravelMode() {
		return travelMode;
	}

	public void setTravelMode(String travelMode) {
		this.travelMode = travelMode;
	}

	public List<GeoPoint> getPolyPoints() {
		return polyPoints;
	}

	public void setPolyPoints(List<GeoPoint> polyPoints) {
		this.polyPoints = polyPoints;
	}

	public GeoPoint getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(GeoPoint startLocation) {
		this.startLocation = startLocation;
	}

	public GeoPoint getEndLocation() {
		return endLocation;
	}

	public void setEndLocation(GeoPoint endLocation) {
		this.endLocation = endLocation;
	}
}
