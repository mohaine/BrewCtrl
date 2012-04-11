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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.inject.AbstractModule;
import com.mohaine.brewcontroller.CloseAction;
import com.mohaine.brewcontroller.Configuration;
import com.mohaine.brewcontroller.Controller;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.ControllerImpl;
import com.mohaine.brewcontroller.Hardware;
import com.mohaine.brewcontroller.SensorConfiguration;
import com.mohaine.brewcontroller.bd.BreweryDisplay.BreweryDisplayDrawer;
import com.mohaine.brewcontroller.json.JsonObjectConverter;
import com.mohaine.brewcontroller.json.JsonPrettyPrint;
import com.mohaine.brewcontroller.json.ReflectionJsonHandler;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.brewcontroller.page.MainMenu.MainMenuDisplay;
import com.mohaine.brewcontroller.page.Overview.OverviewDisplay;
import com.mohaine.brewcontroller.serial.MockComm;
import com.mohaine.brewcontroller.serial.SerialConnection;
import com.mohaine.brewcontroller.serial.SerialHardwareComm;
import com.mohaine.brewcontroller.swing.bd.BreweryDisplayDrawerSwing;
import com.mohaine.brewcontroller.swing.page.MainMenuDisplaySwing;
import com.mohaine.brewcontroller.swing.page.OverviewDisplaySwing;
import com.mohaine.brewcontroller.util.StreamUtils;
import com.mohaine.event.bus.EventBus;

public class BrewControllerSwingModule extends AbstractModule {

	private File configFile;

	public BrewControllerSwingModule(File configFile) {
		this.configFile = configFile;
	}

	private void saveToFile(File configFile, Configuration finalCfg) throws Exception, FileNotFoundException, IOException {
		JsonObjectConverter jc = getJsonConverter();
		String json = jc.encode(finalCfg);

		JsonPrettyPrint jpp = new JsonPrettyPrint();
		jpp.setStripNullAttributes(true);
		json = jpp.prettyPrint(json);

		OutputStream fis = new FileOutputStream(configFile);
		try {
			fis.write(json.getBytes());
		} finally {
			StreamUtils.close(fis);
		}

	}

	public Configuration initFromFile(File file) throws Exception, FileNotFoundException, IOException {
		JsonObjectConverter jc = getJsonConverter();

		InputStream fis = new FileInputStream(file);
		try {
			String json = new String(StreamUtils.readStream(fis));
			return jc.decode(json, Configuration.class);
		} finally {
			StreamUtils.close(fis);
		}
	}

	private static JsonObjectConverter getJsonConverter() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter(false);
		jc.addHandler(ReflectionJsonHandler.build(Configuration.class));
		jc.addHandler(ReflectionJsonHandler.build(SensorConfiguration.class));
		jc.addHandler(ReflectionJsonHandler.build(BreweryLayout.class));
		jc.addHandler(ReflectionJsonHandler.build(Tank.class));
		jc.addHandler(ReflectionJsonHandler.build(Sensor.class));
		jc.addHandler(ReflectionJsonHandler.build(HeatElement.class));
		jc.addHandler(ReflectionJsonHandler.build(Pump.class));
		return jc;
	}

	@Override
	protected void configure() {

		Configuration cfg = null;
		try {
			cfg = initFromFile(configFile);
		} catch (Exception e) {
			e.printStackTrace();
			cfg = new Configuration();
		}

		final Configuration finalCfg = cfg;
		bind(Configuration.class).toInstance(cfg);
		bind(CloseAction.class).toInstance(new CloseAction() {
			@Override
			public void run() {
				try {
					saveToFile(configFile, finalCfg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		bind(ControllerGui.class).to(SwingControllerInterface.class).asEagerSingleton();
		bind(Controller.class).to(ControllerImpl.class).asEagerSingleton();

		bind(MainMenuDisplay.class).to(MainMenuDisplaySwing.class);
		bind(OverviewDisplay.class).to(OverviewDisplaySwing.class);
		bind(Hardware.class).to(SerialHardwareComm.class).asEagerSingleton();

		// bind(SerialConnection.class).to(RxTxComm.class).asEagerSingleton();
		bind(SerialConnection.class).to(MockComm.class).asEagerSingleton();

		bind(EventBus.class).asEagerSingleton();

		bind(BreweryDisplayDrawer.class).to(BreweryDisplayDrawerSwing.class);

	}

}