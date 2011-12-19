package com.mohaine.brewcontroller.android.display;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;

import com.mohaine.brewcontroller.android.widget.TimespanEditorAndroid;
import com.mohaine.brewcontroller.page.StepEditor.StepEditorDisplay;
import com.mohaine.event.AbstractHasValue;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.HandlerRegistration;
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

	}

	private View createLabel(String string) {
		TextView tv = new TextView(context);
		tv.setText(string);
		return tv;
	}

	@Override
	public HasValue<Double> getTunTemp() {
		// TODO
		return new HasValue<Double>() {

			@Override
			public Double getValue() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setValue(Double value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setValue(Double value, boolean fireEvents) {
				// TODO Auto-generated method stub

			}

			@Override
			public HandlerRegistration addChangeHandler(ChangeHandler handler) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void fireEvent(ChangeEvent event) {
				// TODO Auto-generated method stub

			}
		};
	}

	@Override
	public HasValue<Double> getHltTemp() {
		// TODO
		return new HasValue<Double>() {

			@Override
			public Double getValue() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setValue(Double value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setValue(Double value, boolean fireEvents) {
				// TODO Auto-generated method stub

			}

			@Override
			public HandlerRegistration addChangeHandler(ChangeHandler handler) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void fireEvent(ChangeEvent event) {
				// TODO Auto-generated method stub

			}
		};

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
