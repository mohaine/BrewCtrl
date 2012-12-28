package com.mohaine.brewcontroller.web.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.display.BreweryDisplay.BreweryDisplayDrawer;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.page.Overview.OverviewDisplay;
import com.mohaine.brewcontroller.web.client.net.ControllerHardwareJsonGwt;
import com.mohaine.brewcontroller.web.client.page.OverviewDisplayGwt;

public class WebCtrlModule extends AbstractGinModule {

	@Override
	protected void configure() {
		bind(EventBus.class).asEagerSingleton();

		bind(BreweryDisplayDrawer.class).to(BreweryDisplayDrawerGwt.class).asEagerSingleton();
		bind(ControllerHardware.class).to(ControllerHardwareJsonGwt.class).asEagerSingleton();
		bind(OverviewDisplay.class).to(OverviewDisplayGwt.class).asEagerSingleton();

	}

}
