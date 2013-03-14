package com.mohaine.brewcontroller.web.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.FormatterDefaults;
import com.mohaine.brewcontroller.client.display.BreweryDisplayDrawer.DrawerCanvas;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.page.Overview.OverviewDisplay;
import com.mohaine.brewcontroller.web.client.net.ControllerHardwareJsonGwt;
import com.mohaine.brewcontroller.web.client.page.OverviewDisplayGwt;

public class WebCtrlModule extends AbstractGinModule {

	@Override
	protected void configure() {
		bind(EventBus.class).asEagerSingleton();
		bind(FormatterDefaults.class).to(FormatterDefaultsGwt.class).asEagerSingleton();
		bind(DrawerCanvas.class).to(BreweryDisplayDrawerGwt.class).asEagerSingleton();
		bind(ControllerHardware.class).to(ControllerHardwareJsonGwt.class).asEagerSingleton();
		bind(OverviewDisplay.class).to(OverviewDisplayGwt.class).asEagerSingleton();

	}

}
