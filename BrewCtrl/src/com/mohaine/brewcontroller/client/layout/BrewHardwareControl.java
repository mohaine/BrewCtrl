package com.mohaine.brewcontroller.client.layout;

public abstract class BrewHardwareControl extends BreweryComponent {
	private int io;
	private int duty;
	private boolean hasDuty;

	public int getDuty() {
		return duty;
	}

	public void setDuty(int duty) {
		this.duty = duty;
	}

	public int getIo() {
		return io;
	}

	public void setIo(int io) {
		this.io = io;
	}

	public boolean isHasDuty() {
		return hasDuty;
	}

	public void setHasDuty(boolean hasDuty) {
		this.hasDuty = hasDuty;
	}

}
