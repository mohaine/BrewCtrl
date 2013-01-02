package com.mohaine.brewcontroller.client.layout;

import com.mohaine.brewcontroller.client.bean.TempSensor;

public class Sensor extends BreweryComponent {

	public static final String TYPE = "Sensor";

	private TempSensor sensor;

	@Override
	public String getType() {
		return TYPE;
	}

	public boolean isReading() {
		return sensor != null ? sensor.isReading() : false;
	}

	public Double getTempatureC() {
		return sensor != null ? sensor.getTempatureC() : null;

	}

	public String getAddress() {
		return sensor != null ? sensor.getAddress() : "";
	}

	public TempSensor getSensor() {
		return sensor;
	}

	public void setSensor(TempSensor sensor) {
		this.sensor = sensor;
	}

}
