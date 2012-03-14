package com.mohaine.brewcontroller.bd;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.event.BreweryComponentChangeEvent;
import com.mohaine.brewcontroller.event.BreweryComponentChangeEventHandler;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEventHandler;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.brewcontroller.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.layout.BreweryComponent;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
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

		void addMouseListener(DrawerMouseListener drawerMouseListener);

		void redrawAll();
	}

	private BreweryDisplayDrawer drawer;
	// private BreweryLayout brewLayout;
	private HandlerRegistration handler;
	private Controller controller;

	private DragState dragState;
	private EventBus eventBus;

	@Inject
	public BreweryDisplay(BreweryDisplayDrawer drawer, EventBus eventBus, Controller controller) {
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
				dragState.startX = dragState.x;
				dragState.startY = dragState.y;

				for (int i = displays.size() - 1; i > -1; i--) {
					BreweryComponentDisplay display = displays.get(i);

					int absLeft = display.getAbsLeft();
					int absTop = display.getAbsTop();
					if (dragState.startX >= absLeft && dragState.startX < absLeft + display.getWidth()) {
						if (dragState.startY >= absTop && dragState.startY < absTop + display.getHeight()) {
							dragState.display = display;

							break;
						}
					}
				}
				handleDragDown();
			}

			@Override
			public void mouseDragged(DrawerMouseEvent e) {
				if (dragState != null && dragState.display != null) {
					dragState.x = e.getX();
					dragState.y = e.getY();
					handleDrag();
				}
			}
		});

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
		eventBus.addHandler(ChangeSelectedStepEvent.getType(), new ChangeSelectedStepEventHandler() {

			@Override
			public void onStepChange(HeaterStep step) {
				BreweryDisplay.this.drawer.redrawAll();
			}
		});

		// eventBus.addHandler(StepsModifyEvent.getType(), new
		// StepsModifyEventHandler() {
		// @Override
		// public void onStepsChange() {
		// System.out.println("BreweryDisplay.BreweryDisplay(...).new StepsModifyEventHandler() {...}.onStepsChange()");
		// }
		// });
		//
		// eventBus.addHandler(StepModifyEvent.getType(), new
		// StepModifyEventHandler() {
		// @Override
		// public void onStepChange(HeaterStep step) {
		// System.out.println("BreweryDisplay.BreweryDisplay(...).new StepModifyEventHandler() {...}.onStepChange()");
		// if (step != null) {
		// ArrayList<ControlPoint> controlPoints = step.getControlPoints();
		// for (ControlPoint controlPoint : controlPoints) {
		// System.out.println("controlPoint: " + controlPoint);
		// }
		// }
		// }
		// });

	}

	private void handleDragDown() {
		if (dragState.display != null) {
			BreweryComponent component = dragState.display.getComponent();
			HeaterStep selectedStep = controller.getSelectedStep();
			if (selectedStep != null) {
				if (component instanceof BrewHardwareControl) {
					ControlPoint controlPoint = selectedStep.getControlPointForPin(((BrewHardwareControl) component).getPin());
					if (controlPoint != null && !controlPoint.isAutomaticControl()) {
						if (component instanceof Pump) {
							controlPoint.setDuty(controlPoint.getDuty() > 0 ? 0 : 100);
							BreweryDisplay.this.drawer.redrawBreweryComponent(component);
							eventBus.fireEvent(new StepModifyEvent(selectedStep));
						} else if (component instanceof HeatElement) {
							dragState.startValue = controlPoint.getDuty();
						}
					}
				} else if (component instanceof Sensor) {
					Sensor sensor = (Sensor) component;
					ControlPoint controlPoint = selectedStep.getControlPointForAddress(sensor.getAddress());
					if (controlPoint != null && !controlPoint.isAutomaticControl()) {
						dragState.startValue = controlPoint.getTargetTemp();
					}
				}
			}
		}
	}

	private void handleDrag() {
		if (dragState.display != null) {
			double delta = dragState.startY - dragState.y;

			BreweryComponent component = dragState.display.getComponent();
			HeaterStep selectedStep = controller.getSelectedStep();
			if (selectedStep != null) {
				if (component instanceof BrewHardwareControl) {
					ControlPoint controlPoint = selectedStep.getControlPointForPin(((BrewHardwareControl) component).getPin());
					if (controlPoint != null && !controlPoint.isAutomaticControl()) {
						if (component instanceof HeatElement) {
							int newDuty = (int) (dragState.startValue + delta);
							if (newDuty < 0) {
								newDuty = 0;
								dragState.startY = dragState.y;
								dragState.startValue = 0;
							} else if (newDuty > 100) {
								newDuty = 100;
								dragState.startY = dragState.y;
								dragState.startValue = 100;
							}

							if (newDuty != controlPoint.getDuty()) {
								controlPoint.setDuty(newDuty);
								BreweryDisplay.this.drawer.redrawBreweryComponent(component);
								eventBus.fireEvent(new StepModifyEvent(selectedStep));
							}
						}
					}
				} else if (component instanceof Sensor) {
					Sensor sensor = (Sensor) component;
					ControlPoint controlPoint = selectedStep.getControlPointForAddress(sensor.getAddress());
					if (controlPoint != null && !controlPoint.isAutomaticControl()) {

						delta = delta * (5.0 / 9.0);

						double newTemp = dragState.startValue + delta;

						if (newTemp < 0) {
							newTemp = 0;
							dragState.startY = dragState.y;
							dragState.startValue = 0;
						} else if (newTemp > 110) {
							newTemp = 110;
							dragState.startY = dragState.y;
							dragState.startValue = 110;
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
		// this.brewLayout = brewLayout;
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
		drawer.setDisplays(displays);

		layoutDisplays();
	}

	private BreweryComponentDisplay createBcd(BreweryComponent comp, int width, int height) {
		BreweryComponentDisplay display = new BreweryComponentDisplay(comp);
		display.setSize(width, height);
		displays.add(display);
		return display;
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

	private static class DragState {

		public double startValue;
		protected BreweryComponentDisplay display;
		protected int startY;
		protected int startX;
		protected int y;
		protected int x;

	}

}
