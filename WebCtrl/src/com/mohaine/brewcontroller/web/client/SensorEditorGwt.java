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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.Timer;
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
			
			Configuration configuration = controller.getConfiguration();

			List<Tank> tanks = configuration.getBrewLayout().getTanks();

			locationCombo = new ListBox();
			locationCombo.addItem("");
			int index = 1;
			for (Tank tank : tanks) {

				if (tank.getSensor() == null) {
					continue;
				}

				locationCombo.addItem(tank.getName());
				if (sConfig != null && tank.getName().equals(sConfig.getLocation())) {
					locationCombo.setSelectedIndex(index);
				}
				index++;
			}
			KeyDownHandler keyHandler = new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						stopEditingSave();
					} else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
						stopEditing();
					}
				}
			};
			locationCombo.addKeyDownHandler(keyHandler);
			editField.addKeyDownHandler(keyHandler);
			panel = new HorizontalPanel();

			panel.add(locationCombo);
			panel.add(editField);

	
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				
				@Override
				public void execute() {
					locationCombo.setFocus(true);

					FocusBlurHandler focusListener = new FocusBlurHandler();
					editField.addFocusHandler(focusListener);
					editField.addBlurHandler(focusListener);
					
					locationCombo.addFocusHandler(focusListener);
					locationCombo.addBlurHandler(focusListener);					
				}
			});

			

		}

		private class FocusBlurHandler implements BlurHandler, FocusHandler {
			Timer timeout = new Timer() {
				@Override
				public void run() {
					stopEditingSave();
				}
			};

			@Override
			public void onFocus(FocusEvent event) {
				timeout.cancel();

			}

			@Override
			public void onBlur(BlurEvent event) {
				timeout.schedule(250);
			}

		}

		protected void stopEditingSave() {
			String locationName = locationCombo.getValue(locationCombo.getSelectedIndex());
			Configuration config = controller.getConfiguration();
			config.updateSensor(value.getAddress(), editField.getText(), locationName);
			controller.setConfiguration(config);

			stopEditing();
		}
	}
}
