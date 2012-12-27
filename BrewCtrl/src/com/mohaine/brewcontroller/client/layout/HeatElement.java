package com.mohaine.brewcontroller.client.layout;

public class HeatElement extends BrewHardwareControl {

	public static final String TYPE = "Heater";
	public static final String MODE_OFF = "Off";
	public static final String MODE_PID = "Pid";
	public static final String MODE_DUTY = "DUTY";

	private String mode;
	private int fullOnAmps;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public int getFullOnAmps() {
		return fullOnAmps;
	}

	public void setFullOnAmps(int fullOnAmps) {
		this.fullOnAmps = fullOnAmps;
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
