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

package com.mohaine.brewcontroller.event;

import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.event.bus.Event;

public class ChangeSelectedStepEvent extends Event<ChangeSelectedStepEventHandler> {
	private static Type<ChangeSelectedStepEventHandler> TYPE;

	public static Type<ChangeSelectedStepEventHandler> getType() {
		return TYPE != null ? TYPE
				: (TYPE = new Type<ChangeSelectedStepEventHandler>());
	}

	private final HeaterStep step;

	public ChangeSelectedStepEvent(HeaterStep step) {
		super();
		this.step = step;
	}

	@Override
	public void dispatch(ChangeSelectedStepEventHandler event) {
		event.onStepChange(step);
	}

	@Override
	public final Type<ChangeSelectedStepEventHandler> getAssociatedType() {
		return TYPE;
	}
}
