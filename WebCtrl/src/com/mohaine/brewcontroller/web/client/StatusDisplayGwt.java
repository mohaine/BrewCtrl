package com.mohaine.brewcontroller.web.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.UnitConversion;
import com.mohaine.brewcontroller.client.bean.Configuration;
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
import com.mohaine.brewcontroller.client.net.BrewJsonConverter;

public class StatusDisplayGwt extends Composite {

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

	private ToggleButton modeHeatOffButton = new ToggleButton("Heat Off", new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			changeMode(Mode.HEAT_OFF);
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
			if (modeHeatOffButton.getValue()) {
				return Mode.HEAT_OFF.toString();
			}
			return null;
		}

		@Override
		public void setValue(String value, boolean fireEvents) {
			modeHoldButton.setValue(Mode.HOLD.equals(value));
			modeOnButton.setValue(Mode.ON.equals(value));
			modeOffButton.setValue(Mode.OFF.equals(value));
			modeHeatOffButton.setValue(Mode.HEAT_OFF.equals(value));

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

	private VerticalPanel statusPanel;

	private Provider<SensorEditorGwt> providerSensorEditor;
	private BrewJsonConverter brewJsonConverter;
	private PopupPanel loadCfgPopup;

	@Inject
	public StatusDisplayGwt(UnitConversion conversion, ControllerHardware controller, EventBus eventBus, StepDisplayListGwt stepDisplay, Provider<SensorEditorGwt> providerSensorEditor,
			BrewJsonConverter brewJsonConverter) {
		super();
		this.conversion = conversion;
		this.controller = controller;
		this.eventBus = eventBus;
		this.providerSensorEditor = providerSensorEditor;
		this.brewJsonConverter = brewJsonConverter;
		VerticalPanel panel = new VerticalPanel();

		final Label loadCfgLabel = new Label("Load Configuration");
		loadCfgLabel.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				if (loadCfgPopup != null) {
					loadCfgPopup.hide();
					loadCfgPopup = null;
				}

				loadCfgPopup = new PopupPanel();
				loadCfgPopup.setAnimationEnabled(false);
				loadCfgPopup.setAutoHideEnabled(true);
				VerticalPanel w = new VerticalPanel();

				HorizontalPanel hp = new HorizontalPanel();

				hp.add(new Label("Configuration:"));
				hp.add(new HTML("<input type=\"file\" id=\"configFileUpload\" name=\"configFileUpload\"  />"));

				FormPanel fp = new FormPanel();

				hp.add(fp);
				w.add(hp);
				loadCfgPopup.setWidget(w);
				loadCfgPopup.showRelativeTo(w);

				Scheduler.get().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						captureFileInputChange(StatusDisplayGwt.this, "configFileUpload");
					}
				});

			}
		});
		loadCfgLabel.getElement().getStyle().setCursor(Cursor.POINTER);
		panel.add(loadCfgLabel);

		HorizontalPanel modePanel = new HorizontalPanel();
		modePanel.add(modeOnButton);
		modePanel.add(modeHoldButton);
		modePanel.add(modeHeatOffButton);
		modePanel.add(modeOffButton);
		panel.add(modePanel);

		statusPanel = new VerticalPanel();
		panel.add(statusPanel);

		status = addTitledLabel(statusPanel, "Status:");
		mode = addTitledLabel(statusPanel, "Mode:");
		updateState();
		updateMode(controller.getMode());

		panel.add(stepDisplay);

		initWidget(panel);

	}

	public void uploadConfig(String configuration) {
		try {
			Configuration cfg = brewJsonConverter.getJsonConverter().decode(configuration, Configuration.class);
			if (cfg != null) {
				controller.setConfiguration(cfg);
				if (loadCfgPopup != null) {
					loadCfgPopup.hide();
					loadCfgPopup = null;
				}
			} else {
				Window.alert("Invalid Configuration");
			}
		} catch (Exception e) {
			Window.alert("Invalid Configuration : " + e.getMessage());
		}

	}

	public native void captureFileInputChange(StatusDisplayGwt sdg, String id) /*-{

		function handleFileSelect(evt) {
			var files = evt.target.files; // FileList object

			if (files.length > 0) {
				var f = files[0];
				var reader = new FileReader();

				// Closure to capture the file information.
				reader.onload = (function(theFile) {
					return function(e) {

						var text = e.target.result;
						sdg.@com.mohaine.brewcontroller.web.client.StatusDisplayGwt::uploadConfig(Ljava/lang/String;)(text);
					};
				})(f);

				reader.readAsText(f);

			}
		}

		$wnd.document.getElementById(id).addEventListener('change',
				handleFileSelect, false);

	}-*/;

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
		HorizontalPanel hp = new HorizontalPanel();
		panel.add(hp);
		hp.add(new Label(title));
		Label value = new Label();
		hp.add(value);
		return value;

	}

	private SensorLabelGwt addTitledSensorLabel(Panel panel, TempSensor tempSensor) {
		HorizontalPanel hp = new HorizontalPanel();

		panel.add(hp);
		SensorEditorGwt label = providerSensorEditor.get();
		hp.add(label);

		Label value = new Label();
		hp.add(value);

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
				modeHeatOffButton.setValue(false);
				mode.setText("Off");
			} else if (Mode.ON.equals(hardwareStatus.getMode())) {
				modeOffButton.setValue(false);
				modeOnButton.setValue(true);
				modeHoldButton.setValue(false);
				modeHeatOffButton.setValue(false);
				mode.setText("On");
			} else if (Mode.HOLD.equals(hardwareStatus.getMode())) {
				modeOffButton.setValue(false);
				modeOnButton.setValue(false);
				modeHoldButton.setValue(true);
				modeHeatOffButton.setValue(false);
				mode.setText("Hold");
			} else if (Mode.HEAT_OFF.equals(hardwareStatus.getMode())) {
				modeOffButton.setValue(false);
				modeOnButton.setValue(false);
				modeHoldButton.setValue(false);
				modeHeatOffButton.setValue(true);
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
