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
import java.awt.Canvas;
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.BrewPrefs;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.bd.BreweryDisplay;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.page.Overview.OverviewDisplay;
import com.mohaine.brewcontroller.swing.SwingHasClickHandlers;
import com.mohaine.event.ClickHandler;

public class OverviewDisplaySwing extends JPanel implements OverviewDisplay {
	private static final long serialVersionUID = 1L;
	private Hardware hardware;
	private BrewPrefs prefs;
	private JPanel controlPanel = new JPanel();
	private Canvas canvas;
	private BreweryDisplay breweryDisplay;

	@Inject
	public OverviewDisplaySwing(Hardware hardware, final BrewPrefs prefs, BreweryDisplay breweryDisplayp) {
		super();
		this.hardware = hardware;
		this.prefs = prefs;
		this.breweryDisplay = breweryDisplayp;
		setLayout(new BorderLayout());
		JPanel sliderPanel = new JPanel();
		add(sliderPanel, BorderLayout.EAST);
		add(controlPanel, BorderLayout.SOUTH);

		Component drawer = (Component) breweryDisplay.getDrawer();
		add(drawer, BorderLayout.CENTER);
		drawer.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentResized(ComponentEvent arg0) {
				breweryDisplay.layoutDisplays();
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	@Override
	public void init() {

	}

	@Override
	public void setBreweryLayout(BreweryLayout layout) {
		breweryDisplay.setBreweryLayout(layout);
	}

	@Override
	public void addClickable(String name, ClickHandler ch) {
		SwingHasClickHandlers setupClickable = new SwingHasClickHandlers(name);
		setupClickable.addClickHandler(ch);
		controlPanel.add(new JButton(setupClickable));
	}
}
