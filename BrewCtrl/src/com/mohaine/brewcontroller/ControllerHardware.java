package com.mohaine.brewcontroller;

import java.util.List;

import com.mohaine.brewcontroller.bean.ControllerStatus;
import com.mohaine.brewcontroller.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.bean.ControlStep;
import com.mohaine.brewcontroller.layout.BreweryLayout;

public interface ControllerHardware {

	public ControlStep getSelectedStep();

	public void setSelectedStep(ControlStep step);

	public ControlStep createManualStep(String name);

	public List<ControlStep> getSteps();

	public Mode getMode();

	public void changeMode(Mode mode);

	public void changeSteps(List<ControlStep> steps);

	public BreweryLayout getBreweryLayout();

	public ControllerStatus getControllerStatus();

	public String getStatus();

	public Double getSensorTemp(String tunSensor);

	public List<HardwareSensor> getSensors();
}
