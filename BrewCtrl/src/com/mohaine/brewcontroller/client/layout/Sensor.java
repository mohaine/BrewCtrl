package com.mohaine.brewcontroller.client.layout;

public class Sensor extends BreweryComponent {

	public static final String TYPE = "Sensor";
	private boolean reading;
	private Double tempatureC;
	private String address;

	@Override
	public String getType() {
		return TYPE;
	}

	public boolean isReading() {
		return reading;
	}

	public void setReading(boolean reading) {
		this.reading = reading;
	}

	public Double getTempatureC() {
		return tempatureC;
	}

	public void setTempatureC(Double temp) {
		this.tempatureC = temp;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
