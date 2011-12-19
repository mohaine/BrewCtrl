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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;

import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;

public class TimespanEditorSwing extends JPanel implements HasValue<Long>, ChangeListener {
	private static final long serialVersionUID = 1L;
	JSpinner minuteSpinner = new JSpinner();
	JSpinner secondSpinner = new JSpinner();

	public TimespanEditorSwing() {
		super();

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;

		add(minuteSpinner, gbc);
		gbc.gridx++;
		gbc.weightx = 0;
		add(secondSpinner, gbc);

		Dimension preferredSize = secondSpinner.getPreferredSize();
		preferredSize.width = 50;
		secondSpinner.setPreferredSize(preferredSize);
		secondSpinner.addChangeListener(this);
		minuteSpinner.addChangeListener(this);

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
			minuteSpinner.setValue(minutes);
			secondSpinner.setValue(seconds);
		} finally {
			ignoreEvents = false;
		}

	}

	@Override
	public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {

		if (ignoreEvents) {
			return;
		}

		long minutes = getSpinnerLong(minuteSpinner);
		long seconds = getSpinnerLong(secondSpinner);

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

	private long getSpinnerLong(JSpinner spinner) {
		return ((Number) spinner.getValue()).longValue();
	}
}
