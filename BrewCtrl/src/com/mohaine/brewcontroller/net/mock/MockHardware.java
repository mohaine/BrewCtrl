package com.mohaine.brewcontroller.net.mock;

import java.util.List;
import java.util.Random;

import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.bean.ControllerStatus;
import com.mohaine.brewcontroller.client.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.client.bean.SensorConfiguration;
import com.mohaine.brewcontroller.client.bean.TempSensor;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.client.layout.Tank;
import com.mohaine.brewcontroller.shared.util.StringUtils;

public class MockHardware {
	private ControllerStatus status;
	private Configuration configuration;

	public MockHardware() {
		new Thread(new Monitor()).start();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
		updateStatusConfigVersion();
	}

	private void updateStatusConfigVersion() {
		if (status != null && configuration != null) {
			status.setConfigurationVersion(configuration.getVersion());
		}
	}

	public ControllerStatus getStatus() {
		return status;
	}

	public void setStatus(ControllerStatus status) {
		this.status = status;
		updateStatusConfigVersion();
	}

	public void setMode(String modeParam) {

		if (Mode.OFF.toString().equals(modeParam)) {
			status.setMode(Mode.OFF);
		} else if (Mode.ON.toString().equals(modeParam)) {
			status.setMode(Mode.ON);
		} else if (Mode.HOLD.toString().equals(modeParam)) {
			status.setMode(Mode.HOLD);
		}

	}

	public void setSteps(List<ControlStep> steps) {
		status.setSteps(steps);
	}

	private class Monitor implements Runnable {

		@Override
		public void run() {

			long lastOnTime = 0;
			Random r = new Random();
			while (true) {
				try {
					lastOnTime = updateLoop(lastOnTime, r);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}

		private long updateLoop(long lastOnTime, Random r) {
			ControlStep heaterStep = null;
			if (status != null) {
				List<TempSensor> sensors = status.getSensors();
				for (TempSensor hardwareSensor : sensors) {
					hardwareSensor.setTempatureC(hardwareSensor.getTempatureC() + (r.nextDouble() - 0.5));
				}
				if (configuration != null) {
					BreweryLayout layout = configuration.getBrewLayout();
					if (layout != null) {
						List<Tank> tanks = layout.getTanks();
						if (tanks != null) {
							for (Tank tank : tanks) {
								if (tank.getSensor() != null) {
									// TODO Switch to reading sensor
									boolean found = false;
									for (TempSensor hardwareSensor : sensors) {
										if (hardwareSensor.isReading() && hardwareSensor.getAddress().equals(tank.getSensor().getAddress())) {
											found = true;
											break;
										}
									}

									if (!found) {

										System.out.println("Missing Sensor for " + tank.getName());
										for (SensorConfiguration sensorConfig : configuration.getSensors()) {
											if (StringUtils.valueOf(sensorConfig.getLocation()).equals(tank.getName())) {
												for (TempSensor hardwareSensor : sensors) {
													if (hardwareSensor.isReading() && hardwareSensor.getAddress().equals(sensorConfig.getAddress())) {
														// Found it
														tank.getSensor().setSensor(hardwareSensor);

														configuration.setVersion(StringUtils.generateRandomId());
														updateStatusConfigVersion();
														break;
													}
												}
											}
										}
									}
								}
							}
						}

					}
				}
				List<ControlStep> steps = status.getSteps();
				if (steps != null) {
					synchronized (steps) {
						if (steps.size() > 0) {
							heaterStep = steps.get(0);
						}
					}
					if (heaterStep != null) {
						synchronized (heaterStep) {

							if (Mode.ON.equals(status.getMode())) {
								if (!heaterStep.isActive()) {
									lastOnTime = 0;
									heaterStep.setActive(true);
								}
								int stepTime = heaterStep.getStepTime();
								if (stepTime > 0) {
									long now = System.currentTimeMillis();
									long onTime = 0;
									if (lastOnTime > 0) {
										onTime = now - lastOnTime;

										while (onTime > 1000) {
											int newStepTime = stepTime - 1;
											onTime -= 1000;
											if (newStepTime <= 0) {
												synchronized (steps) {
													status.getSteps().remove(heaterStep);
												}
												break;
											} else {
												heaterStep.setStepTime(newStepTime);
											}
										}

									}
									// Put extra back into lastOnTime;
									lastOnTime = now - onTime;
								}
							} else if (Mode.HOLD.equals(status.getMode())) {
								lastOnTime = 0;
								heaterStep.setActive(true);
							} else if (Mode.OFF.equals(status.getMode())) {
								lastOnTime = 0;
								heaterStep.setActive(false);
							}

						}
					}
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
			return lastOnTime;
		}
	}

	public void updateStep(ControlStep modStep) {
		List<ControlStep> steps = status.getSteps();
		if (steps != null) {
			synchronized (steps) {
				for (ControlStep step : steps) {
					if (step.getId().equals(modStep.getId())) {
						step.copyFrom(modStep);
					}
				}
			}
		}
	}
}