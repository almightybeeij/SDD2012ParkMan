package uco.sdd.parking;

import android.app.Application;

public class ParkingApplication extends Application {

	private int resultCloseAll;

	public int getResultCloseAll() {
		return resultCloseAll;
	}

	public void setResultCloseAll(int resultCloseAll) {
		this.resultCloseAll = resultCloseAll;
	}
}
