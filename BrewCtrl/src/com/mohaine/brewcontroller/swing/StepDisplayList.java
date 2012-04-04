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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.event.StepsModifyEvent;
import com.mohaine.brewcontroller.event.StepsModifyEventHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.bus.EventBus;

public class StepDisplayList extends JPanel {
	private static final long serialVersionUID = 1L;

	private Controller controller;
	private EventBus eventBus;
	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private ArrayList<StepEditorSwing> editors = new ArrayList<StepEditorSwing>();

	private JPanel editorsPanel;

	private Provider<StepEditorSwing> providerStepEditorSwing;

	@Inject
	public StepDisplayList(Controller controllerp, EventBus eventBusp, Provider<StepEditorSwing> providerStepEditorSwing) {
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
		JPanel addNew = new JPanel();
		addNew.setLayout(new BorderLayout());

		add(addNew, gbc);
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

		addNew.add(addNewLabel, BorderLayout.WEST);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1;
		gbc.weighty = 1;

		JPanel extraPanel = new JPanel();
		add(extraPanel, gbc);

		updateSteps();
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

		List<HeaterStep> steps = controller.getSteps();
		int index = 0;
		for (HeaterStep heaterStep : steps) {
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
		List<HeaterStep> steps = new ArrayList<HeaterStep>(controller.getSteps());
		steps.add(controller.createManualStep("New Step"));
		controller.setSteps(steps);
	}

}
