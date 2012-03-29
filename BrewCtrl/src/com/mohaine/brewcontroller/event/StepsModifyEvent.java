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

import com.mohaine.event.bus.Event;

public class StepsModifyEvent extends Event<StepsModifyEventHandler> {
	private static Type<StepsModifyEventHandler> TYPE;

	public static Type<StepsModifyEventHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<StepsModifyEventHandler>());
	}

	public StepsModifyEvent() {
		super();
	}

	@Override
	public void dispatch(StepsModifyEventHandler event) {
		event.onStepsChange();
	}

	@Override
	public final Type<StepsModifyEventHandler> getAssociatedType() {
		return TYPE;
	}
}
