/*
 * ParkingApplication Class
 * 
 * Version 1.0.0
 * 
 * Author: Cory Wilson
 * Last Updated: 04/19/2012
 * Last Updated By: Cory Wilson
 * 
 * Extends the application class and represents the parking
 * management application.  Used to hold global application
 * properties.
 * 
 * Properties are globally accessed by calling:
 * ((ParkingApplication)getApplication()).propertyName
 */

package uco.sdd.parking;

import java.util.ArrayList;

import android.app.Application;

public class ParkingApplication extends Application {

	private int resultCloseAll;
	private int resultClose;
	private int resultUpdate;
	
	private String userEmail;
	private String userFirstName;
	private String userLastName;
	
	private ArrayList<RouteStep> steps;
	
	public void onCreate()
	{
		super.onCreate();
		
		steps = new ArrayList<RouteStep>();
	}
	
	public int getResultCloseAll() {		
		return resultCloseAll;
	}

	public void setResultCloseAll(int resultCloseAll) {		
		this.resultCloseAll = resultCloseAll;
	}
	
	public int getResultClose() {
		return resultClose;
	}

	public void setResultClose(int resultClose) {
		this.resultClose = resultClose;
	}

	public int getResultUpdate() {
		return resultUpdate;
	}

	public void setResultUpdate(int resultUpdate) {
		this.resultUpdate = resultUpdate;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserFirstName() {
		return userFirstName;
	}

	public void setUserFirstName(String userFirstName) {
		this.userFirstName = userFirstName;
	}

	public String getUserLastName() {
		return userLastName;
	}

	public void setUserLastName(String userLastName) {
		this.userLastName = userLastName;
	}

	public ArrayList<RouteStep> getSteps() {
		return steps;
	}

	public void setSteps(ArrayList<RouteStep> steps) {
		this.steps = steps;
	}

	public boolean BooleanFromInt(int value) {
		
		return (value == 1 ? true : false);
	}
}
