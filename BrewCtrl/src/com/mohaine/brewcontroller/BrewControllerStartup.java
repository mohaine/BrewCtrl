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

package com.mohaine.brewcontroller;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.page.StepEditor;

public class BrewControllerStartup {
	private final ControllerGui ci;
	private final DisplayPage startupPage;

	@Inject
	public BrewControllerStartup(ControllerGui ci, StepEditor startupPage, final Controller controller, final UnitConversion conversion) {
		super();
		this.ci = ci;
		this.startupPage = startupPage;

		final Converter<Double, Double> tempDisplayConveter = conversion.getTempDisplayConveter();
		List<HeaterStep> steps = new ArrayList<HeaterStep>();
		steps.add(new HeaterStep("Mash In", tempDisplayConveter.convertTo(165.0), 0));
		steps.add(new HeaterStep("Mash", tempDisplayConveter.convertTo(153.0), 60 * 60 * 1000));
		steps.add(new HeaterStep("Mash Out", tempDisplayConveter.convertTo(170.0), 0));
		controller.setSteps(steps);
		controller.setSelectedStep(steps.get(0));
	}

	public void startup() {

		ci.init();
		ci.displayPage(startupPage);
	}

}
