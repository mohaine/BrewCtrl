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

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mohaine.brewcontroller.Converter;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;

public class ClickEditor<T> extends JPanel implements HasValue<T> {
	private static final long serialVersionUID = 1L;

	private JLabel label = new JLabel();
	private final Converter<T, String> converter;

	public ClickEditor(Converter<T, String> converter) {
		super();
		this.converter = converter;
		setLayout(new BorderLayout());
		add(label);

		label.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent paramMouseEvent) {

			}

			@Override
			public void mousePressed(MouseEvent paramMouseEvent) {
				startEditing();
			}

			@Override
			public void mouseExited(MouseEvent paramMouseEvent) {

			}

			@Override
			public void mouseEntered(MouseEvent paramMouseEvent) {

			}

			@Override
			public void mouseClicked(MouseEvent paramMouseEvent) {

			}
		});

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
			removeAll();
			add(label);
			this.doLayout();
			repaint();
		}
	}

	private void startEditing() {

		if (!editing) {
			editing = true;
			removeAll();
			final Editor editor = new Editor();

			add(editor.editField);
			this.doLayout();
			repaint();

			editor.editField.requestFocus();
		}

	}

	private class Editor {

		private JTextField editField;

		public Editor() {
			super();
			editField = new JTextField();
			editField.setText(converter.convertFrom(value));
			editField.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent paramKeyEvent) {
				}

				@Override
				public void keyReleased(KeyEvent paramKeyEvent) {
				}

				@Override
				public void keyPressed(KeyEvent paramKeyEvent) {
					if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
						stopEditing(editField.getText());
					} else if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
						stopEditing();
					}
				}
			});
			editField.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent paramFocusEvent) {
					stopEditing(editField.getText());
				}

				@Override
				public void focusGained(FocusEvent paramFocusEvent) {
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
