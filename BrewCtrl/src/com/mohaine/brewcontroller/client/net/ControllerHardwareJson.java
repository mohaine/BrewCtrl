package com.mohaine.brewcontroller.client.net;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.bean.ControllerStatus;
import com.mohaine.brewcontroller.client.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.client.bean.TempSensor;
import com.mohaine.brewcontroller.client.bean.VersionBean;
import com.mohaine.brewcontroller.client.event.BreweryComponentChangeEvent;
import com.mohaine.brewcontroller.client.event.BreweryLayoutChangeEvent;
import com.mohaine.brewcontroller.client.event.ChangeModeEvent;
import com.mohaine.brewcontroller.client.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.client.event.StatusChangeEvent;
import com.mohaine.brewcontroller.client.event.StepModifyEvent;
import com.mohaine.brewcontroller.client.event.StepModifyEventHandler;
import com.mohaine.brewcontroller.client.event.StepsModifyEvent;
import com.mohaine.brewcontroller.client.event.bus.Event;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.client.layout.HeatElement;
import com.mohaine.brewcontroller.client.layout.Pump;
import com.mohaine.brewcontroller.client.layout.Sensor;
import com.mohaine.brewcontroller.client.layout.Tank;
import com.mohaine.brewcontroller.shared.json.JsonObjectConverter;
import com.mohaine.brewcontroller.shared.util.StringUtils;

public abstract class ControllerHardwareJson implements ControllerHardware {

	public static int DEFAULT_PORT = 2739;

	private List<BrewHardwareControl> brewHardwareControls = new ArrayList<BrewHardwareControl>();

	private ControllerStatus controllerStatus;
	private String status;
	private ControlStep selectedStep;

	private Configuration configuration;
	private EventBus eventBus;
	private VersionBean version;
	public boolean connected = false;

	private boolean updating;
	private boolean forceUpdate;

	private List<ControlStep> pendingSteps;
	private List<ControlStep> pendingStepEdits = new ArrayList<ControlStep>();;

	private String pendingMode;

	private long lastUpdateTime;

	private final JsonObjectConverter converter;

	protected abstract Configuration loadDefaultConfiguration();

	protected abstract void saveDefaultConfiguration() throws Exception;

	protected abstract CommandRequest getCommandRequest(String cmd);

	public interface CommandRequest {
		void runRequest(Callback<String> callback);

		void addParameter(String name, String value);
	}

	@Inject
	public ControllerHardwareJson(EventBus eventBusp, BrewJsonConverter converter) throws Exception {
		super();
		this.converter = converter.getJsonConverter();
		this.eventBus = eventBusp;

		// Listen for step mods, update remote if there is a change
		eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {
			@Override
			public void onStepChange(ControlStep step, boolean fromServer) {
				if (!fromServer) {
					modifyStep(step);
				}
			}
		});
	}

	protected void updateStatus() throws Exception {
		if (connected) {
			long now = System.currentTimeMillis();
			if (!updating && (forceUpdate || now - lastUpdateTime > 500)) {
				forceUpdate = false;
				updating = true;
				lastUpdateTime = now;

				CommandRequest commandRequest = getCommandRequest("status");

				final String pendingModeRequest = this.pendingMode;
				if (pendingModeRequest != null) {
					this.pendingMode = null;
					commandRequest.addParameter("mode", pendingModeRequest);
				}
				// New List?
				final List<ControlStep> pendingStepsRequest = new ArrayList<ControlStep>();
				if (this.pendingSteps != null) {
					pendingStepsRequest.addAll(this.pendingSteps);
					this.pendingSteps = null;
					String encode = converter.encode(pendingStepsRequest);
					commandRequest.addParameter("steps", encode);
				}

				// Step Edits?
				final List<ControlStep> modifySteps = new ArrayList<ControlStep>();
				synchronized (pendingStepEdits) {
					if (pendingStepEdits.size() > 0) {
						modifySteps.addAll(pendingStepEdits);
						pendingStepEdits.clear();
						String encode = converter.encode(modifySteps);
						commandRequest.addParameter("modifySteps", encode);
					}
				}

				commandRequest.runRequest(new Callback<String>() {
					@Override
					public void onSuccess(String response) {

						boolean success = false;
						List<Event<?>> eventsToFire = new ArrayList<Event<?>>();
						try {
							ControllerStatus lastStatus = controllerStatus;
							controllerStatus = converter.decode(response, ControllerStatus.class);
							if (!StringUtils.valueOf(controllerStatus.getConfigurationVersion()).equals(configuration.getVersion())) {
								loadConfiguration(null);
								return;
							}

							updateLayoutState(false, eventsToFire);
							eventsToFire.add(new StatusChangeEvent());

							if (controllerStatus != null) {
								List<ControlStep> newSteps = controllerStatus.getSteps();
								boolean stepsStructureChanged = true;
								if (lastStatus != null) {
									if (!lastStatus.getMode().equals(controllerStatus.getMode())) {
										eventsToFire.add(new ChangeModeEvent(controllerStatus.getMode()));
									}
									List<ControlStep> oldSteps = lastStatus.getSteps();
									if (oldSteps.size() == newSteps.size()) {
										stepsStructureChanged = false;
										for (int i = 0; i < oldSteps.size(); i++) {
											ControlStep oldStep = oldSteps.get(i);
											ControlStep newStep = newSteps.get(i);
											if (!oldStep.getId().equals(newStep.getId())) {
												stepsStructureChanged = true;
												break;
											}
										}
									}
								}

								if (stepsStructureChanged) {
									eventsToFire.add(new StepsModifyEvent());
								} else {
									List<ControlStep> oldSteps = lastStatus.getSteps();
									for (int i = 0; i < oldSteps.size(); i++) {
										ControlStep oldStep = oldSteps.get(i);
										ControlStep newStep = newSteps.get(i);

										if (newStep.isActive() || oldStep.isActive() || !oldStep.equals(newStep)) {
											eventsToFire.add(new StepModifyEvent(newStep, true));
										}
									}
								}

								boolean foundSelected = false;
								if (selectedStep != null) {
									for (int i = 0; i < newSteps.size(); i++) {
										ControlStep step = newSteps.get(i);
										if (step.getId().equals(selectedStep.getId())) {
											foundSelected = true;
											selectedStep = step;
											break;
										}
									}
								}

								if (!foundSelected && newSteps.size() > 0) {
									selectedStep = newSteps.get(0);
									eventsToFire.add(new ChangeSelectedStepEvent(selectedStep));
								}

								success = true;
							}
						} finally {

							// If we failed put back pendings

							if (!success) {

								disconnect();

								if (pendingSteps != null) {
									pendingSteps = pendingStepsRequest;
								}
								if (pendingMode != null) {
									pendingMode = pendingModeRequest;
								}

								synchronized (pendingStepEdits) {
									if (modifySteps.size() > 0) {
										pendingStepsRequest.addAll(0, modifySteps);
									}
								}
							}

							updating = false;
							for (Event<?> event : eventsToFire) {
								eventBus.fireEvent(event);
							}
						}

					}

					@Override
					public void onNotSuccess(Throwable e) {
						e.printStackTrace();
						setStatus(e.getMessage());
					}
				});

			}
		}
	}

	private void updateLayoutState(boolean forceDirty, List<Event<?>> eventsToFire) {

		if (controllerStatus != null) {
			List<ControlStep> steps = controllerStatus.getSteps();
			if (steps.size() > 0) {
				ControlStep heaterStep = steps.get(0);
				List<ControlPoint> controlPoints = heaterStep.getControlPoints();
				if (controlPoints != null) {
					for (BrewHardwareControl bhc : brewHardwareControls) {
						for (ControlPoint controlPoint : controlPoints) {
							if (controlPoint.getControlPin() == bhc.getPin()) {
								if (forceDirty || bhc.getDuty() != controlPoint.getDuty()) {
									bhc.setDuty(controlPoint.getDuty());
									eventsToFire.add(new BreweryComponentChangeEvent(bhc));
								}
							}
						}
					}
				}
			}

			List<Tank> tanks = configuration.getBrewLayout().getTanks();
			for (Tank tank : tanks) {
				Sensor sensor = tank.getSensor();
				if (sensor != null) {
					TempSensor tankTs = null;
					List<TempSensor> sensors = controllerStatus.getSensors();
					for (TempSensor tempSensor : sensors) {
						if (tempSensor.getAddress().equals(sensor.getAddress())) {
							tankTs = tempSensor;
							break;
						}
					}

					if (tankTs == null) {
						tankTs = new TempSensor();
					}

					Double temp = sensor.getTempatureC();
					boolean reading = sensor.isReading();
					sensor.setSensor(tankTs);

					boolean diff = forceDirty || !equals(temp, sensor.getTempatureC()) || reading != sensor.isReading();
					if (diff) {
						eventsToFire.add(new BreweryComponentChangeEvent(sensor));
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

	protected void connect() throws Exception {
		disconnect();
		getCommandRequest("version").runRequest(new Callback<String>() {
			@Override
			public void onSuccess(String versionString) {
				version = converter.decode(versionString, VersionBean.class);

				if (version != null) {
					// System.out.println("  Version: " + version.getVersion());
					connected = true;
					setStatus("Connected");
					loadConfiguration(null);
				}
			}

			@Override
			public void onNotSuccess(Throwable e) {
				connected = false;
			}
		});

	}

	protected void loadConfiguration(Configuration uploadLayout) {
		CommandRequest layoutRequest = getCommandRequest("configuration");
		if (uploadLayout != null) {
			layoutRequest.addParameter("configuration", converter.encode(loadDefaultConfiguration()));
		}
		layoutRequest.runRequest(new Callback<String>() {
			@Override
			public void onSuccess(String responseJson) {

				try {
					configuration = converter.decode(responseJson, Configuration.class);
					if (configuration == null) {
						loadConfiguration(loadDefaultConfiguration());
						return;
					}

					if (configuration != null) {

						saveDefaultConfiguration();
						initLayout();
						eventBus.fireEvent(new BreweryLayoutChangeEvent(configuration.getBrewLayout()));
					}
				} catch (Exception e) {
					configuration = null;
					disconnect();
					e.printStackTrace();
				}
			}

			@Override
			public void onNotSuccess(Throwable e) {
				e.printStackTrace();
				disconnect();
			}
		});

	}

	@Override
	public ControlStep getSelectedStep() {
		return selectedStep;
	}

	@Override
	public List<ControlStep> getSteps() {
		return controllerStatus != null ? controllerStatus.getSteps() : new ArrayList<ControlStep>();
	}

	public List<TempSensor> getSensors() {
		return controllerStatus != null ? controllerStatus.getSensors() : new ArrayList<TempSensor>();
	}

	@Override
	public String getMode() {
		return controllerStatus != null ? controllerStatus.getMode() : Mode.UNKNOWN.toString();
	}

	@Override
	public void changeMode(String mode) {
		this.pendingMode = mode;
		updateStatusAsap();
	}

	protected void updateStatusAsap() {
		forceUpdate = true;
	}

	@Override
	public void setSelectedStep(ControlStep step) {
		boolean dirty = this.selectedStep != step;
		this.selectedStep = step;
		if (dirty && eventBus != null) {
			eventBus.fireEvent(new ChangeSelectedStepEvent(selectedStep));
		}
	}

	private void initLayout() throws Exception {
		BreweryLayout brewLayout = configuration.getBrewLayout();
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
	public void changeSteps(List<ControlStep> steps) {
		this.pendingSteps = steps;
		updateStatusAsap();
	}

	@Override
	public BreweryLayout getBreweryLayout() {
		return configuration != null ? configuration.getBrewLayout() : null;
	}

	@Override
	public Double getSensorTemp(String sensorAddress) {
		List<TempSensor> sensors = controllerStatus.getSensors();
		for (int i = 0; i < sensors.size(); i++) {
			TempSensor tempSensor = sensors.get(i);
			if (tempSensor.getAddress().equals(sensorAddress)) {
				return tempSensor.getTempatureC();
			}
		}
		return null;
	}

	@Override
	public ControllerStatus getControllerStatus() {
		return controllerStatus;
	}

	public String getStatus() {
		return status;
	}

	public ControlStep createManualStep(String name) {
		ControlStep step = new ControlStep();
		step.setName(name);
		List<ControlPoint> controlPoints = step.getControlPoints();

		BreweryLayout breweryLayout = getBreweryLayout();
		List<Pump> pumps = breweryLayout.getPumps();
		for (Pump pump : pumps) {
			ControlPoint controlPoint = new ControlPoint();
			controlPoint.setAutomaticControl(false);
			controlPoint.setControlPin(pump.getPin());
			controlPoint.setHasDuty(pump.isHasDuty());
			controlPoints.add(controlPoint);
		}

		List<Tank> tanks = breweryLayout.getTanks();
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
					List<TempSensor> sensors = controllerStatus.getSensors();
					for (TempSensor hardwareSensor : sensors) {
						if (hardwareSensor.getAddress().equals(sensor.getAddress())) {
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

	private void modifyStep(ControlStep step) {
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
