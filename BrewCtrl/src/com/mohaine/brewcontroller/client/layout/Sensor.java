package com.mohaine.brewcontroller.client.layout;

import com.mohaine.brewcontroller.client.bean.TempSensor;

public class Sensor extends BreweryComponent {

	public static final String TYPE = "Sensor";

	private String address = "";
	private double tempatureC;
	private boolean reading = false;

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public double getTempatureC() {
		return tempatureC;
	}

	public void setTempatureC(double tempatureC) {
		this.tempatureC = tempatureC;
	}

	public boolean isReading() {
		return reading;
	}

	public void setReading(boolean reading) {
		this.reading = reading;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public void updateFrom(TempSensor tankTs) {
		this.address = tankTs.getAddress();
		this.reading = tankTs.isReading();
		this.tempatureC = tankTs.getTempatureC();
	}

}
