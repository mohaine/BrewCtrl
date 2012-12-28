package com.mohaine.brewcontroller.client.bean;

import java.util.List;

import com.mohaine.brewcontroller.shared.json.ListType;

public class ConfigurationStep {
	private String name;
	private String time;

	@ListType(ConfigurationStepControlPoint.class)
	private List<ConfigurationStepControlPoint> controlPoints;

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

	public List<ConfigurationStepControlPoint> getControlPoints() {
		return controlPoints;
	}

	public void setControlPoints(List<ConfigurationStepControlPoint> controlPoints) {
		this.controlPoints = controlPoints;
	}

}
