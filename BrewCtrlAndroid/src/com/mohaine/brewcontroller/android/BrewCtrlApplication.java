package com.mohaine.brewcontroller.android;

import java.util.List;

import roboguice.application.RoboApplication;

import com.google.inject.Module;

public class BrewCtrlApplication extends RoboApplication {
	protected void addApplicationModules(List<Module> modules) {
		modules.add(new BrewControllerAndroidModule());
	}
}
