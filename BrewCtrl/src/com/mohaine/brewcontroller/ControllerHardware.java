package com.mohaine.brewcontroller;

import java.util.List;

import com.mohaine.brewcontroller.bean.ControllerStatus;
import com.mohaine.brewcontroller.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.layout.BreweryLayout;

public interface ControllerHardware {

	public HeaterStep getSelectedStep();

	public void setSelectedStep(HeaterStep step);

	public HeaterStep createManualStep(String name);

	public List<HeaterStep> getSteps();

	public Mode getMode();

	public void changeMode(Mode mode);

	public void changeSteps(List<HeaterStep> steps);

	public BreweryLayout getBreweryLayout();

	public ControllerStatus getControllerStatus();

	public String getStatus();

	public Double getSensorTemp(String tunSensor);

	public List<HardwareSensor> getSensors();
}
