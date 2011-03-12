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
import com.mohaine.brewcontroller.page.MainMenu.MainMenuDisplay;
import com.mohaine.brewcontroller.page.Setup.SetupDisplay;
import com.mohaine.brewcontroller.page.StepEditor.StepEditorDisplay;
import com.mohaine.brewcontroller.serial.SerialHardwareComm;
import com.mohaine.brewcontroller.swing.page.MainMenuDisplaySwing;
import com.mohaine.brewcontroller.swing.page.SetupDisplaySwing;
import com.mohaine.brewcontroller.swing.page.StepEditorDisplaySwing;
import com.mohaine.event.bus.EventBus;

public class BrewControllerSwingModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ControllerGui.class).to(SwingControllerInterface.class).asEagerSingleton();
		bind(Controller.class).to(ControllerImpl.class).asEagerSingleton();

		bind(MainMenuDisplay.class).to(MainMenuDisplaySwing.class);
		bind(SetupDisplay.class).to(SetupDisplaySwing.class);
		bind(StepEditorDisplay.class).to(StepEditorDisplaySwing.class);
		bind(Hardware.class).to(SerialHardwareComm.class).asEagerSingleton();
		// bind(Hardware.class).to(HardwareMock.class).asEagerSingleton();

		bind(EventBus.class).asEagerSingleton();
	}

}