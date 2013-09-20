package com.mohaine.brewcontroller.client.layout;

import com.mohaine.brewcontroller.client.bean.TempSensor;

public class Sensor extends BreweryComponent {

	public static final String TYPE = "Sensor";

	private String address = "";
	private double temperatureC;
	private boolean reading = false;

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public double getTemperatureC() {
		return temperatureC;
	}

	public void setTemperatureC(double temperatureC) {
		this.temperatureC = temperatureC;
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
		this.temperatureC = tankTs.getTemperatureC();
	}

}
