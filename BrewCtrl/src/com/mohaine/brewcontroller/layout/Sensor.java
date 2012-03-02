package com.mohaine.brewcontroller.layout;

public class Sensor extends BreweryComponent {
	public static final String TYPE = "Sensor";
	private boolean reading;
	private Double tempatureC;

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

}
