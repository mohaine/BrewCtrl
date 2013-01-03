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

package com.mohaine.brewcontroller.web.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.client.bean.SensorConfiguration;
import com.mohaine.brewcontroller.client.bean.TempSensor;
import com.mohaine.brewcontroller.client.event.ChangeEvent;
import com.mohaine.brewcontroller.client.event.ChangeHandler;
import com.mohaine.brewcontroller.client.event.HandlerRegistration;
import com.mohaine.brewcontroller.client.event.HasValue;
import com.mohaine.brewcontroller.client.layout.Tank;
import com.mohaine.brewcontroller.shared.util.StringUtils;

public class SensorEditorGwt extends Composite implements HasValue<TempSensor> {

	private Label label = new Label();
	private FlowPanel panel = new FlowPanel();

	private ControllerHardware controller;

	@Inject
	public SensorEditorGwt(ControllerHardware controller) {
		super();

		this.controller = controller;
		// label.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

		panel.add(label);

		label.addMouseDownHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				startEditing();
			}
		});
		initWidget(panel);
	}

	private ArrayList<ChangeHandler> changeHandlers = new ArrayList<ChangeHandler>();
	private TempSensor value;

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
	public TempSensor getValue() {
		return value;
	}

	@Override
	public void setValue(TempSensor value) {
		setValue(value, true);
	}

	@Override
	public void setValue(TempSensor value, boolean fireEvents) {
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

	private SensorConfiguration getSensorConfig(TempSensor value) {
		Configuration configuration = controller.getConfiguration();

		SensorConfiguration sConfig = configuration.findSensor(value.getAddress());
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
		}
	}

	private void add(Widget label2) {
		panel.add(label2);
	}

	private void removeAll() {
		panel.clear();

	}

	private void startEditing() {

		if (!editing) {
			editing = true;
			removeAll();
			final Editor editor = new Editor();

			add(editor.panel);

			editor.locationCombo.setFocus(true);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private class Editor {
		private Panel panel;
		private TextBox editField;
		private ListBox locationCombo;

		public Editor() {
			super();
			SensorConfiguration sConfig = getSensorConfig(value);

			editField = new TextBox();
			String editName = sConfig.getName();
			if (editName != null && editName.equals(value.getAddress())) {
				editName = "";
			}

			editField.setText(editName);
			editField.addKeyDownHandler(new KeyDownHandler() {

				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						stopEditingSave();
					} else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
						stopEditing();
					}
				}
			});
			Configuration configuration = controller.getConfiguration();

			List<Tank> tanks = configuration.getBrewLayout().getTanks();

			locationCombo = new ListBox();
			// locationCombo.addItem(null);
			// for (Tank tank : tanks) {
			//
			// if (tank.getSensor() == null) {
			// continue;
			// }
			//
			// locationCombo.addItem(tank);
			// if (sConfig != null &&
			// tank.getName().equals(sConfig.getLocation())) {
			// locationCombo.setSelectedItem(tank);
			// }
			// }
			// locationCombo.addKeyListener(new KeyListener() {
			//
			// @Override
			// public void keyTyped(KeyEvent paramKeyEvent) {
			// }
			//
			// @Override
			// public void keyReleased(KeyEvent paramKeyEvent) {
			// }
			//
			// @Override
			// public void keyPressed(KeyEvent paramKeyEvent) {
			// if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
			// if (!locationCombo.isPopupVisible()) {
			// stopEditingSave();
			// }
			// } else if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
			// stopEditing();
			// }
			// }
			// });
			panel = new HorizontalPanel();

			panel.add(locationCombo);
			panel.add(editField);

			// FocusHandler focusListener = new FocusHandler() {
			// Timeout timeout = new Timeout();
			//
			// @Override
			// public void focusLost(FocusEvent paramFocusEvent) {
			// timeout.start(250, new Runnable() {
			// @Override
			// public void run() {
			// stopEditingSave();
			// }
			// });
			// }
			//
			// @Override
			// public void focusGained(FocusEvent paramFocusEvent) {
			// timeout.cancel();
			// }
			//
			// @Override
			// public void onBlur(BlurEvent event) {
			//
			// }
			//
			// @Override
			// public void onFocus(com.google.gwt.event.dom.client.FocusEvent
			// event) {
			// timeout.cancel();
			//
			// }
			// };
			// editField.addFocusHandler(focusListener);
			// locationCombo.addFocusHandler(focusListener);
		}

		protected void stopEditingSave() {
			// String locationName = null;
			// Tank tank = (Tank) locationCombo.getSelectedItem();
			// if (tank != null) {
			// locationName = tank.getName();
			// }
			// String name = editField.getText();
			// config.updateSensor(value.getAddress(), name, locationName);
			// controller.setConfiguration(config);

			stopEditing();
		}
	}
}
