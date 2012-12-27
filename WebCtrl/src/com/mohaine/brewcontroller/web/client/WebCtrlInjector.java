package com.mohaine.brewcontroller.web.client;

import com.google.gwt.inject.client.Ginjector;
import com.mohaine.brewcontroller.client.display.BreweryDisplay;

public interface WebCtrlInjector extends Ginjector {
	BreweryDisplay getBreweryDisplay();
}
