package com.mohaine.brewcontroller.client.layout;

import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.shared.json.ListType;

public class BreweryLayout {
	@ListType(Tank.class)
	private List<Tank> tanks = new ArrayList<Tank>();
	@ListType(Pump.class)
	private List<Pump> pumps = new ArrayList<Pump>();

	private int maxAmps = 25;

	public int getMaxAmps() {
		return maxAmps;
	}

	public void setMaxAmps(int maxAmps) {
		this.maxAmps = maxAmps;
	}

	public void setTanks(List<Tank> tanks) {
		this.tanks = tanks;
	}

	public void setPumps(List<Pump> pumps) {
		this.pumps = pumps;
	}

	public List<Tank> getTanks() {
		return tanks;
	}

	public List<Pump> getPumps() {
		return pumps;
	}

	public Tank getTank(String name) {
		for (Tank tank : tanks) {
			if (tank.getName().equals(name)) {
				return tank;
			}
		}
		return null;

	}

	public BrewHardwareControl findBrewHardwareControlByNameOrParentName(String name) {
		// Find by name
		for (Tank tank : tanks) {
			HeatElement heater = tank.getHeater();
			if (heater != null && heater.getName() != null && heater.getName().equals(name)) {
				return heater;
			}
		}
		for (Pump pump : pumps) {
			if (pump != null && pump.getName() != null && pump.getName().equals(name)) {
				return pump;
			}
		}

		// Find by parent name
		for (Tank tank : tanks) {
			HeatElement heater = tank.getHeater();
			if (heater != null && tank.getName().equals(name)) {
				return heater;
			}
		}
		return null;
	}

	public Sensor findSensorByNameOrParentName(String name) {
		// Find by name
		for (Tank tank : tanks) {
			Sensor sensor = tank.getSensor();
			if (sensor != null && sensor.getName() != null && sensor.getName().equals(name)) {
				return sensor;
			}
		}

		// Find by parent name
		for (Tank tank : tanks) {
			Sensor sensor = tank.getSensor();
			if (sensor != null && tank.getName().equals(name)) {
				return sensor;
			}
		}
		return null;
	}

}
