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
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.TimeParser;
import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.client.bean.ConfigurationStep;
import com.mohaine.brewcontroller.client.bean.ConfigurationStepControlPoint;
import com.mohaine.brewcontroller.client.bean.ConfigurationStepList;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.event.HandlerRegistration;
import com.mohaine.brewcontroller.client.event.StepsModifyEvent;
import com.mohaine.brewcontroller.client.event.StepsModifyEventHandler;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.client.layout.Sensor;

public class StepDisplayListGwt extends Composite {

	private ControllerHardware controller;
	private EventBus eventBus;
	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private ArrayList<StepEditorGwt> editors = new ArrayList<StepEditorGwt>();

	private FlowPanel editorsPanel;

	private Provider<StepEditorGwt> providerStepEditor;

	@Inject
	public StepDisplayListGwt(ControllerHardware controllerp, EventBus eventBusp, Provider<StepEditorGwt> providerStepEditor) {
		super();
		this.providerStepEditor = providerStepEditor;
		this.eventBus = eventBusp;
		this.controller = controllerp;

		FlowPanel panel = new FlowPanel();
		editorsPanel = new FlowPanel();
		panel.add(editorsPanel);

		VerticalPanel controlPanel = new VerticalPanel();

		panel.add(controlPanel);

		Label addNewLabel = new Label("+ Add New Step");
		addNewLabel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addNewStep();
			}
		});

		controlPanel.add(addNewLabel);

		Configuration configuration = controllerp.getConfiguration();
		if (configuration != null) {
			List<ConfigurationStepList> stepLists = configuration.getStepLists();
			if (stepLists != null && stepLists.size() > 0) {
				final Label listLabel = new Label("List >");

				controlPanel.add(listLabel);
				// TODO
				/*
				 * final JPopupMenu popup = new JPopupMenu();
				 * 
				 * for (final ConfigurationStepList stepList : stepLists) {
				 * popup.add(new JMenuItem(new
				 * AbstractAction(stepList.getName()) { private static final
				 * long serialVersionUID = 1L;
				 * 
				 * @Override public void actionPerformed(ActionEvent arg0) {
				 * launchList(stepList); } })); } listLabel.addClickHandler(new
				 * ClickHandler() {
				 * 
				 * @Override public void onClick(ClickEvent e) {
				 * popup.show(listLabel, e.getX(), e.getY());
				 * 
				 * } });
				 */
			}
		}

		initWidget(panel);

		System.out.println("StepDisplayListGwt.StepDisplayListGwt()");
		addNewLabel.getElement().getStyle().setCursor(Cursor.POINTER);

	}

	protected void launchList(ConfigurationStepList stepList) {
		BreweryLayout layout = controller.getBreweryLayout();
		TimeParser tp = new TimeParser();

		ArrayList<ControlStep> heaterSteps = new ArrayList<ControlStep>();
		for (ConfigurationStep configurationHeaterStep : stepList.getSteps()) {
			ControlStep step = controller.createManualStep(configurationHeaterStep.getName());
			step.setStepTime(tp.parse(configurationHeaterStep.getTime()));
			for (ConfigurationStepControlPoint cfgCp : configurationHeaterStep.getControlPoints()) {
				BrewHardwareControl bhc = layout.findBrewHardwareControlByNameOrParentName(cfgCp.getControlName());
				Sensor sensor = layout.findSensorByNameOrParentName(cfgCp.getTargetName());
				if (bhc != null && sensor != null) {
					ControlPoint controlPoint = step.getControlPointForPin(bhc.getPin());
					if (controlPoint != null) {
						controlPoint.setAutomaticControl(true);
						controlPoint.setTargetTemp(cfgCp.getTargetTemp());
						controlPoint.setTempSensorAddress(sensor.getAddress());
					}
				}
			}
			heaterSteps.add(step);
		}
		controller.changeSteps(heaterSteps);

	}

	@Override
	protected void onAttach() {
		super.onAttach();
		removeHandlers();
		handlers.add(eventBus.addHandler(StepsModifyEvent.getType(), new StepsModifyEventHandler() {
			@Override
			public void onStepsChange() {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						updateSteps();
					}
				});
			}
		}));
		updateSteps();
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

	private void updateSteps() {

		List<ControlStep> steps = controller.getSteps();
		int index = 0;
		for (ControlStep heaterStep : steps) {
			StepEditorGwt stepEditor;
			if (index < editors.size()) {
				stepEditor = editors.get(index);
			} else {
				stepEditor = providerStepEditor.get();
				editors.add(stepEditor);
				editorsPanel.add(stepEditor);
			}
			stepEditor.setStep(heaterStep);
			index++;
		}
		while (editors.size() > steps.size()) {
			StepEditorGwt extra = editors.remove(steps.size());
			editorsPanel.remove(extra);
		}

	}

	private void addNewStep() {
		List<ControlStep> steps = new ArrayList<ControlStep>(controller.getSteps());
		steps.add(controller.createManualStep("New Step"));
		controller.changeSteps(steps);
	}

}
