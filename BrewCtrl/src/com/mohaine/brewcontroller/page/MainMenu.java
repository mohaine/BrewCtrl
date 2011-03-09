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

package com.mohaine.brewcontroller.page;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mohaine.brewcontroller.BrewPrefs;
import com.mohaine.brewcontroller.ClickEvent;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.Converter;
import com.mohaine.brewcontroller.UnitConversion;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.event.ClickHandler;

public class MainMenu extends BasePage {

	public interface MainMenuDisplay {
		void addClickable(String name, ClickHandler ch);
	}

	private MainMenuDisplay display;

	@Inject
	public MainMenu(MainMenuDisplay display, final Provider<Setup> providerSetup, final Provider<StepEditor> providerStepEditor, final ControllerGui controllerGui, final Controller controller,
			final BrewPrefs prefs, final UnitConversion conversion) {
		super();
		this.display = display;

		final Converter<Double, Double> tempDisplayConveter = conversion.getTempDisplayConveter();

		display.addClickable("Setup", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controllerGui.displayPage(providerSetup.get());
			}
		});
		display.addClickable("Run", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controllerGui.displayPage(providerStepEditor.get());
			}
		});
		display.addClickable("Heater", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<HeaterStep> steps = new ArrayList<HeaterStep>();
				steps.add(new HeaterStep("Heater", 0, 0));
				controller.setSteps(steps);
				controller.setSelectedStep(steps.get(0));
				controllerGui.displayPage(providerStepEditor.get());
			}
		});
		display.addClickable("1 Step Mash", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<HeaterStep> steps = new ArrayList<HeaterStep>();
				steps.add(new HeaterStep("Mash In", tempDisplayConveter.convertTo(165.0), 0));
				steps.add(new HeaterStep("Mash", tempDisplayConveter.convertTo(153.0), 60 * 60 * 1000));
				steps.add(new HeaterStep("Mash Out", tempDisplayConveter.convertTo(170.0), 0));
				controller.setSteps(steps);
				controller.setSelectedStep(steps.get(0));
				controllerGui.displayPage(providerStepEditor.get());
			}
		});
		display.addClickable("3 Step Mash", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<HeaterStep> steps = new ArrayList<HeaterStep>();
				steps.add(new HeaterStep("Mash In", tempDisplayConveter.convertTo(165.0), 0));
				steps.add(new HeaterStep("Mash", 40, 30 * 60 * 1000));
				steps.add(new HeaterStep("Mash", 60, 30 * 60 * 1000));
				steps.add(new HeaterStep("Mash", 70, 30 * 60 * 1000));
				steps.add(new HeaterStep("Mash Out", tempDisplayConveter.convertTo(170.0), 0));
				controller.setSteps(steps);
				controller.setSelectedStep(steps.get(0));
				controllerGui.displayPage(providerStepEditor.get());
			}
		});
	}

	@Override
	public String getTitle() {
		return "Main Menu";
	}

	@Override
	public Object getWidget() {
		return display;
	}
}
