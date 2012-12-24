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
import com.mohaine.brewcontroller.event.BreweryComponentChangeEvent;
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
	private boolean forceUpdate;

	private List<HeaterStep> pendingSteps;
	private List<HeaterStep> pendingStepEdits = new ArrayList<HeaterStep>();;

	private Thread statusThread;

	private Mode pendingMode;

	private long lastUpdateTime;

	@Inject
	public ControllerHardwareJson(EventBus eventBusp, ConfigurationLoader configurationLoader) throws Exception {
		super();
		this.eventBus = eventBusp;
		this.configuration = configurationLoader.getConfiguration();

		controllerStatus = new ControllerStatus();
		controllerStatus.setSteps(new ArrayList<HeaterStep>());
		controllerStatus.setSensors(new ArrayList<HardwareSensor>());

		brewLayout = configurationLoader.getConfiguration().getBrewLayout();

		statusThread = new Thread(new CommThread());
		statusThread.start();

		// Listen for step mods, update remote if there is a change
		eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {
			@Override
			public void onStepChange(HeaterStep step, boolean fromServer) {

				if (!fromServer) {
					System.out.println("modify Step: " + step.getName());
					modifyStep(step);
				}
			}
		});
	}

	private void updateStatus() throws Exception {
		if (connected) {
			long now = System.currentTimeMillis();
			if (!updating && (forceUpdate || now - lastUpdateTime > 500)) {
				forceUpdate = false;
				updating = true;
				lastUpdateTime = now;
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
						this.pendingSteps = null;
						commandRequest.addParameter("steps", converter.encode(pendingSteps));
					}

					// Step Edits?
					List<HeaterStep> modifySteps = null;
					synchronized (pendingStepEdits) {
						if (pendingStepEdits.size() > 0) {
							modifySteps = new ArrayList<HeaterStep>(pendingStepEdits);
							pendingStepEdits.clear();
							String encode = converter.encode(modifySteps);
							commandRequest.addParameter("modifySteps", encode);
						}
					}

					boolean success = false;
					try {

						String response = commandRequest.getString();

						ControllerStatus lastStatus = controllerStatus;

						controllerStatus = converter.decode(response, ControllerStatus.class);

						updateLayoutState(false);

						eventBus.fireEvent(new StatusChangeEvent());
						if (controllerStatus != null) {
							List<HeaterStep> newSteps = controllerStatus.getSteps();

							boolean stepsStructureChanged = true;
							if (lastStatus != null) {

								List<HeaterStep> oldSteps = lastStatus.getSteps();

								if (oldSteps.size() == newSteps.size()) {
									stepsStructureChanged = false;
									for (int i = 0; i < oldSteps.size(); i++) {
										HeaterStep oldStep = oldSteps.get(i);
										HeaterStep newStep = newSteps.get(i);
										if (!oldStep.getId().equals(newStep.getId())) {
											stepsStructureChanged = true;
											break;
										}
									}
								}

							}

							List<HeaterStep> oldSteps = lastStatus.getSteps();

							if (stepsStructureChanged) {
								eventBus.fireEvent(new StepsModifyEvent());
								boolean foundSelected = false;
								if (selectedStep != null) {
									for (int i = 0; i < newSteps.size(); i++) {
										HeaterStep step = newSteps.get(i);
										if (step.getId().equals(selectedStep.getId())) {
											foundSelected = true;
											break;
										}
									}
								}

								if (!foundSelected && newSteps.size() > 0) {
									selectedStep = newSteps.get(0);
									eventBus.fireEvent(new ChangeSelectedStepEvent(selectedStep));
								}

							} else {
								for (int i = 0; i < oldSteps.size(); i++) {
									HeaterStep oldStep = oldSteps.get(i);
									HeaterStep newStep = newSteps.get(i);
									if (newStep.isStarted() || oldStep.isStarted() || !oldStep.equals(newStep)) {
										eventBus.fireEvent(new StepModifyEvent(newStep, true));
									}
								}
							}

							success = true;
						}
					} finally {

						// If we failed put back pendings

						if (!success) {

							disconnect();

							if (this.pendingSteps != null) {
								this.pendingSteps = pendingSteps;
							}
							if (this.pendingMode != null) {
								this.pendingMode = pendingMode;
							}

							synchronized (pendingStepEdits) {
								if (modifySteps != null) {
									this.pendingSteps.addAll(0, modifySteps);
								}
							}
						}
					}

				} finally {
					updating = false;
				}
			}
		}
	}

	private void updateLayoutState(boolean forceDirty) {

		if (controllerStatus != null) {

			List<HeaterStep> steps = controllerStatus.getSteps();
			if (steps.size() > 0) {
				HeaterStep heaterStep = steps.get(0);
				List<ControlPoint> controlPoints = heaterStep.getControlPoints();

				if (controlPoints != null) {
					for (BrewHardwareControl bhc : brewHardwareControls) {
						for (ControlPoint controlPoint : controlPoints) {
							if (controlPoint.getControlPin() == bhc.getPin()) {
								if (forceDirty || bhc.getDuty() != controlPoint.getDuty()) {
									bhc.setDuty(controlPoint.getDuty());

									eventBus.fireEvent(new BreweryComponentChangeEvent(bhc));

								}
							}
						}
					}
				}
			}

			List<Tank> tanks = brewLayout.getTanks();
			for (Tank tank : tanks) {
				Sensor sensor = tank.getSensor();
				if (sensor != null) {

					HardwareSensor tankTs = null;
					List<HardwareSensor> sensors = controllerStatus.getSensors();
					for (HardwareSensor tempSensor : sensors) {
						SensorConfiguration findSensor = configuration.findSensor(tempSensor.getAddress());
						if (findSensor != null && tank.getName().equals(findSensor.getLocation())) {
							tankTs = tempSensor;
							break;
						}
					}

					if (tankTs == null) {
						tankTs = new HardwareSensor();
					}

					Double temp = sensor.getTempatureC();
					boolean reading = sensor.isReading();
					sensor.setAddress(tankTs.getAddress());
					sensor.setReading(tankTs.isReading());
					sensor.setTempatureC(tankTs.getTempatureC());

					boolean diff = forceDirty || !equals(temp, sensor.getTempatureC()) || reading != sensor.isReading();
					if (diff) {
						eventBus.fireEvent(new BreweryComponentChangeEvent(sensor));
					}

				}

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

	private void connect() throws Exception {
		disconnect();
		// System.out.println(String.format("Connect to %s", getBaseCmdUrl()));
		version = converter.decode(getCommandRequest("version").getString(), VersionBean.class);
		if (version != null) {
			System.out.println(String.format("Connected to %s version %s", getBaseCmdUrl(), version.getVersion()));
			connected = true;
			setStatus("Connected");

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
		updateStatusNow();
	}

	private void updateStatusNow() {
		forceUpdate = true;
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
		updateStatusNow();
	}

	@Override
	public BreweryLayout getBreweryLayout() {
		return brewLayout;
	}

	@Override
	public Double getSensorTemp(String sensorAddress) {
		List<HardwareSensor> sensors = controllerStatus.getSensors();
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
					setStatus(e.getMessage());
					disconnect();
				}

			}
		}

	}

	public HeaterStep createManualStep(String name) {
		HeaterStep step = new HeaterStep();
		step.setName(name);
		List<ControlPoint> controlPoints = step.getControlPoints();

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
					List<HardwareSensor> sensors = controllerStatus.getSensors();
					for (HardwareSensor hardwareSensor : sensors) {
						if (sensor.getAddress().equals(hardwareSensor.getAddress())) {
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

	public void disconnect() {
		connected = false;

	}

	public void setStatus(String message) {
		status = message;
		eventBus.fireEvent(new StatusChangeEvent());
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
