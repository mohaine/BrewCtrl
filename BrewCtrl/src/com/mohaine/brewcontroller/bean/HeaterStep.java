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
 aint with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.mohaine.brewcontroller.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mohaine.brewcontroller.json.ListType;

public class HeaterStep {
	private static final long START_TIME = System.currentTimeMillis();
	private String name;
	private String id = UUID.randomUUID().toString();

	@ListType(ControlPoint.class)
	private List<ControlPoint> controlPoints = new ArrayList<ControlPoint>();

	private int stepTime = 0;
	private int extraCompletedTime = 0;
	private int lastStartTime = 0;

	public HeaterStep() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStepTime() {
		return stepTime;
	}

	public void setStepTime(int stepTime) {
		this.stepTime = stepTime;
	}

	public int getLastStartTime() {
		return lastStartTime;
	}

	public void setLastStartTime(int lastStartTime) {
		this.lastStartTime = lastStartTime;
	}

	public int getTimeRemaining() {
		return stepTime - getTotalCompletedTime();
	}

	public void setTimeRemaining(int time) {
		stepTime = getTotalCompletedTime() + time;
	}

	public int getExtraCompletedTime() {
		return extraCompletedTime;
	}

	public void setExtraCompletedTime(int extraCompletedTime) {
		this.extraCompletedTime = extraCompletedTime;
	}

	public int getTotalCompletedTime() {
		int total = extraCompletedTime;
		if (lastStartTime > 0) {
			total += (getMillis() - lastStartTime);
		}
		return total;
	}

	public void stopTimer() {
		if (lastStartTime > 0) {
			extraCompletedTime += (getMillis() - lastStartTime);
		}
		lastStartTime = 0;
	}

	public void startTimer() {
		int now = (int) getMillis();
		if (lastStartTime > 0) {
			throw new RuntimeException("Tried to to start stared");
		}
		lastStartTime = now;

	}

	private long getMillis() {
		return System.currentTimeMillis() - START_TIME;
	}

	public boolean isComplete() {
		return stepTime > 0 && getTotalCompletedTime() >= stepTime;
	}

	public List<ControlPoint> getControlPoints() {
		return controlPoints;
	}

	public void setControlPoints(List<ControlPoint> controlPoints) {
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

	public boolean isStarted() {
		return lastStartTime > 0;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public boolean equals(HeaterStep obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HeaterStep other = (HeaterStep) obj;
		if (controlPoints == null) {
			if (other.controlPoints != null)
				return false;
		} else if (!controlPoints.equals(other.controlPoints))
			return false;
		if (extraCompletedTime != other.extraCompletedTime)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastStartTime != other.lastStartTime)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;

		if (stepTime != other.stepTime) {
			return false;
		}
		return true;
	}

}
