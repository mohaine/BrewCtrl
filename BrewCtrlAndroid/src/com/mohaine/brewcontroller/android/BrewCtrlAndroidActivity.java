package com.mohaine.brewcontroller.android;

import roboguice.activity.RoboActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.BrewControllerStartup;
import com.mohaine.brewcontroller.ControllerGui;

public class BrewCtrlAndroidActivity extends RoboActivity {
	private static final String TAG = "BrewCtrlAndroidActivity";
	@Inject
	BrewControllerStartup bc;
	@Inject
	ControllerGui gui;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "BrewCtrlAndroidActivity.onCreate()");
		setContentView(R.layout.main);
		bc.startup();
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		Log.v(TAG, "BrewCtrlAndroidActivity.onContentChanged()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "BrewCtrlAndroidActivity.onPause()");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.v(TAG, "BrewCtrlAndroidActivity.onRestart()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "BrewCtrlAndroidActivity.onResume()");
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		Log.v(TAG, "BrewCtrlAndroidActivity.setContentView()");
	}

}