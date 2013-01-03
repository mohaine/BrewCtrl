package com.mohaine.brewcontroller.web.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
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

public class StatusPanel extends Composite {

	private ToggleButton modeOnButton = new ToggleButton("Go", new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			changeMode(Mode.ON);
		}
	});
	private ToggleButton modeHoldButton = new ToggleButton("Hold", new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			changeMode(Mode.HOLD);
		}
	});
	private ToggleButton modeOffButton = new ToggleButton("Off", new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			changeMode(Mode.OFF);
		}
	});

	private HasValue<String> modeHasValue = new AbstractHasValue<String>() {

		@Override
		public String getValue() {
			if (modeOffButton.getValue()) {
				return Mode.OFF.toString();
			}
			if (modeHoldButton.getValue()) {
				return Mode.HOLD.toString();
			}
			if (modeOnButton.getValue()) {
				return Mode.ON.toString();
			}
			return null;
		}

		@Override
		public void setValue(String value, boolean fireEvents) {
			modeHoldButton.setValue(Mode.HOLD.equals(value));
			modeOnButton.setValue(Mode.ON.equals(value));
			modeOffButton.setValue(Mode.OFF.equals(value));

			if (fireEvents) {
				fireEvent(new ChangeEvent());
			}

		}
	};

	private Label mode;
	private Label status;
	private ControllerHardware controller;

	private ArrayList<SensorLabelGwt> sensorLabels = new ArrayList<SensorLabelGwt>();

	private NumberFormat tempFormat = NumberFormat.getFormat("0.0");

	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private UnitConversion conversion;

	private EventBus eventBus;

	private FlowPanel statusPanel;

	private Provider<SensorEditorGwt> providerSensorEditor;

	@Inject
	public StatusPanel(UnitConversion conversion, ControllerHardware controller, EventBus eventBus, StepDisplayListGwt stepDisplay, Provider<SensorEditorGwt> providerSensorEditor) {
		super();
		this.conversion = conversion;
		this.controller = controller;
		this.eventBus = eventBus;
		this.providerSensorEditor = providerSensorEditor;
		VerticalPanel panel = new VerticalPanel();

		FlowPanel modePanel = createTitledPanel("Mode");
		modePanel.add(modeOnButton);
		modePanel.add(modeHoldButton);
		modePanel.add(modeOffButton);
		panel.add(modePanel);

		statusPanel = new FlowPanel();
		panel.add(statusPanel);

		status = addTitledLabel(statusPanel, "Status:");
		mode = addTitledLabel(statusPanel, "Mode:");
		updateState();
		updateMode(controller.getMode());

		panel.add(stepDisplay);

		initWidget(panel);

	}

	private FlowPanel createTitledPanel(String name) {
		FlowPanel namePanel = new FlowPanel();

		// TODO
		namePanel.setTitle(name);
		return namePanel;
	}

	@Override
	protected void onAttach() {
		super.onAttach();

		removeHandlers();

		handlers.add(eventBus.addHandler(StatusChangeEvent.getType(), new StatusChangeEventHandler() {
			@Override
			public void onChangeStatus() {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						updateState();
					}
				});
			}
		}));

		handlers.add(eventBus.addHandler(ChangeModeEvent.getType(), new ChangeModeEventHandler() {
			@Override
			public void onChangeMode(final String mode) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						updateMode(mode);
					}
				});
			}
		}));

	}

	@Override
	protected void onDetach() {
		super.onDetach();
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

	private Label addTitledLabel(Panel panel, String title) {

		panel.add(new Label(title));
		Label value = new Label();
		panel.add(value);
		return value;

	}

	private SensorLabelGwt addTitledSensorLabel(Panel panel, TempSensor tempSensor) {

		SensorEditorGwt label = providerSensorEditor.get();
		panel.add(label);

		Label value = new Label();
		panel.add(value);

		SensorLabelGwt sensorLabel = new SensorLabelGwt();
		sensorLabel.value = value;
		sensorLabel.sensor = tempSensor;
		sensorLabel.label = label;

		return sensorLabel;
	}

	private void updateState() {
		{
			status.setText(controller.getStatus());
			boolean statusOk = "Ok".equals(controller.getStatus());
			status.getElement().getStyle().setColor(statusOk ? "" : "red");
		}

		ControllerStatus hardwareStatus = controller.getControllerStatus();
		if (hardwareStatus != null) {
			if (Mode.OFF.equals(hardwareStatus.getMode())) {
				modeOffButton.setValue(true);
				modeOnButton.setValue(false);
				modeHoldButton.setValue(false);
				mode.setText("Off");
			} else if (Mode.ON.equals(hardwareStatus.getMode())) {
				modeOffButton.setValue(false);
				modeOnButton.setValue(true);
				modeHoldButton.setValue(false);
				mode.setText("On");
			} else if (Mode.HOLD.equals(hardwareStatus.getMode())) {
				modeOffButton.setValue(false);
				modeOnButton.setValue(false);
				modeHoldButton.setValue(true);
				mode.setText("Hold");
			} else {
				mode.setText("???????");
			}
		}

		List<TempSensor> sensors = controller.getSensors();
		for (TempSensor tempSensor : sensors) {
			boolean found = false;
			for (SensorLabelGwt sensorLabel : sensorLabels) {
				if (sensorLabel.sensor.getAddress().equals(tempSensor.getAddress())) {
					sensorLabel.sensor = tempSensor;
					sensorLabel.update();
					found = true;
					break;
				}
			}
			if (!found) {
				SensorLabelGwt sensorLabel = addTitledSensorLabel(statusPanel, tempSensor);
				sensorLabel.update();
				sensorLabels.add(sensorLabel);
			}
		}
	}

	private class SensorLabelGwt {
		SensorEditorGwt label;
		Label value;
		TempSensor sensor;

		public void update() {
			label.setValue(sensor);
			value.setText(tempFormat.format(conversion.getTempDisplayConveter().convertFrom(sensor.getTempatureC())));
			// TODO
			// label.setForeground(sensor.isReading() ? normalStatusForeground :
			// Color.red);
		}
	}

}
