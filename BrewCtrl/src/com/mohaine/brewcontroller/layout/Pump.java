package com.mohaine.brewcontroller.layout;

public class Pump extends BrewHardwareControl {
	public static final String TYPE = "Pump";

	@Override
	public String getType() {
		return TYPE;
	}

	public boolean isOn() {
		return getDuty() > 0;
	}

}
