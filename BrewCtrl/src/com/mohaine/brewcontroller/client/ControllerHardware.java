package com.mohaine.brewcontroller.client;

import java.util.List;

import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.bean.ControllerStatus;
import com.mohaine.brewcontroller.client.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.client.bean.TempSensor;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;

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

	public List<TempSensor> getSensors();
}
