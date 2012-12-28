/*
    Copyright 2009-2011 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.mohaine.brewcontroller.swing;

import java.io.File;

import com.google.inject.AbstractModule;
import com.mohaine.brewcontroller.BrewJsonConverterRefection;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.display.BreweryDisplay.BreweryDisplayDrawer;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.net.BrewJsonConverter;
import com.mohaine.brewcontroller.client.page.Overview.OverviewDisplay;
import com.mohaine.brewcontroller.net.ControllerHardwareJsonUrlRequest;
import com.mohaine.brewcontroller.swing.bd.BreweryDisplayDrawerSwing;
import com.mohaine.brewcontroller.swing.page.OverviewDisplaySwing;

public class BrewControllerSwingModule extends AbstractModule {

	private File configFile;

	public BrewControllerSwingModule(File configFile) {
		this.configFile = configFile;
	}

	@Override
	protected void configure() {

		bind(ConfigurationLoader.class).toInstance(new FileConfigurationLoader(configFile));

		bind(BrewJsonConverter.class).to(BrewJsonConverterRefection.class).asEagerSingleton();

		bind(ControllerGui.class).to(SwingControllerInterface.class).asEagerSingleton();
		bind(ControllerHardware.class).to(ControllerHardwareJsonUrlRequest.class).asEagerSingleton();

		bind(OverviewDisplay.class).to(OverviewDisplaySwing.class);

		bind(EventBus.class).asEagerSingleton();

		bind(BreweryDisplayDrawer.class).to(BreweryDisplayDrawerSwing.class);

	}

}