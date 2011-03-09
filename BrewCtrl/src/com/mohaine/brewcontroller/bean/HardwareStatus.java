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

public class HardwareStatus {

	private HeaterMode mode = HeaterMode.OFF;
	private int hltDuty;
	private int boilDuty;
	private boolean pumpOn;
	private int controlId;

	private String tunSensor;
	private String hltSensor;
	private double hltTargetTemp;
	private double tunTargetTemp;
	private boolean mashOn;

	public boolean isMashOn() {
		return mashOn;
	}

	public void setMashOn(boolean mashOn) {
		this.mashOn = mashOn;
	}

	public HeaterMode getMode() {
		return mode;
	}

	public void setMode(HeaterMode mode) {
		this.mode = mode;
	}

	public int getHltDuty() {
		return hltDuty;
	}

	public void setHltDuty(int maxDuty) {
		this.hltDuty = maxDuty;
	}

	public int getBoilDuty() {
		return boilDuty;
	}

	public void setBoilDuty(int duty) {
		this.boilDuty = duty;
	}

	public int getControlId() {
		return controlId;
	}

	public void setControlId(int controlId) {
		this.controlId = controlId;
	}

	public boolean isPumpOn() {
		return pumpOn;
	}

	public void setPumpOn(boolean pumpOn) {
		this.pumpOn = pumpOn;
	}

	public String getTunSensor() {
		return tunSensor;
	}

	public void setTunSensor(String tunSensor) {
		this.tunSensor = tunSensor;
	}

	public String getHltSensor() {
		return hltSensor;
	}

	public void setHltSensor(String hltSensor) {
		this.hltSensor = hltSensor;
	}

	public double getHltTargetTemp() {
		return hltTargetTemp;
	}

	public void setHltTargetTemp(double hltTargetTemp) {
		this.hltTargetTemp = hltTargetTemp;
	}

	public double getTunTargetTemp() {
		return tunTargetTemp;
	}

	public void setTunTargetTemp(double tunTargetTemp) {
		this.tunTargetTemp = tunTargetTemp;
	}

}
