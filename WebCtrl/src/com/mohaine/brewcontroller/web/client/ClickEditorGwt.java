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

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.mohaine.brewcontroller.client.Converter;
import com.mohaine.brewcontroller.client.event.ChangeEvent;
import com.mohaine.brewcontroller.client.event.ChangeHandler;
import com.mohaine.brewcontroller.client.event.HandlerRegistration;
import com.mohaine.brewcontroller.client.event.HasValue;

public class ClickEditorGwt<T> extends Composite implements HasValue<T> {
	private FlowPanel panel = new FlowPanel();

	private Label label = new Label();
	private final Converter<T, String> converter;

	public ClickEditorGwt(Converter<T, String> converter) {
		super();
		this.converter = converter;
		panel.add(label);
		label.getElement().getStyle().setCursor(Cursor.POINTER);

		label.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				startEditing();
			}
		});

		initWidget(panel);

	}

	private ArrayList<ChangeHandler> changeHandlers = new ArrayList<ChangeHandler>();
	private T value;

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
	public T getValue() {
		return value;
	}

	@Override
	public void setValue(T value) {
		setValue(value, true);
	}

	@Override
	public void setValue(T newValue, boolean fireEvents) {
		updateValue(newValue, fireEvents);
	}

	private void updateValue(T newValue, boolean fireEvents) {
		boolean dirty = newValue != value;
		if (dirty) {
			value = newValue;
			if (fireEvents) {
				fireEvent(new ChangeEvent());
			}
		}
		label.setText(converter.convertFrom(value));
	}

	private void stopEditing(String newValue) {
		if (editing) {
			try {
				setValue(converter.convertTo(newValue));
				stopEditing();
			} catch (Exception e) {
				// TODO Show error msg
			}
		}
	}

	private void stopEditing() {
		if (editing) {
			editing = false;
			panel.clear();
			panel.add(label);
		}
	}

	private void startEditing() {

		if (!editing) {
			editing = true;
			panel.clear();
			final Editor editor = new Editor();

			panel.add(editor.editField);
			editor.editField.setFocus(true);
		}

	}

	private class Editor {

		private TextBox editField;

		public Editor() {
			super();
			editField = new TextBox();
			editField.setText(converter.convertFrom(value));
			editField.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						stopEditing(editField.getText());
					} else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
						stopEditing();
					}
				}
			});
			editField.addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent event) {
					stopEditing(editField.getText());
				}
			});

			// SwingUtilities.invokeLater(new Runnable() {
			// @Override
			// public void run() {
			// editField.requestFocus();
			// }
			// });
		}
	}
}
