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

package com.mohaine.brewcontroller.page;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.event.BreweryLayoutChangeEvent;
import com.mohaine.brewcontroller.client.event.BreweryLayoutChangeEventHandler;
import com.mohaine.brewcontroller.client.event.ClickHandler;
import com.mohaine.brewcontroller.client.event.bus.EventBus;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;

public class Overview extends BasePage {

	public interface OverviewDisplay {
		public void addClickable(String name, ClickHandler ch);

		public void init();

		public void setBreweryLayout(BreweryLayout layout);

		public void cleanup();

	}

	private OverviewDisplay display;

	@Inject
	public Overview(OverviewDisplay displayp, EventBus eventBusp, ControllerHardware controller) {
		super();
		this.display = displayp;
		display.init();

		display.setBreweryLayout(controller.getBreweryLayout());

		eventBusp.addHandler(BreweryLayoutChangeEvent.getType(), new BreweryLayoutChangeEventHandler() {
			@Override
			public void onChange(BreweryLayout breweryLayout) {

				display.setBreweryLayout(breweryLayout);
				
				
			}
		});

	}

	@Override
	public String getTitle() {
		return "Overview";
	}

	@Override
	public Object getWidget() {
		return display;
	}

	@Override
	public void hidePage() {
		super.hidePage();

		display.cleanup();
	}

}
