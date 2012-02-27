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

import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareStatus;
import com.mohaine.brewcontroller.bean.HeaterMode;
import com.mohaine.brewcontroller.bean.TempSensor;

public class HardwareMock extends HardwareBase implements Hardware, Runnable {

	private double duty;
	private double maxDuty;
	private boolean run = true;
	private String status = "Ok";

	private HeaterMode mode = HeaterMode.OFF;

	@Inject
	public HardwareMock(BrewPrefs prefs) {

		List<TempSensor> tempSensors = getSensors();

		tempSensors.add(new TempSensor("ABCD"));
		tempSensors.add(new TempSensor("EFGH"));

		int count = 1;
		for (TempSensor sensor : tempSensors) {
			sensor.setName(prefs.getSensorName(sensor.getAddress(), "Sensor " + count));

			sensor.setTempatureC(30);

			count++;
		}

		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void run() {
		int loopCount = 0;

		while (run) {
			loopCount++;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			HardwareStatus hardwareStatus = getHardwareStatus();
			hardwareStatus.setMode(mode);
			if (loopCount % 5 == 0) {
				switch (mode) {
				case OFF:
					duty = 0;
					break;
				case ON:
					duty = maxDuty + Math.round(2 * Math.random() - 1);
					if (duty > maxDuty) {
						duty = maxDuty;
					}
					if (duty < 0) {
						duty = 0;
					}

					for (TempSensor sensor : getSensors()) {
						sensor.setTempatureC(sensor.getTempatureC() + Math.random());
					}
					break;
				}

				fireStateChangeHandlers();
			}
		}

	}

	@Override
	public void setHardwareControl(final HardwareControl hc) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
				maxDuty = hc.getHltTargetTemp();
				mode = hc.getMode();
			}
		}).start();

	}

	@Override
	public String getStatus() {
		return status;
	}

}
