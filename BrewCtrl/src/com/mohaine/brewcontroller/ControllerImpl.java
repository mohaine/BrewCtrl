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
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HeaterMode;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.bean.TempSensor;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.brewcontroller.event.StepModifyEventHandler;
import com.mohaine.brewcontroller.event.StepsModifyEvent;
import com.mohaine.brewcontroller.layout.BreweryComponent;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.Heater;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.event.bus.EventBus;

public class ControllerImpl implements Controller {
	private List<HeaterStep> steps = new ArrayList<HeaterStep>();
	private HeaterStep selectedStep = null;;

	private int boilDuty = 0;
	private Mode mode = Mode.OFF;

	private EventBus eventBus;
	private Monitor monitor = new Monitor();
	private Hardware hardware;
	private BrewPrefs prefs;
	private BreweryLayout brewLayout;

	private class Monitor implements Runnable {
		@Override
		public void run() {
			while (true) {
				HeaterStep heaterStep = null;
				synchronized (steps) {
					if (steps.size() > 0) {
						heaterStep = steps.get(0);
					}
				}
				if (heaterStep != null) {
					synchronized (heaterStep) {
						switch (mode) {
						case ON: {
							heaterStep.startTimer();
							eventBus.fireEvent(new StepModifyEvent(heaterStep));
							if (heaterStep.isComplete()) {
								nextStep();
							}
							break;
						}
						case HOLD: {
							heaterStep.stopTimer();
							break;
						}
						case OFF: {
							heaterStep.stopTimer();
							updateHardware();
							break;
						}
						}

					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}

			}
		}
	}

	public void nextStep() {
		boolean updateSelection = false;
		HeaterStep newSelection = null;
		synchronized (steps) {
			if (steps.size() > 0) {
				HeaterStep remove = steps.remove(0);
				updateSelection = remove == selectedStep;
				if (steps.size() > 0) {
					newSelection = steps.get(0);
				}
			}
		}

		if (updateSelection) {
			setSelectedStep(newSelection);
		}
		eventBus.fireEvent(new StepsModifyEvent());
	}

	@Inject
	public ControllerImpl(EventBus eventBusp, Hardware hardware, BrewPrefs prefs) {
		super();
		this.eventBus = eventBusp;
		this.hardware = hardware;
		this.prefs = prefs;

		initLayout();

		eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {
			@Override
			public void onStepChange(HeaterStep step) {
				if (isCurrentStep(step)) {
					updateHardware();
				}
			}

		});
		new Thread(monitor).start();
	}

	private boolean isCurrentStep(HeaterStep step) {
		if (steps.size() > 0) {
			return step == steps.get(0);
		}
		return false;
	}

	@Override
	public List<HeaterStep> getSteps() {
		return steps;
	}

	@Override
	public void setSteps(List<HeaterStep> steps) {
		this.steps = steps;
		eventBus.fireEvent(new StepsModifyEvent());

	}

	@Override
	public HeaterStep getSelectedStep() {
		return selectedStep;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public void setMode(Mode mode) {
		this.mode = mode;

		updateHardware();
	}

	@Override
	public void setSelectedStep(HeaterStep step) {
		this.selectedStep = step;
		if (eventBus != null) {
			eventBus.fireEvent(new ChangeSelectedStepEvent(selectedStep));
		}
		updateHardware();
	}

	@Override
	public void setBoilDuty(int duty) {
		boilDuty = duty;
		updateHardware();
	}

	private void updateHardware() {
		HardwareControl hc = new HardwareControl();

		hc.setMode(mode == Mode.OFF ? HeaterMode.OFF : HeaterMode.ON);
		hc.setBoilDuty(boilDuty);
		if (steps.size() > 0) {
			HeaterStep currentStep = steps.get(0);
			hc.setMashOn(true);
			// hc.setHltTargetTemp(currentStep.getHltTemp());
			// hc.setTunTargetTemp(currentStep.getTunTemp());
			// hc.setHltSensor(prefs.getHltSensorAddress());
			// hc.setTunSensor(prefs.getTunSensorAddress());
		} else {
			hc.setMashOn(false);
		}
		hardware.setHardwareControl(hc);
	}

	public BreweryLayout getLayout() {
		return brewLayout;
	}

	private void initLayout() {
		brewLayout = new BreweryLayout();
		brewLayout.setName("Brewing");

		List<Tank> tanks = brewLayout.getTanks();
		Tank hlt = new Tank();
		hlt.setName("HLT");
		hlt.setHeater(new Heater());
		hlt.setSensor(new Sensor());
		tanks.add(hlt);

		Tank tun = new Tank();
		tun.setName("TUN");
		tun.setSensor(new Sensor());
		tanks.add(tun);

		Tank kettle = new Tank();
		kettle.setName("Kettle");
		kettle.setHeater(new Heater());
		tanks.add(kettle);

		List<Pump> pumps = brewLayout.getPumps();
		Pump pump = new Pump();
		pump.setName("Loop");
		pumps.add(pump);

		Pump mainPump = new Pump();
		mainPump.setName("Main");
		pumps.add(mainPump);
	}

	@Override
	public Double getTankTemp(BreweryComponent component) {

		List<TempSensor> sensors = hardware.getSensors();
		for (TempSensor tempSensor : sensors) {
			if (component.getName().equals(prefs.getSensorLocation(tempSensor.getAddress(), ""))) {
				return tempSensor.getTempatureC();
			}
		}

		return null;
	}
}
