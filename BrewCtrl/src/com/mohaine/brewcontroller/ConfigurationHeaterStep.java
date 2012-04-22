package com.mohaine.brewcontroller;

import java.util.List;

import com.mohaine.brewcontroller.json.ListType;

public class ConfigurationHeaterStep {
	private String name;
	private String time;

	@ListType(ConfigurationHeaterStepControlPoint.class)
	private List<ConfigurationHeaterStepControlPoint> controlPoints;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public List<ConfigurationHeaterStepControlPoint> getControlPoints() {
		return controlPoints;
	}

	public void setControlPoints(List<ConfigurationHeaterStepControlPoint> controlPoints) {
		this.controlPoints = controlPoints;
	}

}
