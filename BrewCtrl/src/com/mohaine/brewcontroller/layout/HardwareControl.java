package com.mohaine.brewcontroller.layout;

public abstract class HardwareControl extends BreweryComponent {
	private int pin;
	private int duty;
	private boolean canPulse;

	public int getDuty() {
		return duty;
	}

	public void setDuty(int duty) {
		this.duty = duty;
	}

	public int getPin() {
		return pin;
	}

	public void setPin(int pin) {
		this.pin = pin;
	}

	public boolean isHasDuty() {
		return canPulse;
	}

	public void setHasDuty(boolean hasDuty) {
		this.canPulse = hasDuty;
	}

}
