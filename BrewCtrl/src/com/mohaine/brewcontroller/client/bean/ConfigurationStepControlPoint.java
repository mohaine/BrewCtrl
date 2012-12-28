package com.mohaine.brewcontroller.client.bean;

public class ConfigurationStepControlPoint {
	private Double targetTemp;
	private String targetName;
	private String controlName;

	public Double getTargetTemp() {
		return targetTemp;
	}

	public void setTargetTemp(Double targetTemp) {
		this.targetTemp = targetTemp;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getControlName() {
		return controlName;
	}

	public void setControlName(String controlName) {
		this.controlName = controlName;
	}

}
