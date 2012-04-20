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

import android.app.Application;

public class ParkingApplication extends Application {

	private int resultCloseAll;
	private int resultUpdate;
	
	public int getResultCloseAll() {		
		return resultCloseAll;
	}

	public void setResultCloseAll(int resultCloseAll) {		
		this.resultCloseAll = resultCloseAll;
	}
	
	public int getResultUpdate() {
		return resultUpdate;
	}

	public void setResultUpdate(int resultUpdate) {
		this.resultUpdate = resultUpdate;
	}

	public boolean BooleanFromInt(int value) {
		
		return (value == 1 ? true : false);
	}
}
