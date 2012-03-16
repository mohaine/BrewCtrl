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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.BrewPrefs;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.brewcontroller.page.Setup.SetupDisplay;
import com.mohaine.brewcontroller.swing.SwingHasClickHandlers;
import com.mohaine.event.ClickHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.StatusChangeHandler;

public class SetupDisplaySwing extends JPanel implements SetupDisplay {
	private static final long serialVersionUID = 1L;

	private Hardware hardware;
	private BrewPrefs prefs;
	private Timer timer = null;
	private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private JPanel controlPanel;
	private JPanel sensorPanel;
	private List<Tank> tanks;
	private ArrayList<SensorLabel> sensorLabels = new ArrayList<SensorLabel>();

	private GridBagConstraints gbc;

	@Inject
	public SetupDisplaySwing(Hardware hardware, final BrewPrefs prefs, Controller controller) {
		super();
		this.hardware = hardware;
		this.prefs = prefs;
		this.tanks = controller.getLayout().getTanks();

		setLayout(new BorderLayout());

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;

		sensorPanel = new JPanel();
		sensorPanel.setLayout(new GridBagLayout());
		sensorPanel.setBorder(BorderFactory.createTitledBorder("Sensors"));
		add(sensorPanel, BorderLayout.CENTER);

		controlPanel = new JPanel();
		add(controlPanel, BorderLayout.SOUTH);

	}

	@Override
	public void addClickable(String name, ClickHandler ch) {
		SwingHasClickHandlers setupClickable = new SwingHasClickHandlers(name);
		setupClickable.addClickHandler(ch);
		controlPanel.add(new JButton(setupClickable));
	}

	private void updateState(List<HardwareSensor> sensors) {

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
				sensorLabels.add(addSensorLabel(gbc, tempSensor));
				invalidate();
			}
		}
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

	private SensorLabel addSensorLabel(GridBagConstraints gbc, final HardwareSensor tempSensor) {
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.WEST;

		sensorPanel.add(new JLabel("Sensor Name:"), gbc);
		gbc.gridx++;

		final JTextField value = new JTextField();

		Dimension preferredSize = value.getPreferredSize();
		preferredSize.width = 150;
		value.setPreferredSize(preferredSize);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		sensorPanel.add(value, gbc);
		gbc.gridx = 0;
		gbc.gridy++;

		sensorPanel.add(new JLabel("Sensor Location:"), gbc);
		gbc.gridx++;

		JComboBox<Tank> locationCombo = new JComboBox<Tank>();
		sensorPanel.add(locationCombo, gbc);
		locationCombo.addItem(null);

		for (Tank tank : tanks) {

			if (tank.getSensor() == null) {
				continue;
			}

			locationCombo.addItem(tank);
			if (tank.getName().equals(prefs.getSensorLocation(tempSensor.getAddress(), null))) {
				locationCombo.setSelectedItem(tank);
			}
		}

		gbc.gridx = 0;
		gbc.gridy++;

		SensorLabel sensorLabel = new SensorLabel();
		sensorLabel.value = value;
		sensorLabel.sensor = tempSensor;
		sensorLabel.combo = locationCombo;
		sensorLabel.update();

		value.addKeyListener(sensorLabel);
		locationCombo.addItemListener(sensorLabel);

		return sensorLabel;
	}

	@Override
	public void setSensors(final List<HardwareSensor> listSensors) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateState(listSensors);
			}
		});
	}

	private class SensorLabel implements KeyListener, ItemListener {
		public JComboBox<Tank> combo;
		JTextField value;
		HardwareSensor sensor;

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

		@Override
		public void itemStateChanged(ItemEvent e) {
			Tank selectedItem = (Tank) combo.getSelectedItem();
			if (selectedItem != null) {
				prefs.setSensorLocation(sensor.getAddress(), selectedItem.getName());
			} else {
				prefs.setSensorLocation(sensor.getAddress(), "");
			}
		}
	}

	@Override
	public void init() {

	}

}
