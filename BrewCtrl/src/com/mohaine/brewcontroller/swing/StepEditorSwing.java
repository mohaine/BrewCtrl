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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEvent;
import com.mohaine.brewcontroller.event.ChangeSelectedStepEventHandler;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.brewcontroller.event.StepModifyEventHandler;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.bus.EventBus;

public class StepEditorSwing extends JPanel {

	private static final int HEIGHT = 20;

	private static final long serialVersionUID = 1L;

	private JTextField nameField = new JTextField();

	private TimespanEditorSwing timeValue = new TimespanEditorSwing();

	private HeaterStep heaterStep;

	private EventBus eventBus;

	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private Controller controller;

	private JPanel edit;

	@Inject
	public StepEditorSwing(EventBus eventBus, Controller controller) {
		super();
		this.eventBus = eventBus;
		this.controller = controller;
		JPanel mainPanel = this;

		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		mainPanel.add(nameField, gbc);
		nameField.setPreferredSize(new Dimension(200, HEIGHT));
		nameField.setMinimumSize(new Dimension(200, HEIGHT));

		gbc.gridx++;
		gbc.weightx = 0;

		mainPanel.add(timeValue, gbc);
		timeValue.setPreferredSize(new Dimension(70, HEIGHT));
		timeValue.setMinimumSize(new Dimension(70, HEIGHT));

		nameField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				updateName();
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

		timeValue.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (heaterStep != null) {
					long stepTime = timeValue.getValue();
					if (stepTime != heaterStep.getStepTime()) {
						heaterStep.setStepTime(stepTime);
						fireChange();
					}
				}

			}
		});

		gbc.gridx++;

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		gbc.weightx = 0;
		mainPanel.add(controlPanel, gbc);

		JLabel delete = new JLabel("X");
		delete.addMouseListener(new MouseListenerAbstract() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				deleteStep();
			}
		});
		controlPanel.add(delete);

		edit = new JPanel();
		edit.setMinimumSize(new Dimension(20, HEIGHT));
		edit.setPreferredSize(new Dimension(20, HEIGHT));
		edit.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
		edit.addMouseListener(new MouseListenerAbstract() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				selectStep();
			}
		});
		controlPanel.add(edit);

	}

	@Override
	public void addNotify() {
		super.addNotify();
		removeHandlers();
		handlers.add(eventBus.addHandler(StepModifyEvent.getType(), new StepModifyEventHandler() {
			@Override
			public void onStepChange(HeaterStep step) {
				if (step == heaterStep) {
					setStep(step);
				}
			}
		}));

		handlers.add(eventBus.addHandler(ChangeSelectedStepEvent.getType(), new ChangeSelectedStepEventHandler() {

			@Override
			public void onStepChange(HeaterStep step) {
				updateSelected(step == heaterStep);
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

	protected void fireChange() {
		if (heaterStep != null) {
			eventBus.fireEvent(new StepModifyEvent(heaterStep));
		}
	}

	public void setStep(HeaterStep heaterStep) {
		this.heaterStep = heaterStep;
		nameField.setText(heaterStep.getName());
		timeValue.setValue(heaterStep.getStepTime(), false);
		updateSelected(heaterStep == controller.getSelectedStep());
	}

	private void updateName() {
		String newName = nameField.getText();
		if (!newName.equals(heaterStep.getName())) {
			heaterStep.setName(newName);
			fireChange();
		}
	}

	private void updateSelected(boolean selected) {
		if (selected) {
			edit.setBackground(Color.black);
		} else {
			edit.setBackground(getBackground());
		}

	}

	private void selectStep() {
		controller.setSelectedStep(heaterStep);
	}

	private void deleteStep() {
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this, "Delete step \"" + heaterStep.getName() + "\"?", "Confirm Delete", JOptionPane.OK_CANCEL_OPTION)) {
			List<HeaterStep> steps = new ArrayList<HeaterStep>(controller.getSteps());
			steps.remove(heaterStep);
			controller.setSteps(steps);
		}
	}
}
