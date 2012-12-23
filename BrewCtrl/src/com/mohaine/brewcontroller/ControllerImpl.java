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
import com.mohaine.brewcontroller.bean.HardwareControl.Mode;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.event.BreweryComponentChangeEvent;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.brewcontroller.event.StepModifyEventHandler;
import com.mohaine.brewcontroller.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.layout.BreweryComponent;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.event.bus.EventBus;

public class ControllerImpl {

	private List<HeaterStep> steps = new ArrayList<HeaterStep>();
	private List<BrewHardwareControl> brewHardwareControls = new ArrayList<BrewHardwareControl>();
	private HeaterStep selectedStep = null;;
	private Mode mode = Mode.OFF;
	private EventBus eventBus;
	private ControllerHardware hardware;
	private BreweryLayout brewLayout;
	private Configuration configuration;

	@Inject
	public ControllerImpl(EventBus eventBusp, ConfigurationLoader configurationLoader) throws Exception {
		super();
		this.eventBus = eventBusp;
		this.configuration = configurationLoader.getConfiguration();

		initLayout();

		selectedStep = steps.get(0);
		updateHardware();
		eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {
			@Override
			public void onStepChange(HeaterStep step) {
				if (isCurrentStep(step)) {
					updateHardware();
				}
			}
		});

	}

	private boolean isCurrentStep(HeaterStep step) {
		if (steps.size() > 0) {
			return step == steps.get(0);
		}
		return false;
	}

	private void updateHardware() {

	}

	public BreweryLayout getLayout() {
		return brewLayout;
	}

	private void initLayout() throws Exception {
		brewLayout = configuration.getBrewLayout();

		// String json = jc.encode(brewLayout);
		// JsonPrettyPrint jpp = new JsonPrettyPrint();
		// jpp.setStripNullAttributes(true);
		// System.out.println(jpp.prettyPrint(json));

		List<Pump> pumps = brewLayout.getPumps();
		for (Pump pump : pumps) {
			brewHardwareControls.add(pump);
		}

		List<Tank> tanks = brewLayout.getTanks();
		for (Tank tank : tanks) {
			HeatElement heater = tank.getHeater();
			if (heater != null) {
				brewHardwareControls.add(heater);
			}
		}

	}

	private boolean equals(Double temp, Double tempatureC) {
		if (temp == tempatureC) {
			return true;
		} else if (temp == null || tempatureC == null) {
			return false;
		}

		return Math.abs(temp - tempatureC) < 0.001;
	}

	private void fireBreweryComponentChangeHandler(BreweryComponent component) {
		eventBus.fireEvent(new BreweryComponentChangeEvent(component));
	}

}
