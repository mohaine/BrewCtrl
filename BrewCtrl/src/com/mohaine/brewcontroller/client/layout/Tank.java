package com.mohaine.brewcontroller.client.layout;

public class Tank extends BreweryComponent {

	public static final String TYPE = "Tank";

	@Override
	public String getType() {
		return TYPE;
	}

	private String sensorAddress;

	private Sensor sensor;
	private HeatElement heater;

	public String getSensorAddress() {
		return sensorAddress;
	}

	public void setSensorAddress(String sensorAddress) {
		this.sensorAddress = sensorAddress;
	}

	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public HeatElement getHeater() {
		return heater;
	}

	public void setHeater(HeatElement heater) {
		this.heater = heater;
	}

}
