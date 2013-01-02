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

package com.mohaine.brewcontroller.client.bean;

import java.util.List;

import com.mohaine.brewcontroller.shared.json.ListType;

public class ControllerStatus {

	public enum Mode {
		ON, HOLD, OFF, UNKNOWN;

		public boolean equals(String s) {
			return toString().equals(s);
		}
	}

	private String mode = Mode.UNKNOWN.toString();
	private int configurationVersion;

	@ListType(ControlStep.class)
	private List<ControlStep> steps;

	@ListType(TempSensor.class)
	List<TempSensor> sensors;

	public String getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode.toString();
	}

	public void setMode(String mode) {
		this.mode = mode.toString();
	}

	public List<ControlStep> getSteps() {
		return steps;
	}

	public void setSteps(List<ControlStep> steps) {
		this.steps = steps;
	}

	public List<TempSensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<TempSensor> sensors) {
		this.sensors = sensors;
	}

	public int getConfigurationVersion() {
		return configurationVersion;
	}

	public void setConfigurationVersion(int configurationVersion) {
		this.configurationVersion = configurationVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configurationVersion;
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((sensors == null) ? 0 : sensors.hashCode());
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
		if (configurationVersion != other.configurationVersion)
			return false;
		if (mode == null) {
			if (other.mode != null)
				return false;
		} else if (!mode.equals(other.mode))
			return false;
		if (sensors == null) {
			if (other.sensors != null)
				return false;
		} else if (!sensors.equals(other.sensors))
			return false;
		if (steps == null) {
			if (other.steps != null)
				return false;
		} else if (!steps.equals(other.steps))
			return false;
		return true;
	}

}
