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

import java.util.prefs.Preferences;

import com.google.inject.Inject;

public class BrewPrefs {

	private static final String SENSOR_NAME = "SENSOR_NAME_";
	private static final String SENSOR_LOCATION = "SENSOR_LOCATION_";
	private Preferences prefs;

	@Inject
	public BrewPrefs() {
		prefs = Preferences.userNodeForPackage(BrewPrefs.class);
	}

	public String getSensorName(String address, String defaultName) {
		byte[] prefValue = prefs.getByteArray(SENSOR_NAME + address, null);
		if (prefValue != null) {
			String value = new String(prefValue);
			return value;
		}

		return defaultName;
	}

	public void setSensorName(String address, String name) {
		if (name != null && name.length() > 0 && address != null && address.length() > 0) {
			prefs.putByteArray(SENSOR_NAME + address, name.getBytes());
		}
	}

	public String getSensorLocation(String address, String defaultLocation) {
		byte[] prefValue = prefs.getByteArray(SENSOR_LOCATION + address, null);
		if (prefValue != null) {
			String value = new String(prefValue);
			return value;
		}

		return defaultLocation;
	}

	public void setSensorLocation(String address, String location) {
		if (location != null && location.length() > 0 && address != null && address.length() > 0) {
			prefs.putByteArray(SENSOR_LOCATION + address, location.getBytes());
		}
	}

}
