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

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.mohaine.brewcontroller.ClickEvent;
import com.mohaine.event.ClickHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasClickHandlers;

public class SwingHasClickHandlers extends AbstractAction implements HasClickHandlers {
	private static final long serialVersionUID = 1L;

	private ArrayList<ClickHandler> handlers;

	public SwingHasClickHandlers() {
		super();
	}

	public SwingHasClickHandlers(String name, Icon icon) {
		super(name, icon);
	}

	public SwingHasClickHandlers(String name) {
		super(name);
	}

	@Override
	public HandlerRegistration addClickHandler(final ClickHandler handler) {
		if (handlers == null) {
			handlers = new ArrayList<ClickHandler>();
		}
		handlers.add(handler);
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				handlers.remove(handler);
			}
		};
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (handlers != null) {

			ArrayList<ClickHandler> handlers = new ArrayList<ClickHandler>(this.handlers);

			for (ClickHandler clickHandler : handlers) {
				clickHandler.onClick(new ClickEvent() {
				});
			}
		}

	}
}
