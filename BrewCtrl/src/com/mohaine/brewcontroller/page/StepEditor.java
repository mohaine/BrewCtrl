/*
    Copyright 2009-2011 Michael Graessle

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

package com.mohaine.brewcontroller.page;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ClickEvent;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEventHandler;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.ClickHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;
import com.mohaine.event.bus.EventBus;

public class StepEditor extends BasePage {

	public interface StepEditorDisplay {
		public void addClickable(String name, ClickHandler ch);

		public HasValue<Double> getTunTemp();

		public HasValue<Long> getTimeValue();

		HasValue<Double> getHltTemp();

		HasValue<String> getNameValue();

		public void init();

	}

	private Object syncObject = new Object();
	private StepEditorDisplay display;
	private HeaterStep step = new HeaterStep();
	private EventBus eventBus;
	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
	private ControllerGui controllerGui;
	private MainMenu mainMenu;
	private Controller controller;

	@Inject
	public StepEditor(StepEditorDisplay displayp, EventBus eventBusp, Controller controller, final ControllerGui ci, final MainMenu mainMenu) {
		super();
		this.eventBus = eventBusp;
		this.display = displayp;
		this.controllerGui = ci;
		this.mainMenu = mainMenu;
		this.controller = controller;
		
		display.init();
		
		display.addClickable("Next Step", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				nextStep();
			}

		});
		display.addClickable("Main Menu", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				goMainMenu();
			}

		});

		display.getTimeValue().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				synchronized (syncObject) {
					step.setStepTime(display.getTimeValue().getValue());
					fireModifiedEvent();
				}
			}
		});
		display.getHltTemp().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				synchronized (syncObject) {
					step.setHltTemp(display.getHltTemp().getValue());
					fireModifiedEvent();
				}
			}
		});
		display.getTunTemp().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				synchronized (syncObject) {
					step.setTunTemp(display.getTunTemp().getValue());
					fireModifiedEvent();
				}
			}
		});

		display.getNameValue().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				synchronized (syncObject) {
					step.setName(display.getNameValue().getValue());
					fireModifiedEvent();
				}
			}
		});

		handlers.add(eventBus.addHandler(ChangeSelectedStepEvent.getType(), new ChangeSelectedStepEventHandler() {
			@Override
			public void onStepChange(final HeaterStep step) {
				setStep(step);
			}
		}));

		List<HeaterStep> steps = controller.getSteps();
		if (steps != null && steps.size() > 0) {
			setStep(steps.get(0));
		}

	}

	protected void nextStep() {
		controller.nextStep();
	}

	public HeaterStep getStep() {
		return step;
	}

	private void fireModifiedEvent() {
		eventBus.fireEvent(new StepModifyEvent(step));
	}

	public void setStep(HeaterStep step) {
		synchronized (syncObject) {

			this.step = step;
			if (step == null) {
				goMainMenu();
			} else {

				display.getHltTemp().setValue(step.getHltTemp());
				display.getTunTemp().setValue(step.getTunTemp());
				display.getNameValue().setValue(step.getName());
				display.getTimeValue().setValue(step.getStepTime());
			}
		}
	}

	private void goMainMenu() {
		controllerGui.displayPage(mainMenu);
	}

	@Override
	public String getTitle() {
		return "Mash";
	}

	@Override
	public Object getWidget() {
		return display;
	}
}
