package com.mohaine.brewcontroller.net;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.Configuration;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.ControllerHardware;
import com.mohaine.brewcontroller.SensorConfiguration;
import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareControl.Mode;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.brewcontroller.event.StepModifyEventHandler;
import com.mohaine.brewcontroller.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.event.bus.EventBus;

public class ControllerHardwareJson implements ControllerHardware {
	public static int DEFAULT_PORT = 2739;
	private ArrayList<HardwareSensor> tempSensors = new ArrayList<HardwareSensor>();
	private List<BrewHardwareControl> brewHardwareControls = new ArrayList<BrewHardwareControl>();

	private HardwareControl hc;
	private String status;
	private HeaterStep selectedStep;
	private BreweryLayout brewLayout;
	private Configuration configuration;
	private EventBus eventBus;

	@Inject
	public ControllerHardwareJson(EventBus eventBusp, ConfigurationLoader configurationLoader) throws Exception {
		super();
		this.eventBus = eventBusp;
		this.configuration = configurationLoader.getConfiguration();

		hc = new HardwareControl();
		hc.setSteps(new ArrayList<HeaterStep>());

		initLayout();

		// Listen for step mods, update remote if there is a change
		eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {
			@Override
			public void onStepChange(HeaterStep step) {
				// TODO
			}
		});
	}

	@Override
	public HeaterStep getSelectedStep() {
		return selectedStep;
	}

	@Override
	public List<HeaterStep> getSteps() {
		return hc.getSteps();
	}

	@Override
	public Mode getMode() {
		return hc.getMode();
	}

	@Override
	public void changeMode(Mode mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelectedStep(HeaterStep step) {
		boolean dirty = this.selectedStep != step;
		this.selectedStep = step;
		if (dirty && eventBus != null) {
			eventBus.fireEvent(new ChangeSelectedStepEvent(selectedStep));
		}
	}

	private void initLayout() throws Exception {
		brewLayout = configuration.getBrewLayout();

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

	@Override
	public void changeSteps(List<HeaterStep> steps) {
		// TODO Auto-generated method stub

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
				controlPoint.setFullOnAmps(heater.getFullOnAmps());

				Sensor sensor = tank.getSensor();
				if (sensor != null) {
					List<HardwareSensor> sensors = getSensors();
					for (HardwareSensor hardwareSensor : sensors) {
						SensorConfiguration findSensorByLocation = configuration.findSensor(hardwareSensor.getAddress());

						if (findSensorByLocation != null && tank.getName().equals(findSensorByLocation.getLocation())) {
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

	@Override
	public BreweryLayout getBreweryLayout() {
		return brewLayout;
	}

	@Override
	public List<HardwareSensor> getSensors() {
		return tempSensors;
	}

	@Override
	public Double getSensorTemp(String sensorAddress) {
		List<HardwareSensor> sensors = getSensors();
		for (int i = 0; i < sensors.size(); i++) {
			HardwareSensor tempSensor = sensors.get(i);
			if (tempSensor.getAddress().equals(sensorAddress)) {
				return tempSensor.getTempatureC();
			}
		}
		return null;
	}

	@Override
	public HardwareControl getHardwareStatus() {
		return hc;
	}

	public String getStatus() {
		return status;
	}
}
