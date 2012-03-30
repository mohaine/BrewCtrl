package com.mohaine.brewcontroller.bean;

public class ControlPoint implements Cloneable {

	private byte controlPin;
	private int duty;
	private String tempSensorAddress;
	private double targetTemp;
	private boolean hasDuty;
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

	public boolean isHasDuty() {
		return hasDuty;
	}

	public void setHasDuty(boolean hasDuty) {
		this.hasDuty = hasDuty;
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

	public int getDuty() {
		return duty;
	}

	public void setDuty(byte duty) {
		this.duty = (int) duty & 0xff;
	}

	public void setDuty(int duty) {
		this.duty = duty;
	}

	public ControlPoint getClone() {
		try {
			return (ControlPoint) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
