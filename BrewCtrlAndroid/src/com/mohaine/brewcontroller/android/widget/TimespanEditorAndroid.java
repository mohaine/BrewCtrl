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

package com.mohaine.brewcontroller.android.widget;

import java.util.ArrayList;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;

public class TimespanEditorAndroid extends LinearLayout implements HasValue<Long> {

	private EditText minuteEdit;
	private EditText secondEdit;

	public TimespanEditorAndroid(Context context) {
		super(context);
		minuteEdit = new EditText(context);
		minuteEdit.setWidth(100);
		minuteEdit.setInputType(InputType.TYPE_CLASS_NUMBER);

		secondEdit = new EditText(context);
		secondEdit.setWidth(100);
		secondEdit.setInputType(InputType.TYPE_CLASS_NUMBER);

		TextWatcher watcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {

			}

			@Override
			public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {

			}

			@Override
			public void afterTextChanged(Editable paramEditable) {
				stateChanged();
			}
		};
		minuteEdit.addTextChangedListener(watcher);
		secondEdit.addTextChangedListener(watcher);

		addView(minuteEdit);
		addView(secondEdit);

	}

	private ArrayList<ChangeHandler> changeHandlers = new ArrayList<ChangeHandler>();
	private long value = 0;

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
	public Long getValue() {
		return value;
	}

	@Override
	public void setValue(Long value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Long newValue, boolean fireEvents) {
		updateValue(newValue, fireEvents);
	}

	private void updateValue(long newValue) {
		updateValue(newValue, true);
	}

	private boolean ignoreEvents = false;

	private void updateValue(long newValue, boolean fireEvents) {
		boolean dirty = newValue != value;
		if (dirty) {
			value = newValue;

			if (fireEvents) {
				fireEvent(new ChangeEvent());
			}
		}

		long minutes = value / 60000;
		long seconds = (value - minutes * 60000) / 1000;
		ignoreEvents = true;
		try {
			minuteEdit.setText(Long.toString(minutes));
			secondEdit.setText(Long.toString(seconds));
		} finally {
			ignoreEvents = false;
		}

	}

	private void stateChanged() {

		if (ignoreEvents) {
			return;
		}

		long minutes = getSpinnerLong(minuteEdit);
		long seconds = getSpinnerLong(secondEdit);

		if (seconds < 0) {
			if (minutes > 0) {
				minutes--;
				seconds = 59;
			} else {
				seconds = 0;
			}
		}
		if (seconds > 59) {
			long minutesInSeconds = seconds / 60;
			minutes += minutesInSeconds;
			seconds = seconds - (minutesInSeconds * 60);
		}
		if (minutes < 0) {
			minutes = 0;
			seconds = 0;
		}

		long milliseconds = minutes * 60000;
		// System.out.println("Minutes => " + milliseconds);
		milliseconds += seconds * 1000;
		// System.out.println("   Seconds => " + milliseconds);

		updateValue(milliseconds);
	}

	private long getSpinnerLong(EditText te) {
		Editable text = te.getText();
		try {
			return Long.parseLong(text.toString());
		} catch (Exception e) {
			return 0;
		}
	}
}
