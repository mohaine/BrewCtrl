package com.mohaine.brewcontroller.client.layout;

public class Pump extends BrewHardwareControl {

	public static final String HLT_LOOP = "HLT Loop";

	public static final String TYPE = "Pump";

	@Override
	public String getType() {
		return TYPE;
	}

	public boolean isOn() {
		return getDuty() > 0;
	}

}
