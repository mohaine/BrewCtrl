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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.bean.HeaterStep;
import com.mohaine.event.AbstractHasValue;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.HasValue;

public class StepEditorSwing extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTextField nameField = new JTextField();
	private HasValue<String> nameHasValue = new AbstractHasValue<String>() {

		@Override
		public String getValue() {
			return nameField.getText();
		}

		@Override
		public void setValue(String value, boolean fireEvents) {
			nameField.setText(value);
			if (fireEvents) {
				fireEvent(new ChangeEvent());
			}
		}
	};

	private TimespanEditorSwing timeValue = new TimespanEditorSwing();

	private HeaterStep heaterStep;

	@Inject
	public StepEditorSwing() {
		super();

		JPanel mainPanel = this;

		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JPanel namePanel = createTitledPanel("Step Name");
		namePanel.add(nameField, BorderLayout.CENTER);
		mainPanel.add(namePanel, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridheight = 1;

		JPanel timePanel = createTitledPanel("Step Time");
		timePanel.add(timeValue, BorderLayout.CENTER);
		mainPanel.add(timePanel, gbc);

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;

		mainPanel.add(new JPanel(), gbc);

		nameField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				nameHasValue.fireEvent(new ChangeEvent());
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

	}

	private JPanel createTitledPanel(String name) {
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BorderLayout());
		namePanel.setBorder(BorderFactory.createTitledBorder(name));
		return namePanel;
	}

	public HasValue<String> getNameValue() {
		return nameHasValue;
	}

	public HasValue<Long> getTimeValue() {
		return timeValue;
	}

	public void setStep(HeaterStep heaterStep) {
		this.heaterStep = heaterStep;
		nameHasValue.setValue(heaterStep.getName(), false);
		timeValue.setValue(heaterStep.getStepTime(), false);

	}

}
