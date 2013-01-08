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

package com.mohaine.brewcontroller.web.client;

import java.util.ArrayList;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.Converter;
import com.mohaine.brewcontroller.client.TimeParser;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.event.ChangeEvent;
import com.mohaine.brewcontroller.client.event.ChangeHandler;
import com.mohaine.brewcontroller.client.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.client.event.ChangeSelectedStepEventHandler;
import com.mohaine.brewcontroller.client.event.HandlerRegistration;
import com.mohaine.brewcontroller.client.event.StepModifyEvent;
import com.mohaine.brewcontroller.client.event.StepModifyEventHandler;
import com.mohaine.brewcontroller.client.event.bus.EventBus;

public class StepEditorGwt extends Composite {

	private static final int HEIGHT = 20;

	private ClickEditorGwt<String> nameValue = new ClickEditorGwt<String>(new Converter<String, String>() {

		@Override
		public String convertFrom(String value) {
			return value;
		}

		@Override
		public String convertTo(String value) {
			return value;
		}

	});
	private ClickEditorGwt<Integer> timeValue = new ClickEditorGwt<Integer>(new Converter<Integer, String>() {
		TimeParser tp = new TimeParser();

		{
			tp.setZeroDescription("Forever");
		}

		@Override
		public String convertFrom(Integer value) {
			return tp.format(value);
		}

		@Override
		public Integer convertTo(String value) {
			return tp.parse(value);
		}
	});

	private ControlStep heaterStep;

	private EventBus eventBus;

	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private ControllerHardware controller;

	private FlowPanel edit;

	@Inject
	public StepEditorGwt(EventBus eventBus, ControllerHardware controller) {
		super();
		this.eventBus = eventBus;
		this.controller = controller;
		HorizontalPanel mainPanel = new HorizontalPanel();

		mainPanel.add(nameValue);
		// nameValue.setPreferredSize(new Dimension(200, HEIGHT));
		// nameValue.setMinimumSize(new Dimension(200, HEIGHT));

		mainPanel.add(timeValue);
		// timeValue.setPreferredSize(new Dimension(70, HEIGHT));
		// timeValue.setMinimumSize(new Dimension(70, HEIGHT));

		nameValue.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				updateName();
			}
		});

		timeValue.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (heaterStep != null) {
					int stepTime = timeValue.getValue();
					if (stepTime != heaterStep.getStepTime()) {

						heaterStep.setStepTime(stepTime);
						fireChange();
					}
				}
			}
		});

		FlowPanel controlPanel = new FlowPanel();
		mainPanel.add(controlPanel);

		Label delete = new Label("X");
		delete.addMouseDownHandler(new MouseDownHandler() {	
			@Override
			public void onMouseDown(MouseDownEvent event) {
				deleteStep();
			}
		});
		
	
		
		delete.getElement().getStyle().setCursor(Cursor.POINTER);

	
		controlPanel.add(delete);
		edit = new FlowPanel();
		edit.getElement().getStyle().setCursor(Cursor.POINTER);
		edit.getElement().getStyle().setWidth(20, Unit.PX);
		edit.getElement().getStyle().setHeight(20, Unit.PX);
		edit.getElement().getStyle().setProperty("block", "inline");
		edit.getElement().getStyle().setProperty("border", "1px solid black");
		edit.addDomHandler(new MouseDownHandler() {	
			@Override
			public void onMouseDown(MouseDownEvent event) {
				System.out.println("StepEditorGwt.StepEditorGwt(...).new MouseDownHandler() {...}.onMouseDown()");
				selectStep();
			}
		},MouseDownEvent.getType());
		
		controlPanel.add(edit);

		initWidget(mainPanel);
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		removeHandlers();
		handlers.add(eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {

			@Override
			public void onStepChange(ControlStep step, boolean fromServer) {
				if (step.getId().equals(heaterStep.getId())) {
					setStep(step);
				}
			}
		}));

		handlers.add(eventBus.addHandler(ChangeSelectedStepEvent.getType(), new ChangeSelectedStepEventHandler() {

			@Override
			public void onStepChange(ControlStep step) {
				updateSelected(step == heaterStep);
			}

		}));
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		removeHandlers();

	}

	private void removeHandlers() {
		for (HandlerRegistration reg : handlers) {
			reg.removeHandler();
		}
		handlers.clear();
	}

	protected void fireChange() {
		if (heaterStep != null) {
			eventBus.fireEvent(new StepModifyEvent(heaterStep));
		}
	}

	public void setStep(ControlStep heaterStep) {
		this.heaterStep = heaterStep;
		nameValue.setValue(heaterStep.getName());
		timeValue.setValue(heaterStep.getStepTime(), false);
		ControlStep selectedStep = controller.getSelectedStep();

		updateSelected(selectedStep != null && selectedStep.getId().equals(heaterStep.getId()));
	}

	private void updateName() {
		String newName = nameValue.getValue();
		if (!newName.equals(heaterStep.getName())) {
			heaterStep.setName(newName);
			fireChange();
		}
	}

	private void updateSelected(boolean selected) {
		edit.getElement().getStyle().setBackgroundColor(selected ? "black" : "");
	}

	private void selectStep() {
		controller.setSelectedStep(heaterStep);
	}

	private void deleteStep() {
		System.out.println("StepEditorGwt.deleteStep()");
		// TODO
		// if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
		// "Delete step \"" + heaterStep.getName() + "\"?", "Confirm Delete",
		// JOptionPane.OK_CANCEL_OPTION)) {
		// List<ControlStep> steps = new
		// ArrayList<ControlStep>(controller.getSteps());
		//
		// for (int i = 0; i < steps.size(); i++) {
		// ControlStep s = steps.get(i);
		// if (s.getId().equals(heaterStep.getId())) {
		// steps.remove(i);
		// break;
		// }
		// }
		//
		// controller.changeSteps(steps);
		// }
	}
}
