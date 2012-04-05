package com.mohaine.brewcontroller.layout;

import java.util.ArrayList;
import java.util.List;

import com.mohaine.brewcontroller.json.ListType;

public class BreweryLayout {
	@ListType(Tank.class)
	private List<Tank> tanks = new ArrayList<Tank>();
	@ListType(Pump.class)
	private List<Pump> pumps = new ArrayList<Pump>();

	private int maxAmps = 25;
	private boolean turnOffOnCommLoss = false;

	public int getMaxAmps() {
		return maxAmps;
	}

	public void setMaxAmps(int maxAmps) {
		this.maxAmps = maxAmps;
	}

	public boolean isTurnOffOnCommLoss() {
		return turnOffOnCommLoss;
	}

	public void setTurnOffOnCommLoss(boolean turnOffOnCommLoss) {
		this.turnOffOnCommLoss = turnOffOnCommLoss;
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

}
