package com.mohaine.brewcontroller.layout;

import java.util.ArrayList;
import java.util.List;

public class BrewLayout {
	private String name;
	private List<Zone> zones = new ArrayList<Zone>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Zone> getZones() {
		return zones;
	}

	public void setZones(List<Zone> zone) {
		this.zones = zone;
	}


}
