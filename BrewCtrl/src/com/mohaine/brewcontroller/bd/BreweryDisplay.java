package com.mohaine.brewcontroller.bd;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.event.BreweryComponentChangeEvent;
import com.mohaine.brewcontroller.event.BreweryComponentChangeEventHandler;
import com.mohaine.brewcontroller.layout.BreweryComponent;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.bus.EventBus;

public class BreweryDisplay {

	List<BreweryComponentDisplay> displays = new ArrayList<BreweryComponentDisplay>();

	public interface BreweryDisplayDrawer {

		int getHeight();

		int getWidth();

		void setDisplays(List<BreweryComponentDisplay> displays);

		void redrawBreweryComponent(BreweryComponent component);

	}

	private BreweryDisplayDrawer drawer;
	private BreweryLayout brewLayout;
	private HandlerRegistration handler;

	@Inject
	public BreweryDisplay(BreweryDisplayDrawer drawer, EventBus eventBus) {
		this.drawer = drawer;

		handler = eventBus.addHandler(BreweryComponentChangeEvent.getType(), new BreweryComponentChangeEventHandler() {
			@Override
			public void onStateChange(final BreweryComponent component) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						BreweryDisplay.this.drawer.redrawBreweryComponent(component);
					}
				});
			}
		});

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

		int left = 5;
		int top = 5;

		for (BreweryComponentDisplay tank : getComponentForType(Tank.TYPE)) {
			tank.setLeft(left);
			tank.setTop(top);
			left += tank.getHeight();
			left += 5;
		}

		left = 5;
		top = 210;

		for (BreweryComponentDisplay pump : getComponentForType(Pump.TYPE)) {
			pump.setLeft(left);
			pump.setTop(top);
			left += pump.getHeight();
			left += 5;
		}

	}

	private List<BreweryComponentDisplay> getComponentForType(String type) {
		List<BreweryComponentDisplay> results = new ArrayList<BreweryComponentDisplay>();
		for (BreweryComponentDisplay display : displays) {
			if (display.getComponent().getType().equals(type)) {
				results.add(display);
			}
		}
		return results;
	}

	public Object getDrawer() {
		return drawer;
	}

	public void cleanup() {
		if (handler != null) {
			handler.removeHandler();
			handler = null;
		}
	}
}
