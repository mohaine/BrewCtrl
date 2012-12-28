package com.mohaine.brewcontroller.net.mock;

import java.util.List;
import java.util.Random;

import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.bean.ControllerStatus;
import com.mohaine.brewcontroller.client.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.client.bean.TempSensor;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.client.layout.HeatElement;
import com.mohaine.brewcontroller.client.layout.Pump;
import com.mohaine.brewcontroller.client.layout.Sensor;
import com.mohaine.brewcontroller.client.layout.Tank;

public class MockHardware {

	private ControllerStatus status;
	private BreweryLayout layout;

	public MockHardware() {
		new Thread(new Monitor()).start();
	}

	public BreweryLayout getLayout() {
		return layout;
	}

	public void setLayout(BreweryLayout layout) {
		this.layout = layout;

	}

	public ControllerStatus getStatus() {
		return status;
	}

	public void setStatus(ControllerStatus status) {
		this.status = status;
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

	public ControlStep createManualStep(String name) {
		ControlStep step = new ControlStep();
		step.setName(name);
		List<ControlPoint> controlPoints = step.getControlPoints();

		List<Pump> pumps = layout.getPumps();
		for (Pump pump : pumps) {
			ControlPoint controlPoint = new ControlPoint();
			controlPoint.setAutomaticControl(false);
			controlPoint.setControlPin(pump.getPin());
			controlPoint.setHasDuty(pump.isHasDuty());
			controlPoints.add(controlPoint);
		}

		List<Tank> tanks = layout.getTanks();
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
					List<TempSensor> sensors = status.getSensors();
					for (TempSensor hardwareSensor : sensors) {
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

	private class Monitor implements Runnable {

		@Override
		public void run() {
			long lastOnTime = 0;

			Random r = new Random();
			while (true) {

				ControlStep heaterStep = null;

				if (status != null) {
					List<TempSensor> sensors = status.getSensors();
					for (TempSensor hardwareSensor : sensors) {
						hardwareSensor.setTempatureC(hardwareSensor.getTempatureC() + (r.nextDouble() - 0.5));
					}

					List<ControlStep> steps = status.getSteps();
					if (steps != null) {

						synchronized (steps) {

							if (layout != null && steps.size() == 0) {
								steps.add(createManualStep("Default"));
							}

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
									break;
								} else if (Mode.HOLD.equals(status.getMode())) {
									lastOnTime = 0;
									heaterStep.setActive(true);
									break;
								} else if (Mode.OFF.equals(status.getMode())) {
									lastOnTime = 0;
									heaterStep.setActive(false);
									break;
								}

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
