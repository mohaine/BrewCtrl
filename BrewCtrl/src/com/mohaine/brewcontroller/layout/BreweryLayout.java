package com.mohaine.brewcontroller.layout;

import java.util.ArrayList;
import java.util.List;

public class BreweryLayout {
	private final List<Tank> tanks = new ArrayList<Tank>();
	private final List<Pump> pumps = new ArrayList<Pump>();

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

}
