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

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mohaine.brewcontroller.ClickEvent;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.bean.TempSensor;
import com.mohaine.event.ClickHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.StatusChangeHandler;

public class Setup extends BasePage implements StatusChangeHandler {

	public interface SetupDisplay {
		public void addClickable(String name, ClickHandler ch);

		void setSensors(List<TempSensor> listSensors);

	}

	private SetupDisplay display;
	private Hardware hardware;
	private ArrayList<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	@Inject
	public Setup(SetupDisplay display, final Provider<MainMenu> providerMainMenu, final ControllerGui controllerInterface, Hardware hardware) {
		super();
		this.display = display;
		this.hardware = hardware;
		onStateChange();

		display.addClickable("Main Menu", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controllerInterface.displayPage(providerMainMenu.get());
			}
		});
	}

	@Override
	public void showPage() {
		synchronized (handlers) {
			handlers.add(hardware.addStatusChangeHandler(this));
		}
	}

	@Override
	public void onStateChange() {
		display.setSensors(hardware.getSensors());
	}

	@Override
	public void hidePage() {
		super.hidePage();
		synchronized (handlers) {
			for (HandlerRegistration handlerRg : handlers) {
				handlerRg.removeHandler();
			}
			handlers.clear();
		}
	}

	@Override
	public String getTitle() {
		return "Setup";
	}

	@Override
	public Object getWidget() {
		return display;
	}
}
