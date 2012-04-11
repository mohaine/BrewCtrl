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
import com.mohaine.brewcontroller.ClickEvent;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.Converter;
import com.mohaine.brewcontroller.UnitConversion;
import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.event.ClickHandler;

public class MainMenu extends BasePage {

	public interface MainMenuDisplay {
		void addClickable(String name, ClickHandler ch);

		void init();
	}

	private MainMenuDisplay display;
	private Controller controller;
	private Converter<Double, Double> tempDisplayConveter;

	@Inject
	public MainMenu(MainMenuDisplay display, final ControllerGui controllerGui, final Controller controller, final Provider<Overview> providerOverview, final UnitConversion conversion) {
		super();
		this.display = display;
		this.controller = controller;

		display.init();

		tempDisplayConveter = conversion.getTempDisplayConveter();

		display.addClickable("Overview", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controllerGui.displayPage(providerOverview.get());
			}
		});

		display.addClickable("1 Step Mash", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<HeaterStep> steps = new ArrayList<HeaterStep>();

				steps.add(createMashStep("Mash In", tempDisplayConveter.convertTo(165.0), 0));
				steps.add(createMashStep("Mash", tempDisplayConveter.convertTo(153.0), 60 * 60 * 1000));
				steps.add(createMashStep("Mash Out", tempDisplayConveter.convertTo(170.0), 0));
				controller.setSteps(steps);
				controller.setSelectedStep(steps.get(0));
				controllerGui.displayPage(providerOverview.get());
			}

		});
		display.addClickable("3 Step Mash", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				List<HeaterStep> steps = new ArrayList<HeaterStep>();
				steps.add(createMashStep("Mash In", tempDisplayConveter.convertTo(165.0), 0));
				steps.add(createMashStep("Mash", 40, 30 * 60 * 1000));
				steps.add(createMashStep("Mash", 60, 30 * 60 * 1000));
				steps.add(createMashStep("Mash", 70, 30 * 60 * 1000));
				steps.add(createMashStep("Mash Out", tempDisplayConveter.convertTo(170.0), 0));
				controller.setSteps(steps);
				controller.setSelectedStep(steps.get(0));
				controllerGui.displayPage(providerOverview.get());
			}
		});
	}

	private HeaterStep createMashStep(String name, double targetTunTempC, long msAtStep) {
		HeaterStep step = controller.createManualStep(name);
		step.setStepTime(msAtStep);
		BreweryLayout brewLayout = controller.getLayout();

		// Map HLT Loop pump to HTL Sensor
		List<Pump> pumps = brewLayout.getPumps();
		for (Pump pump : pumps) {
			if (Pump.HLT_LOOP.equals(pump.getName())) {
				ControlPoint controlPointForPin = step.getControlPointForPin(pump.getPin());
				if (controlPointForPin != null) {

					Tank tun = brewLayout.getTank(Tank.TUN_NAME);
					if (tun != null) {
						Sensor sensor = tun.getSensor();
						if (sensor != null) {
							controlPointForPin.setAutomaticControl(true);
							controlPointForPin.setTempSensorAddress(sensor.getAddress());
							controlPointForPin.setTargetTemp(targetTunTempC);
						}
					}

				}
			}
		}

		Tank htl = brewLayout.getTank(Tank.HLT_NAME);
		if (htl != null) {
			HeatElement heater = htl.getHeater();
			ControlPoint controlPointForPin = step.getControlPointForPin(heater.getPin());

			if (heater != null) {
				Sensor sensor = htl.getSensor();
				if (sensor != null) {
					controlPointForPin.setAutomaticControl(true);
					controlPointForPin.setTempSensorAddress(sensor.getAddress());
					controlPointForPin.setTargetTemp(targetTunTempC + tempDisplayConveter.convertTo(37.0));
				}
			}
		}

		return step;
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
