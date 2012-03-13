package com.mohaine.brewcontroller.layout;

import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.bean.ControlPoint;

public class BreweryLayout {
	private List<Tank> zones = new ArrayList<Tank>();
	private List<Pump> pumps = new ArrayList<Pump>();

	public List<Tank> getTanks() {
		return zones;
	}

	public List<Pump> getPumps() {
		return pumps;
	}

}
