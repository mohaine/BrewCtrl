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

package com.mohaine.brewcontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mohaine.brewcontroller.bean.HardwareStatus;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.StatusChangeHandler;

public abstract class HardwareBase implements Hardware {
	private HardwareStatus hardwareStatus = new HardwareStatus();
	private ArrayList<HardwareSensor> tempSensors = new ArrayList<HardwareSensor>();
	private List<StatusChangeHandler> statusChangeHandlers = Collections.synchronizedList(new ArrayList<StatusChangeHandler>());

	@Override
	public List<HardwareSensor> getSensors() {
		return tempSensors;
	}

	@Override
	public HardwareStatus getHardwareStatus() {
		return hardwareStatus;
	}

	@Override
	public void fireStateChangeHandlers() {
		for (StatusChangeHandler handler : statusChangeHandlers) {
			handler.onStateChange();
		}
	}

	@Override
	public HandlerRegistration addStatusChangeHandler(final StatusChangeHandler handler) {
		statusChangeHandlers.add(handler);

		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				statusChangeHandlers.remove(handler);
			}
		};
	}

	@Override
	public Double getSensorTemp(String sensorAddress) {
		List<HardwareSensor> sensors = getSensors();
		for (int i = 0; i < sensors.size(); i++) {
			HardwareSensor tempSensor = sensors.get(i);
			if (tempSensor.getAddress().equals(sensorAddress)) {
				return tempSensor.getTempatureC();
			}
		}
		return null;
	}
}
