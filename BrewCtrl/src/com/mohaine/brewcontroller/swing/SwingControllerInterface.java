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

package com.mohaine.brewcontroller.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.ControllerUrlLoader;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.DisplayPage;
import com.mohaine.brewcontroller.client.bean.Configuration;

public class SwingControllerInterface implements ControllerGui {
	private JFrame frame = new JFrame();
	private JPanel mainPanel = new JPanel();
	private JPanel urlPanel = new JPanel();

	DisplayPage currentPage;
	private StatusDisplaySwing statusDisplay;
	private ControllerUrlLoader urlLoader;
	private ControllerHardware hardware;

	@Inject
	public SwingControllerInterface(ControllerUrlLoader urlLoader, StatusDisplaySwing statusDisplay, ControllerHardware hardware) {
		super();
		this.statusDisplay = statusDisplay;
		this.hardware = hardware;
		this.urlLoader = urlLoader;
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

		urlPanel.setLayout(new BorderLayout());
		urlPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		urlPanel.add(new JLabel("URL:"), BorderLayout.WEST);
		final JTextField urlTextField = new JTextField();
		urlTextField.setText(urlLoader.getUrl());

		urlTextField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent paramKeyEvent) {
				if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
					urlLoader.setUrl(urlTextField.getText());
				} else if (paramKeyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
					urlTextField.setText(urlLoader.getUrl());
				}
			}
		});
		urlTextField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				urlLoader.setUrl(urlTextField.getText());
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});

		urlPanel.add(urlTextField, BorderLayout.CENTER);
		urlPanel.add(new JButton(new AbstractAction("Load Configuration") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChoose = new JFileChooser(new File(System.getProperty("user.dir")));
				fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChoose.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return "JSON files";
					}

					@Override
					public boolean accept(File file) {
						return file.getName().toLowerCase().endsWith(".json");
					}
				});

				fileChoose.showDialog(frame, "Select Configuration");
				File selectedFile = fileChoose.getSelectedFile();
				if (selectedFile != null && selectedFile.exists() && selectedFile.isFile()) {
					try {
						Configuration configuration = new FileConfigurationLoader(selectedFile).getConfiguration();
						if (configuration == null) {
							JOptionPane.showInternalMessageDialog(frame.getContentPane(), "Invalid Configuration File");
						} else {
							hardware.setConfiguration(configuration);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showInternalMessageDialog(frame.getContentPane(), "Invalid Configuration File");
					}

				}

			}
		}), BorderLayout.EAST);

		Container cp = frame.getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(urlPanel, BorderLayout.NORTH);
		cp.add(mainPanel, BorderLayout.CENTER);
		cp.add(statusDisplay, BorderLayout.EAST);

		// mainPanel.setPreferredSize(new Dimension(800, 600));
		frame.pack();
		frame.setVisible(true);
	}
}
