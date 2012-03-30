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

import java.util.ArrayList;

public class HeaterStep {
	private String name;

	private ArrayList<ControlPoint> controlPoints = new ArrayList<ControlPoint>();

	private boolean active;

	private long stepTime = 0;
	private long extraCompletedTime = 0;
	private long lastStartTime = 0;

	public HeaterStep() {
		super();
	}

	public HeaterStep(String name, double tunTemp, long stepTime) {
		super();
		this.name = name;
		this.stepTime = stepTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStepTime() {
		return stepTime;
	}

	public void setStepTime(long stepTime) {
		this.stepTime = stepTime;
	}

	public long getLastStartTime() {
		return lastStartTime;
	}

	public void setLastStartTime(long lastStartTime) {
		this.lastStartTime = lastStartTime;
	}

	public long getTimeRemaining() {
		return stepTime - getTotalCompletedTime();
	}

	public void setTimeRemaining(long time) {
		stepTime = getTotalCompletedTime() + time;
	}

	public long getExtraCompletedTime() {
		return extraCompletedTime;
	}

	public void setExtraCompletedTime(long extraCompletedTime) {
		this.extraCompletedTime = extraCompletedTime;
	}

	public long getTotalCompletedTime() {
		long total = extraCompletedTime;
		if (lastStartTime > 0) {
			total += (System.currentTimeMillis() - lastStartTime);
		}
		return total;
	}

	public void stopTimer() {
		if (lastStartTime > 0) {
			extraCompletedTime += (System.currentTimeMillis() - lastStartTime);
		}
		lastStartTime = 0;
	}

	public void startTimer() {
		long now = System.currentTimeMillis();
		if (lastStartTime > 0) {
			extraCompletedTime += (now - lastStartTime);
		}
		lastStartTime = now;

	}

	public boolean isComplete() {
		return stepTime > 0 && getTotalCompletedTime() >= stepTime;
	}

	public ArrayList<ControlPoint> getControlPoints() {
		return controlPoints;
	}

	public void setControlPoints(ArrayList<ControlPoint> controlPoints) {
		this.controlPoints = controlPoints;
	}

	public ControlPoint getControlPointForPin(int pin) {
		for (ControlPoint cp : controlPoints) {
			if (cp.getControlPin() == pin) {
				return cp;
			}
		}
		return null;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public ControlPoint getControlPointForAddress(String address) {
		if (address != null) {
			for (ControlPoint cp : controlPoints) {
				if (address.equals(cp.getTempSensorAddress())) {
					return cp;
				}
			}
		}
		return null;
	}

}
