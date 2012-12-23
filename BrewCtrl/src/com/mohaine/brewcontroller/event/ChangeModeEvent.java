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

import com.mohaine.brewcontroller.bean.HardwareControl.Mode;
import com.mohaine.event.bus.Event;

public class ChangeModeEvent extends Event<ChangeModeEventHandler> {
	private static Type<ChangeModeEventHandler> TYPE;

	public static Type<ChangeModeEventHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<ChangeModeEventHandler>());
	}

	private final Mode mode;

	public ChangeModeEvent(Mode mode) {
		super();
		this.mode = mode;
	}

	@Override
	public void dispatch(ChangeModeEventHandler event) {
		event.onChangeMode(mode);
	}

	@Override
	public final Type<ChangeModeEventHandler> getAssociatedType() {
		return TYPE;
	}
}
