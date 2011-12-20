package com.mohaine.brewcontroller.android.display;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.Converter;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.UnitConversion;
import com.mohaine.brewcontroller.android.widget.TimespanEditorAndroid;
import com.mohaine.brewcontroller.android.widget.ValueSlider;
import com.mohaine.brewcontroller.bean.HardwareStatus;
import com.mohaine.brewcontroller.page.StepEditor.StepEditorDisplay;
import com.mohaine.event.AbstractHasValue;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;
import com.mohaine.event.StatusChangeHandler;

public class StepEditorDisplayAndroid extends ControlPanelAndroid implements StepEditorDisplay {

	@Inject
	protected Activity activity;

	private EditText nameField;

	private HasValue<String> nameHasValue = new AbstractHasValue<String>() {
		@Override
		public String getValue() {
			return nameField.getText().toString();
		}

		@Override
		public void setValue(String value, boolean fireEvents) {
			nameField.setText(value);
			if (fireEvents) {
				fireEvent(new ChangeEvent());
			}
		}
	};
	private TimespanEditorAndroid timespanEditor;
	private Hardware hardware;
	private Converter<Double, Double> tempDisplayConveter;
	private ValueSlider tunTempSlider;
	private ValueSlider hltTempSlider;
	private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	@Inject
	public StepEditorDisplayAndroid(UnitConversion conversion, Hardware hardware) {
		this.hardware = hardware;
		tempDisplayConveter = conversion.getTempDisplayConveter();
	}

	@Override
	public void init() {
		super.init();

		layout.addView(createLabel("Name"));

		nameField = new EditText(context);
		nameField.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout.addView(nameField);

		layout.addView(createLabel("Step Time"));
		timespanEditor = new TimespanEditorAndroid(context);
		timespanEditor.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout.addView(timespanEditor);

		tunTempSlider = new ValueSlider(context, 120, tempDisplayConveter.convertTo(100.0), tempDisplayConveter.convertTo(212.0), tempDisplayConveter);
		hltTempSlider = new ValueSlider(context, 120, tempDisplayConveter.convertTo(100.0), tempDisplayConveter.convertTo(212.0), tempDisplayConveter);

		LinearLayout ll = new LinearLayout(context);

		tunTempSlider.setLayoutParams(new LayoutParams(100, 200));
		ll.addView(tunTempSlider);
		hltTempSlider.setLayoutParams(new LayoutParams(100, 200));
		ll.addView(hltTempSlider);

		layout.addView(ll);

		synchronized (handlers) {
			handlers.add(hardware.addStatusChangeHandler(new StatusChangeHandler() {
				@Override
				public void onStateChange() {

					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							HardwareStatus hardwareStatus = hardware.getHardwareStatus();

							updateOtherTemp(hardwareStatus, hardware.getHardwareStatus().getHltSensor(), hltTempSlider);
							updateOtherTemp(hardwareStatus, hardware.getHardwareStatus().getTunSensor(), tunTempSlider);

						}

						private void updateOtherTemp(HardwareStatus hardwareStatus, String sensor, ValueSlider slider) {
							Double actualTemp = hardware.getSensorTemp(sensor);
							if (actualTemp != null) {

								Double target = slider.getValue();

								if (Math.abs(target - actualTemp) < 1.5) { // Temp
																			// C
									slider.setOtherValueColor(Color.argb(30, 0, 255, 0));
								} else {
									slider.setOtherValueColor(Color.argb(90, 255, 0, 0));
								}

								slider.setDrawOtherValue(true);
								slider.setOtherValue(tempDisplayConveter.convertFrom(actualTemp));
							} else {
								slider.setDrawOtherValue(false);
							}
						}
					});
				}
			}));
		}

	}

	private View createLabel(String string) {
		TextView tv = new TextView(context);
		tv.setText(string);
		return tv;
	}

	@Override
	public HasValue<Double> getTunTemp() {
		return tunTempSlider;
	}

	@Override
	public HasValue<Double> getHltTemp() {
		return hltTempSlider;
	}

	@Override
	public HasValue<Long> getTimeValue() {
		return timespanEditor;
	}

	@Override
	public HasValue<String> getNameValue() {
		return nameHasValue;
	}

}
