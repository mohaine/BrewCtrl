package com.mohaine.brewcontroller.android.display;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.Converter;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.UnitConversion;
import com.mohaine.brewcontroller.android.widget.TimespanEditorAndroid;
import com.mohaine.brewcontroller.android.widget.ValueSlider;
import com.mohaine.brewcontroller.page.StepEditor.StepEditorDisplay;
import com.mohaine.event.AbstractHasValue;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.HasValue;

public class StepEditorDisplayAndroid extends ControlPanelAndroid implements StepEditorDisplay {

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

		layout.addView(tunTempSlider);
		layout.addView(hltTempSlider);

		
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
