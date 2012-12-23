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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mohaine.brewcontroller.ControllerHardware;
import com.mohaine.brewcontroller.UnitConversion;
import com.mohaine.brewcontroller.bean.ControllerStatus;
import com.mohaine.brewcontroller.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.event.ChangeModeEvent;
import com.mohaine.brewcontroller.event.ChangeModeEventHandler;
import com.mohaine.brewcontroller.event.StatusChangeEvent;
import com.mohaine.brewcontroller.event.StatusChangeEventHandler;
import com.mohaine.event.AbstractHasValue;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;
import com.mohaine.event.bus.EventBus;

public class StatusDisplay extends JPanel {
	private static final long serialVersionUID = 1L;

	private JToggleButton modeOnButton = new JToggleButton(new AbstractAction("Go") {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			changeMode(Mode.ON);
		}

	});
	private JToggleButton modeHoldButton = new JToggleButton(new AbstractAction("Hold") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			changeMode(Mode.HOLD);
		}

	});
	private JToggleButton modeOffButton = new JToggleButton(new AbstractAction("Off") {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			changeMode(Mode.OFF);
		}

	});
	private HasValue<Mode> modeHasValue = new AbstractHasValue<Mode>() {

		@Override
		public Mode getValue() {
			if (modeOffButton.getModel().isSelected()) {
				return Mode.OFF;
			}
			if (modeHoldButton.getModel().isSelected()) {
				return Mode.HOLD;
			}
			if (modeOnButton.getModel().isSelected()) {
				return Mode.ON;
			}
			return null;
		}

		@Override
		public void setValue(Mode value, boolean fireEvents) {
			modeHoldButton.getModel().setSelected(value == Mode.HOLD);
			modeOnButton.getModel().setSelected(value == Mode.ON);
			modeOffButton.getModel().setSelected(value == Mode.OFF);

			if (fireEvents) {
				fireEvent(new ChangeEvent());
			}

		}
	};

	private JLabel mode;
	private JLabel status;
	private ControllerHardware controller;

	private ArrayList<SensorLabel> sensorLabels = new ArrayList<SensorLabel>();
	private static final NumberFormat tempFormat = new DecimalFormat("0.0");
	private GridBagConstraints gbc = new GridBagConstraints();
	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private UnitConversion conversion;

	private EventBus eventBus;

	private JPanel statusPanel;

	private Color normalStatusForeground;

	private Provider<SensorEditor> providerSensorEditor;

	@Inject
	public StatusDisplay(UnitConversion conversion, ControllerHardware controller, EventBus eventBus, StepDisplayList stepDisplay, Provider<SensorEditor> providerSensorEditor) {
		super();
		this.conversion = conversion;
		this.controller = controller;
		this.eventBus = eventBus;
		this.providerSensorEditor = providerSensorEditor;
		setLayout(new BorderLayout());

		JPanel modePanel = createTitledPanel("Mode");
		modePanel.setLayout(new FlowLayout());
		modePanel.add(modeOnButton);
		modePanel.add(modeHoldButton);
		modePanel.add(modeOffButton);
		add(modePanel, BorderLayout.NORTH);

		statusPanel = new JPanel();
		add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new GridBagLayout());

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		status = addTitledLabel(gbc, statusPanel, "Status:");
		mode = addTitledLabel(gbc, statusPanel, "Mode:");

		normalStatusForeground = status.getForeground();

		updateState();
		updateMode(controller.getMode());

		add(stepDisplay, BorderLayout.CENTER);

	}

	private JPanel createTitledPanel(String name) {
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BorderLayout());
		namePanel.setBorder(BorderFactory.createTitledBorder(name));
		return namePanel;
	}

	@Override
	public void addNotify() {
		super.addNotify();

		removeHandlers();

		handlers.add(eventBus.addHandler(StatusChangeEvent.getType(), new StatusChangeEventHandler() {

			@Override
			public void onChangeStatus() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateState();
					}
				});
			}
		}));

		handlers.add(eventBus.addHandler(ChangeModeEvent.getType(), new ChangeModeEventHandler() {

			@Override
			public void onChangeMode(final Mode mode) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateMode(mode);
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

	private void changeMode(Mode newMode) {
		modeHasValue.setValue(newMode, true);
		controller.changeMode(newMode);
	}

	private void updateMode(Mode mode) {
		modeHasValue.setValue(mode, false);
	}

	private JLabel addTitledLabel(GridBagConstraints gbc, JPanel panel, String title) {

		gbc.gridx = 0;
		gbc.weightx = 2;
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		panel.add(new JLabel(title), gbc);

		gbc.gridx++;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		JLabel value = new JLabel();
		panel.add(value, gbc);
		return value;

	}

	private SensorLabel addTitledSensorLabel(GridBagConstraints gbc, JPanel panel, HardwareSensor tempSensor) {

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 2;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		SensorEditor label = providerSensorEditor.get();
		panel.add(label, gbc);

		gbc.weightx = 1;
		gbc.gridx++;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		JLabel value = new JLabel();
		panel.add(value, gbc);

		SensorLabel sensorLabel = new SensorLabel();
		sensorLabel.value = value;
		sensorLabel.sensor = tempSensor;
		sensorLabel.label = label;

		return sensorLabel;
	}

	private void updateState() {
		{
			status.setText(controller.getStatus());
			boolean statusOk = "Ok".equals(controller.getStatus());
			status.setForeground(statusOk ? normalStatusForeground : Color.red);
		}

		ControllerStatus hardwareStatus = controller.getHardwareStatus();
		if (hardwareStatus != null) {
			switch (hardwareStatus.getMode()) {
			case OFF:
				mode.setText("Off");
				break;
			case ON:
				mode.setText("On");
				break;
			case HOLD:
				mode.setText("Hold");
				break;
			default:
				mode.setText("???????");
				break;
			}
		}

		List<HardwareSensor> sensors = controller.getSensors();
		for (HardwareSensor tempSensor : sensors) {

			boolean found = false;
			for (SensorLabel sensorLabel : sensorLabels) {
				if (sensorLabel.sensor == tempSensor) {
					sensorLabel.update();
					found = true;
					break;
				}
			}
			if (!found) {
				SensorLabel sensorLabel = addTitledSensorLabel(gbc, statusPanel, tempSensor);
				sensorLabel.update();
				sensorLabels.add(sensorLabel);
			}
		}
	}

	private class SensorLabel {
		SensorEditor label;
		JLabel value;
		HardwareSensor sensor;

		public void update() {
			label.setValue(sensor);
			value.setText(tempFormat.format(conversion.getTempDisplayConveter().convertFrom(sensor.getTempatureC())));
			label.setForeground(sensor.isReading() ? normalStatusForeground : Color.red);
		}
	}

}
