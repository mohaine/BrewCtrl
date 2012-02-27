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

	private static final String HLT_SENSOR_ADDRESS = "HLT_SENSOR_ADDRESS";
	private static final String TUN_SENSOR_ADDRESS = "TUN_SENSOR_ADDRESS";
	private static final String SENSOR_NAME = "SENSOR_NAME_";
	private static final String SENSOR_LOCATION = "SENSOR_LOCATION_";

	@Inject
	public BrewPrefs() {
	}

	public String getSensorName(String address, String defaultName) {
		Preferences prefs = Preferences.userNodeForPackage(BrewPrefs.class);
		byte[] prefValue = prefs.getByteArray(SENSOR_NAME + address, null);
		if (prefValue != null) {
			String value = new String(prefValue);
			return value;
		}

		return defaultName;
	}

	public void setSensorName(String address, String name) {
		Preferences prefs = Preferences.userNodeForPackage(BrewPrefs.class);
		if (name != null && name.length() > 0 && address != null && address.length() > 0) {
			prefs.putByteArray(SENSOR_NAME + address, name.getBytes());
		}
	}

	public String getSensorLocation(String address, String defaultLocation) {
		Preferences prefs = Preferences.userNodeForPackage(BrewPrefs.class);
		byte[] prefValue = prefs.getByteArray(SENSOR_LOCATION + address, null);
		if (prefValue != null) {
			String value = new String(prefValue);
			return value;
		}

		return defaultLocation;
	}

	public void setSensorLocation(String address, String location) {
		Preferences prefs = Preferences.userNodeForPackage(BrewPrefs.class);
		if (location != null && location.length() > 0 && address != null && address.length() > 0) {
			prefs.putByteArray(SENSOR_LOCATION + address, location.getBytes());
		}
	}

	
	public String getTunSensorAddress() {
		Preferences prefs = Preferences.userNodeForPackage(BrewPrefs.class);
		byte[] prefValue = prefs.getByteArray(TUN_SENSOR_ADDRESS, null);
		if (prefValue != null) {
			String value = new String(prefValue);
			return value;
		}
		return "";
	}

	public void setHltSensorAddress(String address) {
		Preferences prefs = Preferences.userNodeForPackage(BrewPrefs.class);
		prefs.putByteArray(HLT_SENSOR_ADDRESS, address.getBytes());
	}

	public String getHltSensorAddress() {
		Preferences prefs = Preferences.userNodeForPackage(BrewPrefs.class);
		byte[] prefValue = prefs.getByteArray(HLT_SENSOR_ADDRESS, null);
		if (prefValue != null) {
			String value = new String(prefValue);
			return value;
		}
		return "";
	}

	public void setTunSensorAddress(String address) {
		Preferences prefs = Preferences.userNodeForPackage(BrewPrefs.class);
		prefs.putByteArray(TUN_SENSOR_ADDRESS, address.getBytes());
	}

	

}
