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
import com.mohaine.brewcontroller.ClickEvent;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.event.ClickHandler;
import com.mohaine.event.bus.EventBus;

public class Overview extends BasePage {

	public interface OverviewDisplay {
		public void addClickable(String name, ClickHandler ch);

		public void init();

		public void setBreweryLayout(BreweryLayout layout);

	}

	private OverviewDisplay display;

	private ControllerGui controllerGui;

	private MainMenu mainMenu;

	private Controller controller;

	@Inject
	public Overview(OverviewDisplay displayp, EventBus eventBusp, Controller controller, final ControllerGui ci, final MainMenu mainMenu) {
		super();
		this.display = displayp;
		this.mainMenu = mainMenu;
		this.controllerGui = ci;
		this.controller = controller;
		display.init();

		display.addClickable("Main Menu", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				goMainMenu();
			}

		});

		display.setBreweryLayout(controller.getLayout());

	}

	private void goMainMenu() {
		controllerGui.displayPage(mainMenu);
	}

	@Override
	public String getTitle() {
		return "Overview";
	}

	@Override
	public Object getWidget() {
		return display;
	}
}
