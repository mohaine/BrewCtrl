package com.mohaine.brewcontroller.layout;

public class Sensor extends BreweryComponent {
	public static final String TYPE = "Sensor";
	private boolean reading;
	private double tempatureC;

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

	public double getTempatureC() {
		return tempatureC;
	}

	public void setTempatureC(double temp) {
		this.tempatureC = temp;
	}

}
