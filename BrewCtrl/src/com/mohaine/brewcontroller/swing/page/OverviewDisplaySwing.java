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

package com.mohaine.brewcontroller.swing.page;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.BrewPrefs;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.layout.BrewLayout;
import com.mohaine.brewcontroller.page.Overview.OverviewDisplay;
import com.mohaine.brewcontroller.swing.SwingHasClickHandlers;
import com.mohaine.event.ClickHandler;

public class OverviewDisplaySwing extends JPanel implements OverviewDisplay {
	private static final long serialVersionUID = 1L;
	private Hardware hardware;
	private BrewPrefs prefs;
	private JPanel controlPanel = new JPanel();

	@Inject
	public OverviewDisplaySwing(Hardware hardware, final BrewPrefs prefs) {
		super();
		this.hardware = hardware;
		this.prefs = prefs;

		setLayout(new BorderLayout());

		JPanel sliderPanel = new JPanel();
		add(sliderPanel, BorderLayout.EAST);

		add(controlPanel, BorderLayout.SOUTH);

	}

	@Override
	public void init() {

	}

	@Override
	public void setBreweryLayout(BrewLayout layout) {

	}

	@Override
	public void addClickable(String name, ClickHandler ch) {
		SwingHasClickHandlers setupClickable = new SwingHasClickHandlers(name);
		setupClickable.addClickHandler(ch);
		controlPanel.add(new JButton(setupClickable));

	}
}
