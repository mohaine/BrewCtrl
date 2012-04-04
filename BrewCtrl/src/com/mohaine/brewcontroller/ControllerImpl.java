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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.bean.HeaterMode;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.event.BreweryComponentChangeEvent;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.brewcontroller.event.StepModifyEventHandler;
import com.mohaine.brewcontroller.event.StepsModifyEvent;
import com.mohaine.brewcontroller.json.JsonObjectConverter;
import com.mohaine.brewcontroller.json.ReflectionJsonHandler;
import com.mohaine.brewcontroller.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.layout.BreweryComponent;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.brewcontroller.util.StreamUtils;
import com.mohaine.event.StatusChangeHandler;
import com.mohaine.event.bus.EventBus;

public class ControllerImpl implements Controller {

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

							if (heaterStep.isComplete()) {
								nextStep();
							} else {
								if (heaterStep.getStepTime() > 0) {
									eventBus.fireEvent(new StepModifyEvent(heaterStep));
								}
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

	private List<HeaterStep> steps = new ArrayList<HeaterStep>();
	private List<BrewHardwareControl> brewHardwareControls = new ArrayList<BrewHardwareControl>();
	private HeaterStep selectedStep = null;;
	private Mode mode = Mode.OFF;
	private EventBus eventBus;
	private Hardware hardware;
	private BrewPrefs prefs;
	private BreweryLayout brewLayout;
	private Monitor monitor = new Monitor();

	@Inject
	public ControllerImpl(EventBus eventBusp, Hardware hardware, BrewPrefs prefs) throws Exception {
		super();
		this.eventBus = eventBusp;
		this.hardware = hardware;
		this.prefs = prefs;

		initLayout();
		steps.add(createManualStep("Default"));
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

		hardware.addStatusChangeHandler(new StatusChangeHandler() {
			@Override
			public void onStateChange() {
				updateLayoutState();
			}
		});

		new Thread(monitor).start();

	}

	public void nextStep() {
		boolean updateSelection = false;
		HeaterStep newSelection = null;
		synchronized (steps) {

			if (steps.size() > 0) {
				HeaterStep remove = steps.remove(0);
				updateSelection = remove == selectedStep;
			}

			if (steps.size() == 0) {
				steps.add(createManualStep("Default"));
			}
			newSelection = steps.get(0);
		}

		if (updateSelection) {
			setSelectedStep(newSelection);
		}
		eventBus.fireEvent(new StepsModifyEvent());
	}

	@Override
	public HeaterStep createManualStep(String name) {
		HeaterStep step = new HeaterStep();
		step.setName(name);
		ArrayList<ControlPoint> controlPoints = step.getControlPoints();

		List<Pump> pumps = brewLayout.getPumps();
		for (Pump pump : pumps) {
			ControlPoint controlPoint = new ControlPoint();
			controlPoint.setAutomaticControl(false);
			controlPoint.setControlPin(pump.getPin());
			controlPoint.setHasDuty(pump.isHasDuty());
			controlPoints.add(controlPoint);
		}

		List<Tank> tanks = brewLayout.getTanks();
		for (Tank tank : tanks) {
			HeatElement heater = tank.getHeater();
			if (heater != null) {
				ControlPoint controlPoint = new ControlPoint();
				controlPoint.setAutomaticControl(false);
				controlPoint.setControlPin(heater.getPin());
				controlPoint.setHasDuty(heater.isHasDuty());

				Sensor sensor = tank.getSensor();
				if (sensor != null) {
					List<HardwareSensor> sensors = hardware.getSensors();
					for (HardwareSensor hardwareSensor : sensors) {
						if (tank.getName().equals(prefs.getSensorLocation(hardwareSensor.getAddress(), ""))) {
							controlPoint.setAutomaticControl(false);
							controlPoint.setTempSensorAddress(hardwareSensor.getAddress());
							break;
						}
					}
				}

				controlPoints.add(controlPoint);
			}
		}

		return step;
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
		boolean updateSelection;
		synchronized (steps) {
			if (steps.size() == 0) {
				steps.add(createManualStep("Default"));
			}
			updateSelection = selectedStep == null || !steps.contains(selectedStep);
		}

		eventBus.fireEvent(new StepsModifyEvent());

		if (updateSelection) {
			setSelectedStep(steps.get(0));
		}
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
		updateHardware();
		if (eventBus != null) {
			eventBus.fireEvent(new ChangeSelectedStepEvent(selectedStep));
		}
	}

	private void updateHardware() {
		HardwareControl hc = new HardwareControl();
		hc.setMode(mode == Mode.OFF ? HeaterMode.OFF : HeaterMode.ON);

		if (steps.size() > 0) {
			HeaterStep currentStep = steps.get(0);
			currentStep.setActive(true);
			ArrayList<ControlPoint> controlPoints = currentStep.getControlPoints();
			hc.setControlPoints(controlPoints);
		}
		hardware.setHardwareControl(hc);
	}

	public BreweryLayout getLayout() {
		return brewLayout;
	}

	private void initLayout() throws Exception {

		JsonObjectConverter jc = getJsonConverter();

		InputStream resourceAsStream = getClass().getResourceAsStream("/BreweryLayout.json");
		try {
			if (resourceAsStream != null) {
				String json = new String(StreamUtils.readStream(resourceAsStream));
				brewLayout = jc.decode(json, BreweryLayout.class);
			}
		} finally {
			StreamUtils.close(resourceAsStream);
		}

		if (brewLayout == null) {
			brewLayout = new BreweryLayout();
		}

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

	private JsonObjectConverter getJsonConverter() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter(false);
		jc.addHandler(ReflectionJsonHandler.build(BreweryLayout.class));
		jc.addHandler(ReflectionJsonHandler.build(Tank.class));
		jc.addHandler(ReflectionJsonHandler.build(Sensor.class));
		jc.addHandler(ReflectionJsonHandler.build(HeatElement.class));
		jc.addHandler(ReflectionJsonHandler.build(Pump.class));
		return jc;
	}

	private void updateLayoutState() {

		for (BrewHardwareControl bhc : brewHardwareControls) {
			HardwareControl hardwareStatus = hardware.getHardwareStatus();
			List<ControlPoint> controlPoints = hardwareStatus.getControlPoints();
			for (ControlPoint controlPoint : controlPoints) {
				if (controlPoint.getControlPin() == bhc.getPin()) {
					if (bhc.getDuty() != controlPoint.getDuty()) {
						bhc.setDuty(controlPoint.getDuty());
						fireBreweryComponentChangeHandler(bhc);
					}
				}
			}
		}

		List<HardwareSensor> sensors = hardware.getSensors();
		for (HardwareSensor tempSensor : sensors) {
			List<Tank> tanks = brewLayout.getTanks();
			for (Tank tank : tanks) {
				Sensor sensor = tank.getSensor();
				if (sensor != null) {
					if (tank.getName().equals(prefs.getSensorLocation(tempSensor.getAddress(), ""))) {
						Double temp = sensor.getTempatureC();
						boolean reading = sensor.isReading();
						sensor.setAddress(tempSensor.getAddress());
						sensor.setReading(tempSensor.isReading());
						sensor.setTempatureC(tempSensor.getTempatureC());
						boolean diff = temp != sensor.getTempatureC() || reading != sensor.isReading();
						if (diff) {
							fireBreweryComponentChangeHandler(sensor);
						}
					}
				}
			}
		}
	}

	private void fireBreweryComponentChangeHandler(BreweryComponent component) {
		eventBus.fireEvent(new BreweryComponentChangeEvent(component));
	}

}
