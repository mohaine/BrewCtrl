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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.brewcontroller.event.StepModifyEvent;
import com.mohaine.brewcontroller.event.StepModifyEventHandler;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.bus.EventBus;

public class StepEditorSwing extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTextField nameField = new JTextField();

	private TimespanEditorSwing timeValue = new TimespanEditorSwing();

	private HeaterStep heaterStep;

	private EventBus eventBus;

	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	@Inject
	public StepEditorSwing(EventBus eventBus) {
		super();
		this.eventBus = eventBus;
		JPanel mainPanel = this;

		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BorderLayout());
		namePanel.add(nameField, BorderLayout.CENTER);
		mainPanel.add(namePanel, gbc);

		gbc.gridx++;
		gbc.gridheight = 1;

		JPanel timePanel = new JPanel();
		timePanel.setLayout(new BorderLayout());
		timePanel.add(timeValue, BorderLayout.CENTER);
		mainPanel.add(timePanel, gbc);

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
					System.out.println("Step Time: " + stepTime + " " + heaterStep.getStepTime());

					if (stepTime != heaterStep.getStepTime()) {
						System.out.println("  CHANGE Step Time: " + stepTime);

						heaterStep.setStepTime(stepTime);
						fireChange();
					}
				}

			}
		});

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

	private JPanel createTitledPanel(String name) {
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BorderLayout());
		namePanel.setBorder(BorderFactory.createTitledBorder(name));
		return namePanel;
	}

	public void setStep(HeaterStep heaterStep) {
		this.heaterStep = heaterStep;
		nameField.setText(heaterStep.getName());
		timeValue.setValue(heaterStep.getStepTime(), false);
	}

	private void updateName() {
		String newName = nameField.getName();
		if (!newName.equals(heaterStep.getName())) {
			heaterStep.setName(newName);
			fireChange();
		}

	}
}
