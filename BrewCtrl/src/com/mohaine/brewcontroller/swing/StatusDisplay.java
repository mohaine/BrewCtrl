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
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.UnitConversion;
import com.mohaine.brewcontroller.client.bean.ControllerStatus;
import com.mohaine.brewcontroller.client.bean.ControllerStatus.Mode;
import com.mohaine.brewcontroller.client.bean.TempSensor;
import com.mohaine.brewcontroller.client.event.AbstractHasValue;
import com.mohaine.brewcontroller.client.event.ChangeEvent;
import com.mohaine.brewcontroller.client.event.ChangeModeEvent;
import com.mohaine.brewcontroller.client.event.ChangeModeEventHandler;
import com.mohaine.brewcontroller.client.event.HandlerRegistration;
import com.mohaine.brewcontroller.client.event.HasValue;
import com.mohaine.brewcontroller.client.event.StatusChangeEvent;
import com.mohaine.brewcontroller.client.event.StatusChangeEventHandler;
import com.mohaine.brewcontroller.client.event.bus.EventBus;

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
	private HasValue<String> modeHasValue = new AbstractHasValue<String>() {

		@Override
		public String getValue() {
			if (modeOffButton.getModel().isSelected()) {
				return Mode.OFF.toString();
			}
			if (modeHoldButton.getModel().isSelected()) {
				return Mode.HOLD.toString();
			}
			if (modeOnButton.getModel().isSelected()) {
				return Mode.ON.toString();
			}
			return null;
		}

		@Override
		public void setValue(String value, boolean fireEvents) {
			modeHoldButton.getModel().setSelected(Mode.HOLD.equals(value));
			modeOnButton.getModel().setSelected(Mode.ON.equals(value));
			modeOffButton.getModel().setSelected(Mode.OFF.equals(value));

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
			public void onChangeMode(final String mode) {
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
		changeMode(newMode.toString());
	}

	private void changeMode(String newMode) {
		modeHasValue.setValue(newMode, true);
		controller.changeMode(newMode);
	}

	private void updateMode(String mode) {
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

	private SensorLabel addTitledSensorLabel(GridBagConstraints gbc, JPanel panel, TempSensor tempSensor) {

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

		ControllerStatus hardwareStatus = controller.getControllerStatus();
		if (hardwareStatus != null) {
			if (Mode.OFF.equals(hardwareStatus.getMode())) {
				modeOffButton.setSelected(true);
				modeOnButton.setSelected(false);
				modeHoldButton.setSelected(false);
				mode.setText("Off");
			} else if (Mode.ON.equals(hardwareStatus.getMode())) {
				modeOffButton.setSelected(false);
				modeOnButton.setSelected(true);
				modeHoldButton.setSelected(false);
				mode.setText("On");
			} else if (Mode.HOLD.equals(hardwareStatus.getMode())) {
				modeOffButton.setSelected(false);
				modeOnButton.setSelected(false);
				modeHoldButton.setSelected(true);
				mode.setText("Hold");
			} else {
				mode.setText("???????");
			}
		}

		List<TempSensor> sensors = controller.getSensors();
		for (TempSensor tempSensor : sensors) {
			boolean found = false;
			for (SensorLabel sensorLabel : sensorLabels) {
				if (sensorLabel.sensor.getAddress().equals(tempSensor.getAddress())) {
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
		TempSensor sensor;

		public void update() {
			label.setValue(sensor);
			value.setText(tempFormat.format(conversion.getTempDisplayConveter().convertFrom(sensor.getTempatureC())));
			label.setForeground(sensor.isReading() ? normalStatusForeground : Color.red);
		}
	}

}
