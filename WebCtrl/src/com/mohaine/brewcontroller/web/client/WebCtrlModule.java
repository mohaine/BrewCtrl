package com.mohaine.brewcontroller.web.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.mohaine.brewcontroller.BrewJsonConverterRefection;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.display.BreweryDisplay.BreweryDisplayDrawer;
import com.mohaine.brewcontroller.client.net.BrewJsonConverter;
import com.mohaine.brewcontroller.web.client.net.ControllerHardwareJsonGwt;

public class WebCtrlModule extends AbstractGinModule{

	@Override
	protected void configure() {
		
		
		bind(BreweryDisplayDrawer.class).to(BreweryDisplayDrawerGwt.class).asEagerSingleton();
		bind(ControllerHardware.class).to(ControllerHardwareJsonGwt.class).asEagerSingleton();
		
		//TODO Make a generator for this
		bind(BrewJsonConverter.class).to(BrewJsonConverterGwt.class).asEagerSingleton();

	}

}
