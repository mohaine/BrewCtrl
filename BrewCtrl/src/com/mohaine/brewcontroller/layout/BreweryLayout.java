package com.mohaine.brewcontroller.layout;

import java.util.ArrayList;
import java.util.List;

public class BreweryLayout {
	private List<Tank> zones = new ArrayList<Tank>();
	private List<Pump> pumps = new ArrayList<Pump>();

	public List<Tank> getTanks() {
		return zones;
	}

	public void setZones(List<Tank> zone) {
		this.zones = zone;
	}

	public List<Pump> getPumps() {
		return pumps;
	}

	public void setPumps(List<Pump> pumps) {
		this.pumps = pumps;
	}

}
