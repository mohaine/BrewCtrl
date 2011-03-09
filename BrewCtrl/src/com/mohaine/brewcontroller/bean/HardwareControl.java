/*
    Copyright 2009-2011 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.mohaine.brewcontroller.bean;

public class HardwareControl {

	private HeaterMode mode;
	private String tunSensor;
	private String hltSensor;
	private double hltTargetTemp;
	private double tunTargetTemp;
	private int controlId;
	private int boilDuty;
	private boolean mashOn;

	public int getBoilDuty() {
		return boilDuty;
	}

	public void setBoilDuty(int boilDuty) {
		this.boilDuty = boilDuty;
	}

	public int getControlId() {
		return controlId;
	}

	public void setControlId(int controlId) {
		this.controlId = controlId;
	}

	public HeaterMode getMode() {
		return mode;
	}

	public void setMode(HeaterMode mode) {
		this.mode = mode;
	}

	public String getHltSensor() {
		return hltSensor;
	}

	public void setHltSensor(String limitSensorAddress) {
		this.hltSensor = limitSensorAddress;
	}

	public String getTunSensor() {
		return tunSensor;
	}

	public void setTunSensor(String sensorAddress) {
		this.tunSensor = sensorAddress;
	}

	public double getHltTargetTemp() {
		return hltTargetTemp;
	}

	public void setHltTargetTemp(double targetTemp) {
		this.hltTargetTemp = targetTemp;
	}

	public double getTunTargetTemp() {
		return tunTargetTemp;
	}

	public void setTunTargetTemp(double limitTemp) {
		this.tunTargetTemp = limitTemp;
	}

	public boolean isMashOn() {
		return mashOn;
	}

	public void setMashOn(boolean mashOn) {
		this.mashOn = mashOn;
	}

}
