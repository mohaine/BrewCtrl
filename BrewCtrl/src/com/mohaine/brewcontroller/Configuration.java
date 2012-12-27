package com.mohaine.brewcontroller;

import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.shared.json.ListType;

public class Configuration {

	private BreweryLayout brewLayout;
	private String commPorts;
	private boolean logMessages;

	@ListType(SensorConfiguration.class)
	private List<SensorConfiguration> sensors;

	@ListType(ConfigurationStepList.class)
	private List<ConfigurationStepList> stepLists;

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

	public List<ConfigurationStepList> getStepLists() {
		return stepLists;
	}

	public void setStepLists(List<ConfigurationStepList> stepLists) {
		System.out.println("Configuration.setStepLists()" + stepLists);
		this.stepLists = stepLists;
	}

	public String getCommPorts() {
		return commPorts;
	}

	public void setCommPorts(String commPorts) {
		this.commPorts = commPorts;
	}

	public boolean isLogMessages() {
		return logMessages;
	}

	public void setLogMessages(boolean logMessages) {
		this.logMessages = logMessages;
	}

}
