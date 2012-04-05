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

import com.google.inject.AbstractModule;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.ControllerImpl;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.bd.BreweryDisplay.BreweryDisplayDrawer;
import com.mohaine.brewcontroller.page.MainMenu.MainMenuDisplay;
import com.mohaine.brewcontroller.page.Overview.OverviewDisplay;
import com.mohaine.brewcontroller.page.Setup.SetupDisplay;
import com.mohaine.brewcontroller.serial.RxTxComm;
import com.mohaine.brewcontroller.serial.SerialConnection;
import com.mohaine.brewcontroller.serial.SerialHardwareComm;
import com.mohaine.brewcontroller.swing.bd.BreweryDisplayDrawerSwing;
import com.mohaine.brewcontroller.swing.page.MainMenuDisplaySwing;
import com.mohaine.brewcontroller.swing.page.OverviewDisplaySwing;
import com.mohaine.brewcontroller.swing.page.SetupDisplaySwing;
import com.mohaine.event.bus.EventBus;

public class BrewControllerSwingModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ControllerGui.class).to(SwingControllerInterface.class).asEagerSingleton();
		bind(Controller.class).to(ControllerImpl.class).asEagerSingleton();

		bind(MainMenuDisplay.class).to(MainMenuDisplaySwing.class);
		bind(OverviewDisplay.class).to(OverviewDisplaySwing.class);
		bind(SetupDisplay.class).to(SetupDisplaySwing.class);
		bind(Hardware.class).to(SerialHardwareComm.class).asEagerSingleton();

		bind(SerialConnection.class).to(RxTxComm.class).asEagerSingleton();
		// bind(SerialConnection.class).to(MockComm.class).asEagerSingleton();

		bind(EventBus.class).asEagerSingleton();

		bind(BreweryDisplayDrawer.class).to(BreweryDisplayDrawerSwing.class);

	}

}