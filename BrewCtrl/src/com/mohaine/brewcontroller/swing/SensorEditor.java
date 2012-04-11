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
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.Configuration;
import com.mohaine.brewcontroller.SensorConfiguration;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.brewcontroller.util.StringUtils;
import com.mohaine.brewcontroller.util.Timeout;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;

public class SensorEditor extends JPanel implements HasValue<HardwareSensor> {
	private static final long serialVersionUID = 1L;

	private JLabel label = new JLabel();

	private Configuration config;

	@Inject
	public SensorEditor(Configuration config) {
		super();
		this.config = config;
		setLayout(new BorderLayout());
		label.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

		add(label);

		label.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent paramMouseEvent) {

			}

			@Override
			public void mousePressed(MouseEvent paramMouseEvent) {
				startEditing();
			}

			@Override
			public void mouseExited(MouseEvent paramMouseEvent) {

			}

			@Override
			public void mouseEntered(MouseEvent paramMouseEvent) {

			}

			@Override
			public void mouseClicked(MouseEvent paramMouseEvent) {

			}
		});

	}

	private ArrayList<ChangeHandler> changeHandlers = new ArrayList<ChangeHandler>();
	private HardwareSensor value;

	private boolean editing;

	@Override
	public HandlerRegistration addChangeHandler(final ChangeHandler handler) {
		changeHandlers.add(handler);
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				changeHandlers.remove(handler);
			}
		};
	}

	@Override
	public void fireEvent(ChangeEvent event) {
		for (ChangeHandler handler : changeHandlers) {
			handler.onChange(event);
		}

	}

	@Override
	public HardwareSensor getValue() {
		return value;
	}

	@Override
	public void setValue(HardwareSensor value) {
		setValue(value, true);
	}

	@Override
	public void setValue(HardwareSensor value, boolean fireEvents) {
		this.value = value;
		if (fireEvents) {
			fireEvent(new ChangeEvent());
		}

		StringBuffer sb = new StringBuffer();

		SensorConfiguration sConfig = getSensorConfig(value);

		String locationName = sConfig.getLocation();

		if (StringUtils.hasLength(locationName)) {
			sb.append(locationName);
		}
		if (StringUtils.hasLength(sConfig.getName())) {
			if (!sConfig.getName().equals(locationName)) {
				if (sb.length() > 0) {
					sb.append(" - ");
				}
				sb.append(sConfig.getName());
			}
		}
		sb.append(":");

		label.setText(sb.toString());
	}

	private SensorConfiguration getSensorConfig(HardwareSensor value) {
		SensorConfiguration sConfig = config.findSensor(value.getAddress());
		if (sConfig == null) {
			sConfig = new SensorConfiguration();
			sConfig.setName(value.getAddress());
		}
		return sConfig;
	}

	private void stopEditing() {
		if (editing) {
			editing = false;
			removeAll();
			setValue(value);
			
			add(label);
			this.doLayout();
			repaint();
		}
	}

	private void startEditing() {

		if (!editing) {
			editing = true;
			removeAll();
			final Editor editor = new Editor();

			add(editor.panel);

			this.doLayout();
			editor.panel.doLayout();
			editor.locationCombo.doLayout();
			repaint();
			editor.panel.repaint();
			editor.locationCombo.requestFocus();
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private class Editor {
		private JPanel panel;
		private JTextField editField;
		private JComboBox locationCombo;

		public Editor() {
			super();
			SensorConfiguration sConfig = getSensorConfig(value);

			editField = new JTextField();
			String editName = sConfig.getName();
			if (editName != null && editName.equals(value.getAddress())) {
				editName = "";
			}

			editField.setText(editName);
			editField.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent paramKeyEvent) {
				}

				@Override
				public void keyReleased(KeyEvent paramKeyEvent) {
				}

				@Override
				public void keyPressed(KeyEvent paramKeyEvent) {
					if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
						stopEditingSave();
					} else if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
						stopEditing();
					}
				}
			});

			List<Tank> tanks = config.getBrewLayout().getTanks();

			locationCombo = new JComboBox();
			locationCombo.addItem(null);
			for (Tank tank : tanks) {

				if (tank.getSensor() == null) {
					continue;
				}

				locationCombo.addItem(tank);
				if (sConfig != null && tank.getName().equals(sConfig.getLocation())) {
					locationCombo.setSelectedItem(tank);
				}
			}
			locationCombo.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent paramKeyEvent) {
				}

				@Override
				public void keyReleased(KeyEvent paramKeyEvent) {
				}

				@Override
				public void keyPressed(KeyEvent paramKeyEvent) {
					if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
						if (!locationCombo.isPopupVisible()) {
							stopEditingSave();
						}
					} else if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
						stopEditing();
					}
				}
			});
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			// GridBagConstraints gbc = new GridBagConstraints();
			// gbc.gridx = 0;
			// gbc.gridy = 0;
			// gbc.fill = GridBagConstraints.BOTH;
			// gbc.weightx = 1;
			// gbc.weighty = 1;
			//
			// panel.add(editField, gbc);
			// gbc.gridx++;
			// panel.add(locationCombo, gbc);

			panel.setLayout(new GridLayout(1, 2));
			panel.add(locationCombo);
			panel.add(editField);

			FocusListener focusListener = new FocusListener() {
				Timeout timeout = new Timeout();

				@Override
				public void focusLost(FocusEvent paramFocusEvent) {
					timeout.start(250, new Runnable() {
						@Override
						public void run() {
							stopEditingSave();
						}
					});
				}

				@Override
				public void focusGained(FocusEvent paramFocusEvent) {
					timeout.cancel();
				}
			};
			editField.addFocusListener(focusListener);
			locationCombo.addFocusListener(focusListener);

		}

		protected void stopEditingSave() {
			String locationName = null;
			Tank tank = (Tank) locationCombo.getSelectedItem();
			if (tank != null) {
				locationName = tank.getName();
			}
			String name = editField.getText();
			config.updateSensor(value.getAddress(), name, locationName);
			stopEditing();
		}
	}
}
