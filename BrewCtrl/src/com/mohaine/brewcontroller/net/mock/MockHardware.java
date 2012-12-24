package com.mohaine.brewcontroller.net.mock;

import java.util.List;

import com.mohaine.brewcontroller.bean.ControllerStatus;
import com.mohaine.brewcontroller.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.layout.BreweryLayout;

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

	public void setSteps(List<HeaterStep> steps) {
		status.setSteps(steps);
	}

	private class Monitor implements Runnable {

		@Override
		public void run() {
			while (true) {
				HeaterStep heaterStep = null;

				if (status != null) {
					List<HeaterStep> steps = status.getSteps();
					if (steps != null) {

						synchronized (steps) {
							if (steps.size() > 0) {
								heaterStep = steps.get(0);

							}
						}
						if (heaterStep != null) {
							synchronized (heaterStep) {
								switch (status.getMode()) {
								case ON: {

									if (!heaterStep.isStarted()) {
										heaterStep.startTimer();
									} else {
										if (heaterStep.isComplete()) {
											synchronized (steps) {
												status.getSteps().remove(heaterStep);
											}
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
									break;
								}
								default:
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

			}
		}
	}

	public void updateStep(HeaterStep modStep) {
		List<HeaterStep> steps = status.getSteps();
		if (steps != null) {
			synchronized (steps) {
				for (HeaterStep step : steps) {
					if (step.getId().equals(modStep.getId())) {
						step.setName(modStep.getName());
						step.setStepTime(modStep.getStepTime());
					}
				}
			}
		}
	}
}
