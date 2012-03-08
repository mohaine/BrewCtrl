package com.mohaine.brewcontroller.layout;

public class HeatElement extends HardwareControl {

	public static final String TYPE = "Heater";
	public static final String MODE_OFF = "Off";
	public static final String MODE_PID = "Pid";
	public static final String MODE_DUTY = "DUTY";

	String mode;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
