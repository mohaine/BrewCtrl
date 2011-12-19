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

package com.mohaine.brewcontroller.swing.page;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.BrewPrefs;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.bean.TempSensor;
import com.mohaine.brewcontroller.page.Setup.SetupDisplay;
import com.mohaine.brewcontroller.swing.SwingHasClickHandlers;
import com.mohaine.event.ClickHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.StatusChangeHandler;

public class SetupDisplaySwing extends JPanel implements SetupDisplay {
	private static final long serialVersionUID = 1L;
	private ArrayList<SensorLabel> sensorLabels = new ArrayList<SensorLabel>();
	private GridBagConstraints gbc = new GridBagConstraints();

	private JComboBox tunSensorCombo = new JComboBox();
	private JComboBox hltSensorCombo = new JComboBox();

	private Hardware hardware;
	private BrewPrefs prefs;
	private Timer timer = null;
	private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private JPanel inputPanel = new JPanel();
	private JPanel controlPanel;

	@Inject
	public SetupDisplaySwing(Hardware hardware, final BrewPrefs prefs) {
		super();
		this.hardware = hardware;
		this.prefs = prefs;

		setLayout(new BorderLayout());
		add(inputPanel, BorderLayout.CENTER);

		inputPanel.setLayout(new GridBagLayout());

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;

		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		inputPanel.add(new JLabel("HLT Sensor:"), gbc);

		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		inputPanel.add(hltSensorCombo, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		inputPanel.add(new JLabel("Tun Sensor:"), gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		inputPanel.add(tunSensorCombo, gbc);
		gbc.gridy++;
		gbc.gridx = 0;

		tunSensorCombo.addItem(new TempSensor(""));
		hltSensorCombo.addItem(new TempSensor(""));

		hltSensorCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				TempSensor selectedItem = (TempSensor) hltSensorCombo.getSelectedItem();
				if (selectedItem != null) {
					prefs.setHltSensorAddress(selectedItem.getAddress());
				}
			}
		});
		tunSensorCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				TempSensor selectedItem = (TempSensor) tunSensorCombo.getSelectedItem();
				if (selectedItem != null) {
					prefs.setTunSensorAddress(selectedItem.getAddress());
				}
			}
		});

		controlPanel = new JPanel();
		add(controlPanel, BorderLayout.SOUTH);

	}

	@Override
	public void addClickable(String name, ClickHandler ch) {
		gbc.gridy++;
		SwingHasClickHandlers setupClickable = new SwingHasClickHandlers(name);
		setupClickable.addClickHandler(ch);
		controlPanel.add(new JButton(setupClickable), gbc);
	}

	private void updateState(List<TempSensor> sensors) {
		for (TempSensor tempSensor : sensors) {

			boolean found = false;
			for (SensorLabel sensorLabel : sensorLabels) {
				if (sensorLabel.sensor == tempSensor) {
					sensorLabel.update();
					found = true;
					break;
				}
			}
			if (!found) {
				sensorLabels.add(addSensorLabel(gbc, tempSensor));
				invalidate();
			}
		}
		updateCombo(sensors, hltSensorCombo, prefs.getHltSensorAddress());
		updateCombo(sensors, tunSensorCombo, prefs.getTunSensorAddress());
		doLayout();
	}

	@Override
	public void addNotify() {
		synchronized (handlers) {
			handlers.add(hardware.addStatusChangeHandler(new StatusChangeHandler() {
				@Override
				public void onStateChange() {
					setSensors(hardware.getSensors());
				}
			}));
		}
		super.addNotify();
	}

	@Override
	public void removeNotify() {
		synchronized (handlers) {
			for (HandlerRegistration handlerReg : handlers) {
				handlerReg.removeHandler();
			}
			handlers.clear();
		}
		super.removeNotify();
	}

	private void updateCombo(List<TempSensor> sensors, JComboBox combo, String defaultValue) {
		combo.repaint();
		for (TempSensor tempSensor : sensors) {
			int itemCount = combo.getItemCount();
			boolean found = false;
			for (int i = 0; i < itemCount; i++) {
				TempSensor indexSensor = (TempSensor) combo.getItemAt(i);
				if (indexSensor.getAddress().equals(tempSensor.getAddress())) {
					found = true;
					break;
				}
			}

			if (!found) {
				combo.addItem(tempSensor);
				if (tempSensor.getAddress().equals(defaultValue)) {
					combo.setSelectedItem(tempSensor);
				}
			}

		}
	}

	private SensorLabel addSensorLabel(GridBagConstraints gbc, final TempSensor tempSensor) {
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;

		inputPanel.add(new JLabel("Sensor Name:"), gbc);
		gbc.gridx++;

		final JTextField value = new JTextField();

		Dimension preferredSize = value.getPreferredSize();
		preferredSize.width = 150;
		value.setPreferredSize(preferredSize);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		inputPanel.add(value, gbc);
		gbc.gridx = 0;
		gbc.gridy++;

		SensorLabel sensorLabel = new SensorLabel();
		sensorLabel.value = value;
		sensorLabel.sensor = tempSensor;
		sensorLabel.update();

		value.addKeyListener(sensorLabel);

		return sensorLabel;
	}

	@Override
	public void setSensors(final List<TempSensor> listSensors) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateState(listSensors);
			}
		});
	}

	private class SensorLabel implements KeyListener {
		JTextField value;
		TempSensor sensor;

		public void update() {
			String name = sensor.getName();
			if (!value.hasFocus()) {
				if (!name.equals(value.getText())) {
					value.setText(sensor.getName());
				}
			}
		}

		@Override
		public void keyPressed(KeyEvent arg0) {
			clearTimer();
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			clearTimer();
			updateSensorName();
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			clearTimer();
		}

		private synchronized void scheduleTimer() {
			clearTimer();

			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					hardware.fireStateChangeHandlers();
				}
			}, 100);
		}

		private synchronized void clearTimer() {
			if (timer != null) {
				timer.cancel();
				timer.purge();
				timer = null;
			}

		}

		private void updateSensorName() {
			String newName = value.getText();
			if (!sensor.getName().equals(newName)) {
				sensor.setName(newName);

				prefs.setSensorName(sensor.getAddress(), newName);
				scheduleTimer();
			}
		}
	}

	@Override
	public void init() {

	}

}
