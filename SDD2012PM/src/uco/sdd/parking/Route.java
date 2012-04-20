/*
 * Route Class
 * 
 * Version 1.0.0
 * 
 * Author: Cory Wilson
 * Last Updated: 04/19/2012
 * Last Updated By: Cory Wilson
 * 
 * Represents a route or set of directions returned by the
 * Google Directions API.
 */

package uco.sdd.parking;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

public class Route {

	private GeoPoint boundsNE;
	private GeoPoint boundsSW;
	private GeoPoint startLocation;
	private GeoPoint endLocation;
	
	private String distance;
	private String duration;
	private String startAddress;
	private String endAddress;
	
	private ArrayList<RouteStep> steps;
	
	public Route() {
		steps = new ArrayList<RouteStep>();
	}

	public GeoPoint getBoundsNE() {
		return boundsNE;
	}

	public void setBoundsNE(GeoPoint boundsNE) {
		this.boundsNE = boundsNE;
	}

	public GeoPoint getBoundsSW() {
		return boundsSW;
	}

	public void setBoundsSW(GeoPoint boundsSW) {
		this.boundsSW = boundsSW;
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

	public String getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}

	public String getEndAddress() {
		return endAddress;
	}

	public void setEndAddress(String endAddress) {
		this.endAddress = endAddress;
	}

	public ArrayList<RouteStep> getSteps() {
		return steps;
	}

	public void setSteps(ArrayList<RouteStep> steps) {
		this.steps = steps;
	}
}
