package com.mohaine.brewcontroller.bean;

public class ControlPoint implements Cloneable {

	private int controlPin;
	private int duty;
	private int fullOnAmps;
	private String tempSensorAddress = "0000000000000000";
	private double targetTemp;
	private boolean hasDuty;
	private boolean automaticControl;

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

	public int getControlPin() {
		return controlPin;
	}

	public void setControlPin(int i) {
		controlPin = i;
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

	public int getFullOnAmps() {
		return fullOnAmps;
	}

	public void setFullOnAmps(int fullOnAmps) {
		this.fullOnAmps = fullOnAmps;
	}

	public void setFullOnAmps(byte fullOnAmps) {
		this.fullOnAmps = (int) fullOnAmps & 0xff;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (automaticControl ? 1231 : 1237);
		result = prime * result + controlPin;
		result = prime * result + duty;
		result = prime * result + fullOnAmps;
		result = prime * result + (hasDuty ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(targetTemp);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((tempSensorAddress == null) ? 0 : tempSensorAddress.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ControlPoint other = (ControlPoint) obj;
		if (automaticControl != other.automaticControl)
			return false;
		if (controlPin != other.controlPin)
			return false;
		if (duty != other.duty)
			return false;
		if (fullOnAmps != other.fullOnAmps)
			return false;
		if (hasDuty != other.hasDuty)
			return false;
		if (Double.doubleToLongBits(targetTemp) != Double.doubleToLongBits(other.targetTemp))
			return false;
		if (tempSensorAddress == null) {
			if (other.tempSensorAddress != null)
				return false;
		} else if (!tempSensorAddress.equals(other.tempSensorAddress))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ControlPoint [controlPin=" + controlPin + ", duty=" + duty + ", tempSensorAddress=" + tempSensorAddress + ", targetTemp=" + targetTemp + ", hasDuty=" + hasDuty + ", automaticControl="
				+ automaticControl + "]";
	}

}
