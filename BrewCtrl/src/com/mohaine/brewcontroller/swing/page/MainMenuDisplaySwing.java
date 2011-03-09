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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.mohaine.brewcontroller.page.MainMenu.MainMenuDisplay;
import com.mohaine.brewcontroller.swing.SwingHasClickHandlers;
import com.mohaine.event.ClickHandler;

public class MainMenuDisplaySwing extends JPanel implements MainMenuDisplay {
	private static final long serialVersionUID = 1L;
	private GridBagConstraints gbc = new GridBagConstraints();
	private JPanel menuPanel = new JPanel();

	public MainMenuDisplaySwing() {
		super();

		add(menuPanel);

		menuPanel.setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.insets = new Insets(2, 2, 2, 2);

	}

	@Override
	public void addClickable(String name, ClickHandler ch) {
		gbc.gridy++;
		SwingHasClickHandlers setupClickable = new SwingHasClickHandlers(name);
		setupClickable.addClickHandler(ch);
		menuPanel.add(new JButton(setupClickable), gbc);

	}

}
