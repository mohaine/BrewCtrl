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

import com.mohaine.brewcontroller.json.ListType;

public class ControllerStatus implements Cloneable {

	public enum Mode {
		ON, HOLD, OFF, UNKNOWN
	}

	private Mode mode = Mode.UNKNOWN;

	@ListType(HeaterStep.class)
	private List<HeaterStep> steps;

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public List<HeaterStep> getSteps() {
		return steps;
	}

	public void setSteps(List<HeaterStep> steps) {
		this.steps = steps;
	}

	public ControllerStatus getClone() throws CloneNotSupportedException {
		return (ControllerStatus) clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((steps == null) ? 0 : steps.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ControllerStatus other = (ControllerStatus) obj;
		if (mode != other.mode)
			return false;
		if (steps == null) {
			if (other.steps != null)
				return false;
		} else if (!steps.equals(other.steps))
			return false;
		return true;
	}

}
