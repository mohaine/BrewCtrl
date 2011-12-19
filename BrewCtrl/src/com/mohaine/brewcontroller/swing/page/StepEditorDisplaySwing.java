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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.Converter;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.UnitConversion;
import com.mohaine.brewcontroller.bean.HardwareStatus;
import com.mohaine.brewcontroller.page.StepEditor.StepEditorDisplay;
import com.mohaine.brewcontroller.swing.SwingHasClickHandlers;
import com.mohaine.brewcontroller.swing.TimespanEditorSwing;
import com.mohaine.brewcontroller.swing.ValueSlider;
import com.mohaine.event.AbstractHasValue;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ClickHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;
import com.mohaine.event.StatusChangeHandler;

public class StepEditorDisplaySwing extends JPanel implements StepEditorDisplay {

	private static final long serialVersionUID = 1L;
	private List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	private JPanel controlPanel = new JPanel();
	private ValueSlider tunTempSlider;
	private ValueSlider hltTempSlider;

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
	private Hardware hardware;
	private Converter<Double, Double> tempDisplayConveter;

	@Inject
	public StepEditorDisplaySwing(UnitConversion conversion, Hardware hardware) {
		super();

		this.hardware = hardware;
		tempDisplayConveter = conversion.getTempDisplayConveter();
		tunTempSlider = new ValueSlider(120, tempDisplayConveter.convertTo(100.0), tempDisplayConveter.convertTo(212.0), tempDisplayConveter);
		hltTempSlider = new ValueSlider(120, tempDisplayConveter.convertTo(100.0), tempDisplayConveter.convertTo(212.0), tempDisplayConveter);

		setLayout(new BorderLayout());

		JPanel sliderPanel = new JPanel();
		add(sliderPanel, BorderLayout.EAST);

		add(controlPanel, BorderLayout.SOUTH);

		JPanel hltPanel = createTitledPanel("HLT");
		hltPanel.add(hltTempSlider);
		sliderPanel.add(hltPanel);

		JPanel tunPanel = createTitledPanel("Tun");
		tunPanel.add(tunTempSlider);
		sliderPanel.add(tunPanel);

		JPanel mainPanel = new JPanel();
		add(mainPanel, BorderLayout.CENTER);

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

	@Override
	public void addNotify() {
		synchronized (handlers) {
			handlers.add(hardware.addStatusChangeHandler(new StatusChangeHandler() {
				@Override
				public void onStateChange() {

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							HardwareStatus hardwareStatus = hardware.getHardwareStatus();

							updateOtherTemp(hardwareStatus, hardware.getHardwareStatus().getHltSensor(), hltTempSlider);
							updateOtherTemp(hardwareStatus, hardware.getHardwareStatus().getTunSensor(), tunTempSlider);

						}

						private void updateOtherTemp(HardwareStatus hardwareStatus, String sensor, ValueSlider slider) {
							Double actualTemp = hardware.getSensorTemp(sensor);
							if (actualTemp != null) {

								Double target = slider.getValue();

								if (Math.abs(target - actualTemp) < 1.5) { // Temp
																			// C
									slider.setOtherValueColor(new Color(0, 255, 0, 30));
								} else {
									slider.setOtherValueColor(new Color(255, 0, 0, 90));
								}

								slider.setDrawOtherValue(true);
								slider.setOtherValue(tempDisplayConveter.convertFrom(actualTemp));
							} else {
								slider.setDrawOtherValue(false);
							}
						}
					});
				}
			}));
		}
		super.addNotify();
	}

	@Override
	public void removeNotify() {
		synchronized (handlers) {
			for (HandlerRegistration handlerReg : handlers) {
				handlerReg.removeHandler();
			}
			handlers.clear();
		}
		super.removeNotify();
	}

	private JPanel createTitledPanel(String name) {
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BorderLayout());
		namePanel.setBorder(BorderFactory.createTitledBorder(name));
		return namePanel;
	}

	@Override
	public HasValue<Double> getHltTemp() {
		return hltTempSlider;
	}

	@Override
	public HasValue<Double> getTunTemp() {
		return tunTempSlider;
	}

	@Override
	public HasValue<String> getNameValue() {
		return nameHasValue;
	}

	@Override
	public HasValue<Long> getTimeValue() {
		return timeValue;
	}

	@Override
	public void addClickable(String name, ClickHandler ch) {
		SwingHasClickHandlers setupClickable = new SwingHasClickHandlers(name);
		setupClickable.addClickHandler(ch);
		controlPanel.add(new JButton(setupClickable));

	}

	@Override
	public void init() {
		
	}

}
