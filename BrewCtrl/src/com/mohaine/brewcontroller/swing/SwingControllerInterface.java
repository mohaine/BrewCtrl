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

package com.mohaine.brewcontroller.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.client.DisplayPage;

public class SwingControllerInterface implements ControllerGui {
	private JFrame frame = new JFrame();
	private JPanel mainPanel = new JPanel();

	DisplayPage currentPage;
	private StatusDisplaySwing statusDisplay;
	private ConfigurationLoader configurationLoader;

	@Inject
	public SwingControllerInterface(StatusDisplaySwing statusDisplay, ConfigurationLoader configurationLoader) {
		super();
		this.configurationLoader = configurationLoader;
		this.statusDisplay = statusDisplay;
		mainPanel.setLayout(new BorderLayout());
	}

	@Override
	public void displayPage(final DisplayPage page) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				if (currentPage != null) {
					currentPage.hidePage();
					mainPanel.removeAll();
				}
				currentPage = page;
				page.showPage();
				mainPanel.add((Component) page.getWidget());
				mainPanel.invalidate();
				mainPanel.repaint();
				frame.setTitle(page.getTitle());
				frame.pack();
			}
		});

	}

	@Override
	public void init() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				try {
					configurationLoader.saveConfiguration();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
			}

			@Override
			public void windowActivated(WindowEvent arg0) {
			}
		});

		Container cp = frame.getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(mainPanel, BorderLayout.CENTER);
		cp.add(statusDisplay, BorderLayout.EAST);

		// mainPanel.setPreferredSize(new Dimension(800, 600));
		frame.pack();
		frame.setVisible(true);
	}

}
