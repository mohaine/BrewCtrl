package com.mohaine.brewcontroller.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.BrewJsonConverter;
import com.mohaine.brewcontroller.Configuration;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.ControllerHardware;
import com.mohaine.brewcontroller.SensorConfiguration;
import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.ControllerStatus;
import com.mohaine.brewcontroller.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.bean.VersionBean;
import com.mohaine.brewcontroller.event.BreweryLayoutChangeEvent;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.event.StatusChangeEvent;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.brewcontroller.event.StepModifyEventHandler;
import com.mohaine.brewcontroller.event.StepsModifyEvent;
import com.mohaine.brewcontroller.json.JsonObjectConverter;
import com.mohaine.brewcontroller.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.event.bus.EventBus;

public class ControllerHardwareJson implements ControllerHardware {
	private final JsonObjectConverter converter = BrewJsonConverter.getJsonConverter();

	public static int DEFAULT_PORT = 2739;

	private ArrayList<HardwareSensor> tempSensors = new ArrayList<HardwareSensor>();

	private List<BrewHardwareControl> brewHardwareControls = new ArrayList<BrewHardwareControl>();

	private ControllerStatus controllerStatus;
	private String status;
	private HeaterStep selectedStep;
	private BreweryLayout brewLayout;
	private Configuration configuration;
	private EventBus eventBus;
	private VersionBean version;
	private boolean run = true;
	public boolean connected = false;

	private boolean updating;

	private List<HeaterStep> pendingSteps;
	private List<HeaterStep> pendingStepEdits = new ArrayList<HeaterStep>();;

	private Thread statusThread;

	private Mode pendingMode;

	@Inject
	public ControllerHardwareJson(EventBus eventBusp, ConfigurationLoader configurationLoader) throws Exception {
		super();
		this.eventBus = eventBusp;
		this.configuration = configurationLoader.getConfiguration();

		controllerStatus = new ControllerStatus();
		controllerStatus.setSteps(new ArrayList<HeaterStep>());

		brewLayout = configurationLoader.getConfiguration().getBrewLayout();

		statusThread = new Thread(new CommThread());
		statusThread.start();

		// Listen for step mods, update remote if there is a change
		eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {
			@Override
			public void onStepChange(HeaterStep step) {
				modifyStep(step);
			}

		});
	}

	private void updateStatus() throws Exception {
		if (connected) {
			if (!updating) {
				updating = true;
				try {
					URLRequest commandRequest = getCommandRequest("status");

					Mode pendingMode = this.pendingMode;
					if (pendingMode != null) {
						this.pendingMode = null;
						String string = pendingMode.toString();
						commandRequest.addParameter("mode", string);
					}
					// New List?
					List<HeaterStep> pendingSteps = this.pendingSteps;
					if (pendingSteps != null) {
						commandRequest.addParameter("steps", converter.encode(pendingSteps));
					}

					// Step Edits?
					synchronized (pendingStepEdits) {
						if (pendingStepEdits.size() > 0) {
							List<HeaterStep> modifySteps = new ArrayList<HeaterStep>(pendingStepEdits);
							pendingStepEdits.clear();
							if (pendingSteps != null) {
								commandRequest.addParameter("modifySteps", converter.encode(modifySteps));
							}
						}
					}

					try {
						controllerStatus = converter.decode(commandRequest.getString(), ControllerStatus.class);
					} finally {

						// If we failed put back pendings
						if (controllerStatus == null) {
							if (this.pendingSteps != null) {
								this.pendingSteps = pendingSteps;
							}
							if (this.pendingMode != null) {
								this.pendingMode = pendingMode;
							}
						}
					}
					if (controllerStatus == null) {
						disconnect();
					} else {
						eventBus.fireEvent(new StatusChangeEvent());

						// Only do if there is a change
						eventBus.fireEvent(new StepsModifyEvent());

					}

				} finally {
					updating = false;
				}
			}
		}
	}

	private void connect() throws Exception {
		disconnect();
		System.out.println(String.format("Connect to %s", getBaseCmdUrl()));
		version = converter.decode(getCommandRequest("version").getString(), VersionBean.class);
		if (version != null) {
			System.out.println(String.format("Connected to %s version %s", getBaseCmdUrl(), version.getVersion()));

			status = "Connected";
			connected = true;
			eventBus.fireEvent(new StatusChangeEvent());

			brewLayout = converter.decode(getCommandRequest("layout").getString(), BreweryLayout.class);
			if (brewLayout == null) {
				System.out.println(String.format("Server doesn't have layout, upload local version"));
				URLRequest layoutRequest = getCommandRequest("layout");
				String encode = converter.encode(configuration.getBrewLayout());
				layoutRequest.addParameter("layout", encode);
				this.brewLayout = converter.decode(layoutRequest.getString(), BreweryLayout.class);
			}

			if (brewLayout != null) {
				initLayout();
				eventBus.fireEvent(new BreweryLayoutChangeEvent(brewLayout));
			}

		}
	}

	private URLRequest getCommandRequest(String cmd) throws MalformedURLException {
		String baseUrl = getBaseCmdUrl();
		URL url = new URL(baseUrl + cmd);
		URLRequest r = new URLRequest(url);
		return r;
	}

	private String getBaseCmdUrl() {
		String baseUrl = "http://localhost:" + DEFAULT_PORT + "/cmd/";
		return baseUrl;
	}

	@Override
	public HeaterStep getSelectedStep() {
		return selectedStep;
	}

	@Override
	public List<HeaterStep> getSteps() {
		return controllerStatus.getSteps();
	}

	@Override
	public Mode getMode() {
		return controllerStatus.getMode();
	}

	@Override
	public void changeMode(Mode mode) {
		this.pendingMode = mode;
		statusThread.interrupt();
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
		this.pendingSteps = steps;
		statusThread.interrupt();
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
	public ControllerStatus getHardwareStatus() {
		return controllerStatus;
	}

	public String getStatus() {
		return status;
	}

	private final class CommThread implements Runnable {
		@Override
		public void run() {
			while (run) {
				try {
					if (!connected) {
						connect();
					}

					updateStatus();

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignore
					}
				} catch (Exception e) {
					status = e.getMessage();
					e.printStackTrace();
					disconnect();

				}

			}
		}

	}

	public void disconnect() {
		connected = false;

	}

	private void modifyStep(HeaterStep step) {
		synchronized (pendingStepEdits) {
			for (int i = 0; i < pendingStepEdits.size(); i++) {
				if (pendingStepEdits.get(i).getId().equals(step.getId())) {
					pendingStepEdits.set(i, step);
					return;
				}
			}
			pendingStepEdits.add(step);
		}
	}
}
