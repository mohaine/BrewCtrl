package com.mohaine.brewcontroller.android;

import roboguice.activity.RoboActivity;
import android.os.Bundle;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.BrewControllerStartup;

public class BrewCtrlAndroidActivity extends RoboActivity {
	@Inject
	BrewControllerStartup bc;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		bc.startup();
	}

}