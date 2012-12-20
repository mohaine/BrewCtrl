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

import java.util.List;

public class HardwareControl implements Cloneable {
	private HeaterMode mode = HeaterMode.UNKNOWN;
	private long controlId;
	private long millis;
	private int maxAmps;
	private boolean turnOffOnCommLoss;

	private List<ControlPoint> controlPoints;

	public long getControlId() {
		return controlId;
	}

	public void setControlId(long controlId) {
		this.controlId = controlId;
	}

	public int getMaxAmps() {
		return maxAmps;
	}

	public void setMaxAmps(int maxAmps) {
		this.maxAmps = maxAmps;
	}

	public void setMaxAmps(byte maxAmps) {
		this.maxAmps = (int) maxAmps & 0xff;
	}

	public boolean isTurnOffOnCommLoss() {
		return turnOffOnCommLoss;
	}

	public void setTurnOffOnCommLoss(boolean turnOffOnCommLoss) {
		this.turnOffOnCommLoss = turnOffOnCommLoss;
	}

	public HeaterMode getMode() {
		return mode;
	}

	public void setMode(HeaterMode mode) {
		this.mode = mode;
	}

	public List<ControlPoint> getControlPoints() {
		return controlPoints;
	}

	public void setControlPoints(List<ControlPoint> controlPoints) {
		this.controlPoints = controlPoints;
	}

	public long getMillis() {
		return millis;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public HardwareControl getClone() throws CloneNotSupportedException {
		return (HardwareControl) clone();
	}

}
