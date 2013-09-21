/*
    Copyright 2009-2013 Michael Graessle

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

package com.mohaine.brewcontroller.web.client.page;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.display.BreweryDisplay;
import com.mohaine.brewcontroller.client.event.ClickHandler;
import com.mohaine.brewcontroller.client.layout.BreweryLayout;
import com.mohaine.brewcontroller.client.page.Overview.OverviewDisplay;

public class OverviewDisplayGwt extends Composite implements OverviewDisplay {
	private BreweryDisplay breweryDisplay;

	@Inject
	public OverviewDisplayGwt(ControllerHardware hardware, BreweryDisplay breweryDisplayp) {
		super();
		this.breweryDisplay = breweryDisplayp;

		FlowPanel fp = new FlowPanel();

		fp.add((Widget) breweryDisplay.getDisplay());

		initWidget(fp);
	}

	@Override
	public void init() {

	}

	@Override
	public void setBreweryLayout(BreweryLayout layout) {
		if (layout != null) {
			breweryDisplay.setBreweryLayout(layout);
		}
	}

	@Override
	public void addClickable(String name, ClickHandler ch) {
		// SwingHasClickHandlers setupClickable = new
		// SwingHasClickHandlers(name);
		// setupClickable.addClickHandler(ch);
		// controlPanel.add(new JButton(setupClickable));
	}

	@Override
	public void cleanup() {
		breweryDisplay.cleanup();

	}

}
