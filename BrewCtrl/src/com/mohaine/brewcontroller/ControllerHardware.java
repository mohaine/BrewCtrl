package com.mohaine.brewcontroller;

import java.util.List;

import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareControl.Mode;
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

	public List<HardwareSensor> getSensors();

	public HardwareControl getHardwareStatus();

	public String getStatus();

	public Double getSensorTemp(String tunSensor);
}
