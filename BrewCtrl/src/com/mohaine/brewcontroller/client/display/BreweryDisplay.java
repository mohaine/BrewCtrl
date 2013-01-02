package com.mohaine.brewcontroller.client.display;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.event.BreweryComponentChangeEvent;
import com.mohaine.brewcontroller.client.event.BreweryComponentChangeEventHandler;
import com.mohaine.brewcontroller.client.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.client.event.ChangeSelectedStepEventHandler;
import com.mohaine.brewcontroller.client.event.HandlerRegistration;
import com.mohaine.brewcontroller.client.event.StepModifyEvent;
import com.mohaine.brewcontroller.client.event.StepModifyEventHandler;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.client.layout.BreweryComponent;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.client.layout.HeatElement;
import com.mohaine.brewcontroller.client.layout.Pump;
import com.mohaine.brewcontroller.client.layout.Sensor;
import com.mohaine.brewcontroller.client.layout.Tank;

public class BreweryDisplay {

	List<BreweryComponentDisplay> displays = new ArrayList<BreweryComponentDisplay>();

	public interface BreweryDisplayDrawer {

		void setDisplays(List<BreweryComponentDisplay> displays, int width, int height);

		void redrawBreweryComponent(BreweryComponent component);

		void addMouseListener(DrawerMouseListener drawerMouseListener);

		void redrawAll();

		Object getWidget();
	}

	private BreweryDisplayDrawer drawer;
	// private BreweryLayout brewLayout;
	private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
	private ControllerHardware controller;

	private DragState dragState;
	private EventBus eventBus;

	@Inject
	public BreweryDisplay(BreweryDisplayDrawer drawer, EventBus eventBus, ControllerHardware controller) {
		this.drawer = drawer;
		this.controller = controller;
		this.eventBus = eventBus;

		drawer.addMouseListener(new DrawerMouseListener() {

			@Override
			public void mouseUp(DrawerMouseEvent e) {
				dragState = null;
			}

			@Override
			public void mouseDown(DrawerMouseEvent e) {

				dragState = new DragState();
				dragState.x = e.getX();
				dragState.y = e.getY();

				for (int i = displays.size() - 1; i > -1; i--) {
					BreweryComponentDisplay display = displays.get(i);

					int absLeft = display.getAbsLeft();
					int absTop = display.getAbsTop();
					if (dragState.x >= absLeft && dragState.x < absLeft + display.getWidth()) {
						if (dragState.y >= absTop && dragState.y < absTop + display.getHeight()) {
							dragState.display = display;

							break;
						}
					}
				}
				handleDragDown();
				dragState.lastTime = System.currentTimeMillis();
				dragState.lastX = dragState.x;
				dragState.lastY = dragState.y;
			}

			@Override
			public void mouseDragged(DrawerMouseEvent e) {
				if (dragState != null && dragState.display != null) {

					long time = System.currentTimeMillis() - dragState.lastTime;
					int deltaX = dragState.x - e.getX();
					int deltaY = dragState.y - e.getY();

					if (time < 300 && Math.abs(deltaX) < 15 && Math.abs(deltaY) < 15) {
						// System.out.println("   Ignore: " + time + "  " +
						// deltaX + "," + deltaY);
						return;
					}

					dragState.x = e.getX();
					dragState.y = e.getY();
					handleDrag();
					dragState.lastTime = System.currentTimeMillis();
					dragState.lastX = dragState.x;
					dragState.lastY = dragState.y;
				}
			}
		});

		handlers.add(eventBus.addHandler(BreweryComponentChangeEvent.getType(), new BreweryComponentChangeEventHandler() {
			@Override
			public void onStateChange(final BreweryComponent component) {
				BreweryDisplay.this.drawer.redrawBreweryComponent(component);
			}
		}));

		// handlers.add(eventBus.addHandler(StepsModifyEvent.getType(), new
		// StepsModifyEventHandler() {
		// @Override
		// public void onStepsChange() {
		// BreweryDisplay.this.drawer.redrawAll();
		// }
		// }));

		eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {

			@Override
			public void onStepChange(ControlStep step, boolean fromServer) {

				BreweryDisplay.this.drawer.redrawAll();

			}
		});

		handlers.add(eventBus.addHandler(ChangeSelectedStepEvent.getType(), new ChangeSelectedStepEventHandler() {
			@Override
			public void onStepChange(ControlStep step) {
				BreweryDisplay.this.drawer.redrawAll();
			}
		}));

	}

	private void handleDragDown() {
		if (dragState.display != null) {
			BreweryComponent component = dragState.display.getComponent();
			ControlStep selectedStep = controller.getSelectedStep();
			if (selectedStep != null) {
				if (component instanceof BrewHardwareControl) {
					BrewHardwareControl brewHardwareControl = (BrewHardwareControl) component;
					ControlPoint controlPoint = selectedStep.getControlPointForPin(brewHardwareControl.getPin());
					if (controlPoint != null && !controlPoint.isAutomaticControl()) {
						if (component instanceof Pump) {
							controlPoint.setDuty(controlPoint.getDuty() > 0 ? 0 : 100);
							BreweryDisplay.this.drawer.redrawBreweryComponent(component);
							eventBus.fireEvent(new StepModifyEvent(selectedStep));
						}
					}
					// } else if (component instanceof Sensor) {
					// Sensor sensor = (Sensor) component;
					// ControlPoint controlPoint =
					// selectedStep.getControlPointForAddress(sensor.getAddress());
					// if (controlPoint != null &&
					// !controlPoint.isAutomaticControl()) {
					// }
				}
			}
		}
	}

	private void handleDrag() {
		if (dragState.display != null) {
			double delta = dragState.lastY - dragState.y;

			long time = System.currentTimeMillis() - dragState.lastTime;
			if (time > 200 && Math.abs(delta) < 30) {
				delta = delta < 0 ? -1 : 1;
			} else if (time > 100 && Math.abs(delta) > 15) {
				delta = delta * 0.5;
			}

			BreweryComponent component = dragState.display.getComponent();
			ControlStep selectedStep = controller.getSelectedStep();
			if (selectedStep != null) {

				if (component instanceof BrewHardwareControl) {
					ControlPoint controlPoint = selectedStep.getControlPointForPin(((BrewHardwareControl) component).getPin());
					if (controlPoint != null && !controlPoint.isAutomaticControl()) {
						if (component instanceof HeatElement) {
							int newDuty = (int) (controlPoint.getDuty() + delta);
							if (newDuty < 0) {
								newDuty = 0;
							} else if (newDuty > 100) {
								newDuty = 100;
							}

							if (newDuty != controlPoint.getDuty()) {

								// System.out.println("   " +
								// controlPoint.getDuty() + " =>: " + newDuty +
								// " Delta: " + delta);

								controlPoint.setDuty(newDuty);
								BreweryDisplay.this.drawer.redrawBreweryComponent(component);
								eventBus.fireEvent(new StepModifyEvent(selectedStep));
							}
						}
					}
				} else if (component instanceof Sensor) {
					Sensor sensor = (Sensor) component;
					ControlPoint controlPoint = selectedStep.getControlPointForAddress(sensor.getAddress());
					if (controlPoint != null && controlPoint.isAutomaticControl()) {

						delta = delta * (5.0 / 9.0);

						double newTemp = controlPoint.getTargetTemp() + delta;

						if (newTemp < 0) {
							newTemp = 0;
						} else if (newTemp > 110) {
							newTemp = 110;
						}

						if (newTemp != controlPoint.getTargetTemp()) {
							controlPoint.setTargetTemp(newTemp);
							BreweryDisplay.this.drawer.redrawBreweryComponent(component);
							eventBus.fireEvent(new StepModifyEvent(selectedStep));
						}
					}
				}

			}
		}
	}

	public void setBreweryLayout(BreweryLayout brewLayout) {
		displays.clear();

		List<Tank> tanks = brewLayout.getTanks();

		for (Tank tank : tanks) {
			BreweryComponentDisplay tankBcd = createBcd(tank, 200, 200);

			if (tank.getHeater() != null) {
				BreweryComponentDisplay bcd = createBcd(tank.getHeater(), 98, 30);
				bcd.setTop(25);
				bcd.setLeft(102);
				bcd.setParent(tankBcd);
			}

			if (tank.getSensor() != null) {
				BreweryComponentDisplay bcd = createBcd(tank.getSensor(), 98, 60);
				bcd.setTop(25);
				bcd.setLeft(2);
				bcd.setParent(tankBcd);
			}

		}

		List<Pump> pumps = brewLayout.getPumps();
		for (Pump pump : pumps) {
			createBcd(pump, 100, 100);
		}

		layoutDisplays();
	}

	private BreweryComponentDisplay createBcd(BreweryComponent comp, int width, int height) {
		BreweryComponentDisplay display = new BreweryComponentDisplay(comp);
		display.setSize(width, height);
		displays.add(display);
		return display;
	}

	public void layoutDisplays() {
		int width = 0;
		int height = 0;
		int left = 5;
		int top = 5;

		for (BreweryComponentDisplay tank : getComponentForType(Tank.TYPE)) {
			tank.setLeft(left);
			tank.setTop(top);
			left += tank.getHeight();
			left += 5;

			width = Math.max(width, left);
			height = Math.max(height, top + tank.getHeight() + 5);

		}

		left = 5;
		top = 210;

		for (BreweryComponentDisplay pump : getComponentForType(Pump.TYPE)) {
			pump.setLeft(left);
			pump.setTop(top);
			left += pump.getHeight();
			left += 5;

			width = Math.max(width, left);
			height = Math.max(height, top + pump.getHeight() + 5);

		}
		drawer.setDisplays(displays, width, height);

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

	public Object getDisplay() {
		return drawer.getWidget();
	}

	public void cleanup() {
		if (handlers != null) {
			for (HandlerRegistration handler : handlers) {
				handler.removeHandler();
			}
			handlers.clear();
		}
	}

	private static class DragState {

		protected long lastTime;
		protected BreweryComponentDisplay display;
		protected int lastY;
		@SuppressWarnings("unused")
		protected int lastX;
		protected int y;
		protected int x;

	}

}
