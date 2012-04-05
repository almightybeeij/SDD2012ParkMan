package uco.sdd.parking;

import uco.sdd.utility.*;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class LogInActivity extends Activity
{
	private HTTPDataAccess dac;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }
    
    public void logIn(View view)
    {
    
    }
}
