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

public class StepModifyEvent extends Event<StepModifyEventHandler> {
	private static Type<StepModifyEventHandler> TYPE;

	public static Type<StepModifyEventHandler> getType() {
		return TYPE != null ? TYPE : (TYPE = new Type<StepModifyEventHandler>());
	}

	private final HeaterStep step;
	private final boolean fromServer;

	public StepModifyEvent(HeaterStep step, boolean fromServer) {
		super();
		this.step = step;
		this.fromServer = fromServer;
	}

	public StepModifyEvent(HeaterStep step) {
		super();
		this.step = step;
		this.fromServer = false;
	}

	@Override
	public void dispatch(StepModifyEventHandler event) {
		event.onStepChange(step, fromServer);
	}

	@Override
	public final Type<StepModifyEventHandler> getAssociatedType() {
		return TYPE;
	}
}
