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

package com.mohaine.brewcontroller.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mohaine.brewcontroller.ConfigurationHeaterStep;
import com.mohaine.brewcontroller.ConfigurationHeaterStepControlPoint;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.ConfigurationStepList;
import com.mohaine.brewcontroller.TimeParser;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.event.HandlerRegistration;
import com.mohaine.brewcontroller.client.event.StepsModifyEvent;
import com.mohaine.brewcontroller.client.event.StepsModifyEventHandler;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.layout.BrewHardwareControl;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.client.layout.Sensor;

public class StepDisplayList extends JPanel {
	private static final long serialVersionUID = 1L;

	private ControllerHardware controller;
	private EventBus eventBus;
	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private ArrayList<StepEditorSwing> editors = new ArrayList<StepEditorSwing>();

	private JPanel editorsPanel;

	private Provider<StepEditorSwing> providerStepEditorSwing;

	@Inject
	public StepDisplayList(ControllerHardware controllerp, EventBus eventBusp, Provider<StepEditorSwing> providerStepEditorSwing, ConfigurationLoader configLoader) {
		super();
		this.providerStepEditorSwing = providerStepEditorSwing;
		this.eventBus = eventBusp;
		this.controller = controllerp;

		setPreferredSize(new Dimension(200, 200));
		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;

		editorsPanel = new JPanel();
		editorsPanel.setLayout(new GridLayout(0, 1));
		add(editorsPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());

		add(controlPanel, gbc);

		JLabel addNewLabel = new JLabel("+ Add New Step");
		addNewLabel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				addNewStep();
			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {

			}
		});
		controlPanel.add(addNewLabel, BorderLayout.WEST);

		List<ConfigurationStepList> stepLists = configLoader.getConfiguration().getStepLists();
		if (stepLists != null && stepLists.size() > 0) {
			final JLabel listLabel = new JLabel("List");

			controlPanel.add(listLabel, BorderLayout.EAST);

			final JPopupMenu popup = new JPopupMenu();

			for (final ConfigurationStepList stepList : stepLists) {
				popup.add(new JMenuItem(new AbstractAction(stepList.getName()) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent arg0) {
						launchList(stepList);
					}
				}));
			}
			listLabel.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {

				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Locate better
					popup.show(listLabel, e.getX(), e.getY());
				}

				@Override
				public void mouseExited(MouseEvent e) {

				}

				@Override
				public void mouseEntered(MouseEvent e) {

				}

				@Override
				public void mouseClicked(MouseEvent e) {

				}
			});

		}

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.weighty = 1;

		JPanel extraPanel = new JPanel();
		add(extraPanel, gbc);

		
	}

	protected void launchList(ConfigurationStepList stepList) {
		BreweryLayout layout = controller.getBreweryLayout();
		TimeParser tp = new TimeParser();

		ArrayList<ControlStep> heaterSteps = new ArrayList<ControlStep>();
		for (ConfigurationHeaterStep configurationHeaterStep : stepList.getSteps()) {
			ControlStep step = controller.createManualStep(configurationHeaterStep.getName());
			step.setStepTime(tp.parse(configurationHeaterStep.getTime()));
			for (ConfigurationHeaterStepControlPoint cfgCp : configurationHeaterStep.getControlPoints()) {
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
	public void addNotify() {
		super.addNotify();

		removeHandlers();
		handlers.add(eventBus.addHandler(StepsModifyEvent.getType(), new StepsModifyEventHandler() {
			@Override
			public void onStepsChange() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateSteps();
					}
				});

			}
		}));
		updateSteps();

	}

	@Override
	public void removeNotify() {
		super.removeNotify();
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
			StepEditorSwing stepEditor;
			if (index < editors.size()) {
				stepEditor = editors.get(index);
			} else {
				stepEditor = providerStepEditorSwing.get();
				editors.add(stepEditor);
				editorsPanel.add(stepEditor);
			}
			stepEditor.setStep(heaterStep);
			index++;
		}
		while (editors.size() > steps.size()) {
			StepEditorSwing extra = editors.remove(steps.size());
			editorsPanel.remove(extra);
		}

		editorsPanel.repaint();
	}

	private void addNewStep() {
		List<ControlStep> steps = new ArrayList<ControlStep>(controller.getSteps());
		steps.add(controller.createManualStep("New Step"));
		controller.changeSteps(steps);
	}

}
