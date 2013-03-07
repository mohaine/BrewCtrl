/*
    Copyright 2009-2013 Michael Graessle

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

package com.mohaine.brewcontroller;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.DisplayPage;
import com.mohaine.brewcontroller.client.UnitConversion;
import com.mohaine.brewcontroller.client.page.Overview;

public class BrewControllerStartup {
	private final ControllerGui ci;
	private final DisplayPage startupPage;

	private boolean started = false;

	@Inject
	public BrewControllerStartup(ControllerGui ci, Overview startupPage, final ControllerHardware controller, final UnitConversion conversion) {
		super();
		this.ci = ci;
		this.startupPage = startupPage;
	}

	public void startup() {
		if (!started) {
			ci.init();
			ci.displayPage(startupPage);
			started = true;
		}
	}

}
