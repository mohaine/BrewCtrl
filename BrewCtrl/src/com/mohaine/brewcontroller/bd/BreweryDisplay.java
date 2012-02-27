package com.mohaine.brewcontroller.bd;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Tank;

public class BreweryDisplay {

	List<BreweryComponentDisplay> displays = new ArrayList<BreweryComponentDisplay>();

	public interface BreweryDisplayDrawer {

		int getHeight();

		int getWidth();

		void setDisplays(List<BreweryComponentDisplay> displays);

		void cleanup();

	}

	private BreweryDisplayDrawer drawer;
	private BreweryLayout brewLayout;

	@Inject
	public BreweryDisplay(BreweryDisplayDrawer drawer) {
		this.drawer = drawer;
	}

	public void setBreweryLayout(BreweryLayout brewLayout) {
		this.brewLayout = brewLayout;
		List<Tank> zones = brewLayout.getTanks();

		for (Tank zone : zones) {
			BreweryComponentDisplay display = new BreweryComponentDisplay(zone);
			display.setSize(200, 200);
			displays.add(display);
		}

		List<Pump> pumps = brewLayout.getPumps();
		for (Pump pump : pumps) {
			BreweryComponentDisplay display = new BreweryComponentDisplay(pump);
			display.setSize(100, 100);
			displays.add(display);
		}

		drawer.setDisplays(displays);

		layoutDisplays();
	}

	public void layoutDisplays() {
		int width = drawer.getWidth();
		int height = drawer.getHeight();

		int left = 5;
		int top = 5;

		for (BreweryComponentDisplay zoneView : getZonesForType(Tank.TYPE)) {
			zoneView.setLeft(left);
			zoneView.setTop(top);
			left += zoneView.getHeight();
			left += 5;
		}

		left = 5;
		top = 210;

		for (BreweryComponentDisplay pump : getZonesForType(Pump.TYPE)) {
			pump.setLeft(left);
			pump.setTop(top);
			left += pump.getHeight();
			left += 5;
		}

	}

	private List<BreweryComponentDisplay> getZonesForType(String type) {
		List<BreweryComponentDisplay> results = new ArrayList<BreweryComponentDisplay>();
		for (BreweryComponentDisplay display : displays) {
			if (display.getComponent().getType().equals(type)) {
				results.add(display);
			}
		}
		return results;
	}

	private List<BreweryComponentDisplay> getZonesForType(Class<Tank> class1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getDrawer() {
		return drawer;
	}

	public void cleanup() {
		drawer.cleanup();
	}

}
