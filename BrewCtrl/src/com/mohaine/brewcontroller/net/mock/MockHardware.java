package com.mohaine.brewcontroller.net.mock;

import java.util.List;

import com.mohaine.brewcontroller.bean.ControllerStatus;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.layout.BreweryLayout;

public class MockHardware {

	private ControllerStatus status;
	private BreweryLayout layout;

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

}
