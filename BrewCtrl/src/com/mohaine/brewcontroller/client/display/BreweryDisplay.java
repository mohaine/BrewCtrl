package com.mohaine.brewcontroller.client.display;

/*
 Copyright 2009-2013 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.TempFConverter;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.display.BreweryComponentDisplay.DisplayType;
import com.mohaine.brewcontroller.client.display.Scheduler.Cancelable;
import com.mohaine.brewcontroller.client.display.Scheduler.RunRepeat;
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

	private static final int MAX_TEMP = 110;

	private static final int ARROW_SIZE = 15;

	List<BreweryComponentDisplay> displays = new ArrayList<BreweryComponentDisplay>();

	private BreweryDisplayDrawer<?> drawer;
	// private BreweryLayout brewLayout;
	private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
	private ControllerHardware controller;

	private MouseState mouseState;
	private EventBus eventBus;
	private Scheduler scheduler;

	@Inject
	public BreweryDisplay(@SuppressWarnings("rawtypes") BreweryDisplayDrawer drawer, EventBus eventBus, ControllerHardware controller, Scheduler scheduler) {
		this.drawer = drawer;
		this.controller = controller;
		this.eventBus = eventBus;

		this.scheduler = scheduler;

		drawer.addMouseListener(new DrawerMouseListener() {

			@Override
			public void mouseUp(DrawerMouseEvent e) {
				if (mouseState != null) {
					handleUp();
				}
				mouseState = null;
			}

			@Override
			public void mouseDown(DrawerMouseEvent e) {

				if (mouseState != null) {
					Cancelable whileDown = mouseState.whileDown;
					if (whileDown != null) {
						whileDown.cancel();
					}
					mouseState.whileDown = null;
					mouseState = null;
				}
				mouseState = new MouseState();
				mouseState.x = e.getX();
				mouseState.y = e.getY();

				for (int i = displays.size() - 1; i > -1; i--) {
					BreweryComponentDisplay display = displays.get(i);

					int absLeft = display.getAbsLeft();
					int absTop = display.getAbsTop();
					if (mouseState.x >= absLeft && mouseState.x < absLeft + display.getWidth()) {
						if (mouseState.y >= absTop && mouseState.y < absTop + display.getHeight()) {
							mouseState.display = display;
							break;
						}
					}
				}
				handleDown();
				mouseState.startTime = System.currentTimeMillis();
				mouseState.lastTime = mouseState.startTime;
				mouseState.lastX = mouseState.x;
				mouseState.lastY = mouseState.y;
			}

			@Override
			public void mouseDragged(DrawerMouseEvent e) {
				if (mouseState != null && mouseState.display != null) {
					long time = System.currentTimeMillis() - mouseState.lastTime;
					int deltaX = mouseState.x - e.getX();
					int deltaY = mouseState.y - e.getY();
					if (time < 300 && Math.abs(deltaX) < 15 && Math.abs(deltaY) < 15) {
						// System.out.println("   Ignore: " + time + "  " +
						// deltaX + "," + deltaY);
						return;
					}
					mouseState.x = e.getX();
					mouseState.y = e.getY();
					if (mouseState.canDrag) {
						handleDrag();
					}
					mouseState.lastTime = System.currentTimeMillis();
					mouseState.lastX = mouseState.x;
					mouseState.lastY = mouseState.y;
				}
			}
		});

		handlers.add(eventBus.addHandler(BreweryComponentChangeEvent.getType(), new BreweryComponentChangeEventHandler() {
			@Override
			public void onStateChange(final BreweryComponent component) {
				BreweryDisplay.this.drawer.redrawBreweryComponent(component);
			}
		}));

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

	private void handleDown() {
		mouseState.canDrag = false;
		if (mouseState.display != null) {
			mouseState.canDrag = true;
			final BreweryComponent component = mouseState.display.getComponent();
			ControlStep selectedStep = controller.getSelectedStep();

			mouseState.display.setMouseDown(true);

			if (mouseState.display.getType() == DisplayType.UpCtrl || mouseState.display.getType() == DisplayType.DownCtrl) {
				mouseState.canDrag = false;
				final double direction = mouseState.display.getType() == DisplayType.UpCtrl ? TempFConverter.convertF2C(1) : TempFConverter.convertF2C(-1);
				if (selectedStep != null) {
					if (component instanceof BrewHardwareControl) {
						RunRepeat run = new RunRepeat() {
							int delay = 400;

							@Override
							public long run() {
								ControlStep selectedStep = controller.getSelectedStep();
								if (selectedStep != null) {
									ControlPoint controlPoint = selectedStep.getControlPointForPin(((BrewHardwareControl) component).getPin());
									if (controlPoint != null && !controlPoint.isAutomaticControl()) {
										if (component instanceof HeatElement) {

											if (mouseState.adjustValue == null) {
												mouseState.adjustValue = new Double(controlPoint.getDuty());
											}

											int newDuty = (int) (mouseState.adjustValue + direction);
											setNewDuty(mouseState, component, selectedStep, controlPoint, newDuty);

											if (newDuty >= 100 || newDuty <= 0) {
												return -1;
											}
											if (delay > 100) {
												delay -= 100;
											} else if (delay > 50) {
												delay = 50;
											}
											return delay;
										}
									}
								}
								return -1;
							}
						};
						long schedule = run.run();
						if (schedule > -1) {
							mouseState.whileDown = scheduler.scheduleReapeating(run, schedule);
						}

					} else if (component instanceof Sensor) {

						RunRepeat run = new RunRepeat() {
							int delay = 400;

							@Override
							public long run() {
								ControlStep selectedStep = controller.getSelectedStep();
								if (selectedStep != null) {
									Sensor sensor = (Sensor) component;
									ControlPoint controlPoint = selectedStep.getControlPointForAddress(sensor.getAddress());
									if (controlPoint != null && controlPoint.isAutomaticControl()) {

										if (mouseState.adjustValue == null) {
											mouseState.adjustValue = new Double(controlPoint.getTargetTemp());
										}

										double newTemp = mouseState.adjustValue + direction;
										setNewTemp(mouseState, component, selectedStep, controlPoint, newTemp);

										if (newTemp <= 0 || newTemp >= MAX_TEMP) {
											return -1;
										}
										if (delay > 100) {
											delay -= 100;
										} else if (delay > 50) {
											delay = 50;
										}
										return delay;

									}
								}
								return -1;
							}
						};
						long schedule = run.run();
						if (schedule > -1) {
							mouseState.whileDown = scheduler.scheduleReapeating(run, schedule);
						}

					}
				}
			} else if (component instanceof Pump) {
				mouseState.canDrag = false;
				if (selectedStep != null) {
					BrewHardwareControl brewHardwareControl = (BrewHardwareControl) component;
					ControlPoint controlPoint = selectedStep.getControlPointForPin(brewHardwareControl.getPin());
					if (controlPoint != null && !controlPoint.isAutomaticControl()) {
						controlPoint.setDuty(controlPoint.getDuty() > 0 ? 0 : 100);
						eventBus.fireEvent(new StepModifyEvent(selectedStep));
					}
				}
			}

			BreweryDisplay.this.drawer.redrawDisplay(mouseState.display);

		}
	}

	private void handleUp() {
		if (mouseState.whileDown != null) {
			mouseState.whileDown.cancel();
			mouseState.whileDown = null;
		}

		Runnable whenComplete = mouseState.whenComplete;
		if (whenComplete != null) {
			whenComplete.run();
		}
		if (mouseState.display != null) {
			mouseState.display.setMouseDown(false);
			BreweryDisplay.this.drawer.redrawDisplay(mouseState.display);
		}
	}

	private void handleDrag() {
		if (mouseState.display != null) {
			double delta = mouseState.lastY - mouseState.y;

			long time = System.currentTimeMillis() - mouseState.lastTime;
			if (time > 200 && Math.abs(delta) < 30) {
				delta = delta < 0 ? -1 : 1;
			} else if (time > 100 && Math.abs(delta) > 15) {
				delta = delta * 0.5;
			}

			BreweryComponent component = mouseState.display.getComponent();
			ControlStep selectedStep = controller.getSelectedStep();
			if (selectedStep != null) {
				if (component instanceof BrewHardwareControl) {
					ControlPoint controlPoint = selectedStep.getControlPointForPin(((BrewHardwareControl) component).getPin());
					if (controlPoint != null && !controlPoint.isAutomaticControl()) {
						if (component instanceof HeatElement) {
							int newDuty = (int) (controlPoint.getDuty() + delta);
							setNewDuty(mouseState, component, selectedStep, controlPoint, newDuty);
						}
					}
				} else if (component instanceof Sensor) {
					Sensor sensor = (Sensor) component;
					ControlPoint controlPoint = selectedStep.getControlPointForAddress(sensor.getAddress());
					if (controlPoint != null && controlPoint.isAutomaticControl()) {
						delta = delta * (5.0 / 9.0);
						double newTemp = controlPoint.getTargetTemp() + delta;

						setNewTemp(mouseState, component, selectedStep, controlPoint, newTemp);
					}
				}

			}
		}
	}

	private void setNewTemp(MouseState mouseState, BreweryComponent component, ControlStep selectedStep, ControlPoint controlPoint, double newTemp) {
		if (newTemp < 0) {
			newTemp = 0;
		} else if (newTemp > 110) {
			newTemp = MAX_TEMP;
		}

		if (newTemp != controlPoint.getTargetTemp()) {
			if (mouseState.whenComplete == null) {
				mouseState.whenComplete = new MouseStateWhenComplete();
			}
			MouseStateWhenComplete wc = (MouseStateWhenComplete) mouseState.whenComplete;
			wc.selectedStep = selectedStep;
			controlPoint.setTargetTemp(newTemp);
			drawer.addPending(controlPoint);
			BreweryDisplay.this.drawer.redrawBreweryComponent(component);
		}

	}

	private void setNewDuty(MouseState mouseState, BreweryComponent component, ControlStep selectedStep, ControlPoint controlPoint, int newDuty) {
		if (newDuty < 0) {
			newDuty = 0;
		} else if (newDuty > 100) {
			newDuty = 100;
		}

		if (newDuty != controlPoint.getDuty()) {
			if (mouseState.whenComplete == null) {
				mouseState.whenComplete = new MouseStateWhenComplete();
			}
			MouseStateWhenComplete wc = (MouseStateWhenComplete) mouseState.whenComplete;
			wc.selectedStep = selectedStep;
			controlPoint.setDuty(newDuty);
			drawer.addPending(controlPoint);
			BreweryDisplay.this.drawer.redrawBreweryComponent(component);
		}

	}

	public void setBreweryLayout(BreweryLayout brewLayout) {
		displays.clear();
		if (brewLayout != null) {
			List<Tank> tanks = brewLayout.getTanks();

			for (Tank tank : tanks) {
				BreweryComponentDisplay tankBcd = createBcd(tank, 200, 200);

				if (tank.getHeater() != null) {
					BreweryComponentDisplay elementDisplay = createBcd(tank.getHeater(), 97 - ARROW_SIZE, 30);
					elementDisplay.setTop(25);
					elementDisplay.setLeft(102);
					elementDisplay.setParent(tankBcd);

					BreweryComponentDisplay up = createBcd(tank.getHeater(), ARROW_SIZE, ARROW_SIZE);
					up.setTop(elementDisplay.getTop());
					up.setLeft(elementDisplay.getLeft() + elementDisplay.getWidth());
					up.setType(DisplayType.UpCtrl);
					up.setParent(tankBcd);

					BreweryComponentDisplay down = createBcd(tank.getHeater(), ARROW_SIZE, ARROW_SIZE);
					down.setTop(elementDisplay.getTop() + ARROW_SIZE);
					down.setLeft(elementDisplay.getLeft() + elementDisplay.getWidth());
					down.setType(DisplayType.DownCtrl);
					down.setParent(tankBcd);
				}

				if (tank.getSensor() != null) {
					BreweryComponentDisplay elementDisplay = createBcd(tank.getSensor(), 97, 60);
					elementDisplay.setTop(25);
					elementDisplay.setLeft(2);
					elementDisplay.setParent(tankBcd);

					BreweryComponentDisplay up = createBcd(tank.getSensor(), ARROW_SIZE, ARROW_SIZE);
					up.setTop(elementDisplay.getTop());
					up.setLeft(elementDisplay.getLeft() + elementDisplay.getWidth());
					up.setType(DisplayType.UpCtrl);
					up.setParent(tankBcd);

					BreweryComponentDisplay down = createBcd(tank.getSensor(), ARROW_SIZE, ARROW_SIZE);
					down.setTop(elementDisplay.getTop() + ARROW_SIZE);
					down.setLeft(elementDisplay.getLeft() + elementDisplay.getWidth());
					down.setType(DisplayType.DownCtrl);
					down.setParent(tankBcd);
				}
			}

			List<Pump> pumps = brewLayout.getPumps();
			for (Pump pump : pumps) {
				createBcd(pump, 100, 120);
			}

			layoutDisplays();
		}
	}

	private BreweryComponentDisplay createBcd(BreweryComponent comp, int width, int height) {
		BreweryComponentDisplay display = new BreweryComponentDisplay(comp);
		displays.add(display);
		display.setSize(width, height);
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

	private static class MouseState {
		protected Double adjustValue;
		public Cancelable whileDown;
		public Runnable whenComplete;
		public boolean canDrag;
		protected long startTime;
		protected long lastTime;
		protected BreweryComponentDisplay display;
		protected int lastY;
		@SuppressWarnings("unused")
		protected int lastX;
		protected int y;
		protected int x;

	}

	private class MouseStateWhenComplete implements Runnable {
		public ControlStep selectedStep;

		@Override
		public void run() {
			eventBus.fireEvent(new StepModifyEvent(selectedStep));

			drawer.clearPending();
		}
	}

}
