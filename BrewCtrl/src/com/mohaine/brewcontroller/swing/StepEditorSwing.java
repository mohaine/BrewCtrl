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

package com.mohaine.brewcontroller.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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

public class StepEditorSwing extends JPanel {

	private static final int HEIGHT = 20;

	private static final long serialVersionUID = 1L;

	private ClickEditorSwing<String> nameValue = new ClickEditorSwing<String>(new Converter<String, String>() {

		@Override
		public String convertFrom(String value) {
			return value;
		}

		@Override
		public String convertTo(String value) {
			return value;
		}

	});
	private ClickEditorSwing<Integer> timeValue = new ClickEditorSwing<Integer>(new Converter<Integer, String>() {
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

	private JPanel edit;

	@Inject
	public StepEditorSwing(EventBus eventBus, ControllerHardware controller) {
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

		mainPanel.add(nameValue, gbc);
		nameValue.setPreferredSize(new Dimension(200, HEIGHT));
		nameValue.setMinimumSize(new Dimension(200, HEIGHT));

		gbc.gridx++;
		gbc.weightx = 0;

		mainPanel.add(timeValue, gbc);
		timeValue.setPreferredSize(new Dimension(70, HEIGHT));
		timeValue.setMinimumSize(new Dimension(70, HEIGHT));

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
			List<ControlStep> steps = new ArrayList<ControlStep>(controller.getSteps());

			for (int i = 0; i < steps.size(); i++) {
				ControlStep s = steps.get(i);
				if (s.getId().equals(heaterStep.getId())) {
					steps.remove(i);
					break;
				}
			}

			controller.changeSteps(steps);
		}
	}
}
