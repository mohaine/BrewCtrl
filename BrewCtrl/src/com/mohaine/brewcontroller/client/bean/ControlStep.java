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

package com.mohaine.brewcontroller.client.bean;

import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.shared.json.ListType;

public class ControlStep {
	private String name;
	private String id = generateRandomId();

	public static String generateRandomId() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			sb.append((char) (0x41 + Math.floor(Math.random() * 26)));
		}
		return sb.toString();
	}

	@ListType(ControlPoint.class)
	private List<ControlPoint> controlPoints = new ArrayList<ControlPoint>();

	private int stepTime = 0;
	private boolean active;

	public ControlStep() {
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

	public boolean equals(ControlStep obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ControlStep other = (ControlStep) obj;
		if (controlPoints == null) {
			if (other.controlPoints != null)
				return false;
		} else if (!controlPoints.equals(other.controlPoints))
			return false;

		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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

	public void copyFrom(ControlStep modStep) {
		this.controlPoints = modStep.controlPoints;
		this.id = modStep.id;
		this.name = modStep.name;
		this.stepTime = modStep.stepTime;
		this.active = modStep.active;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean started) {
		this.active = started;
	}

}
