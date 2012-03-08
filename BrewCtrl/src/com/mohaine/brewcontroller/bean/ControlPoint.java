package com.mohaine.brewcontroller.bean;

public class ControlPoint {

	private byte controlPin;
	private String tempSensorAddress;
	private double targetTemp;
	private boolean mechanical;
	private boolean automaticControl;

	public byte getControlPin() {
		return controlPin;
	}

	public void setControlPin(byte controlPin) {
		this.controlPin = controlPin;
	}

	public String getTempSensorAddress() {
		return tempSensorAddress;
	}

	public void setTempSensorAddress(String tempSensorAddress) {
		this.tempSensorAddress = tempSensorAddress;
	}

	public double getTargetTemp() {
		return targetTemp;
	}

	public void setTargetTemp(double targetTemp) {
		this.targetTemp = targetTemp;
	}

	public boolean isMechanical() {
		return mechanical;
	}

	public void setMechanical(boolean mechanical) {
		this.mechanical = mechanical;
	}

	public boolean isAutomaticControl() {
		return automaticControl;
	}

	public void setAutomaticControl(boolean automaticControl) {
		this.automaticControl = automaticControl;
	}

	public void setControlPin(int i) {
		setControlPin((byte) i);
	}

	// TODO PID VALUES

}
