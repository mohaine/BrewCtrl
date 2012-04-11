package com.mohaine.brewcontroller;

import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.json.ListType;
import com.mohaine.brewcontroller.layout.BreweryLayout;

public class Configuration {

	private BreweryLayout brewLayout;

	@ListType(SensorConfiguration.class)
	private List<SensorConfiguration> sensors;

	public Configuration() {

	}

	public BreweryLayout getBrewLayout() {
		return brewLayout;
	}

	public void setBrewLayout(BreweryLayout brewLayout) {
		this.brewLayout = brewLayout;
	}

	public List<SensorConfiguration> getSensors() {
		return sensors;
	}

	public void setSensors(List<SensorConfiguration> sensors) {
		this.sensors = sensors;
	}

	public SensorConfiguration updateSensor(String address, String name, String locationName) {

		SensorConfiguration sensor = findSensor(address);

		if (sensor == null) {
			if (sensors == null) {
				sensors = new ArrayList<SensorConfiguration>();
			}
			sensor = new SensorConfiguration();
			sensor.setAddress(address);
			sensors.add(sensor);
		}

		sensor.setName(name);
		sensor.setLocation(locationName);

		return sensor;

	}

	public SensorConfiguration findSensor(String address) {
		SensorConfiguration sensor = null;
		if (sensors != null) {
			for (SensorConfiguration s : sensors) {
				if (s.getAddress().equals(address)) {
					sensor = s;
					break;
				}
			}
		}
		return sensor;
	}

}
