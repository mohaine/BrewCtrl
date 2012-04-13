package com.mohaine.brewcontroller.swing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.mohaine.brewcontroller.Configuration;
import com.mohaine.brewcontroller.ConfigurationHeaterStep;
import com.mohaine.brewcontroller.ConfigurationHeaterStepControlPoint;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.ConfigurationStepList;
import com.mohaine.brewcontroller.SensorConfiguration;
import com.mohaine.brewcontroller.json.JsonObjectConverter;
import com.mohaine.brewcontroller.json.JsonPrettyPrint;
import com.mohaine.brewcontroller.json.ReflectionJsonHandler;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;
import com.mohaine.brewcontroller.util.FileUtils;
import com.mohaine.brewcontroller.util.StreamUtils;

public class FileConfigurationLoader implements ConfigurationLoader {

	private File configFile;
	private Configuration config;

	public FileConfigurationLoader(File configFile) {
		this.configFile = configFile;
	}

	public void saveConfiguration() throws Exception {
		if (config != null) {
			JsonObjectConverter jc = getJsonConverter();
			String json = jc.encode(config);

			JsonPrettyPrint jpp = new JsonPrettyPrint();
			jpp.setStripNullAttributes(true);
			byte[] cfg = jpp.prettyPrint(json).getBytes();

			String newSha1 = FileUtils.getSHA1(new ByteArrayInputStream(cfg));
			String existingSha1 = FileUtils.getSHA1(configFile);
			if (!newSha1.equals(existingSha1)) {
				configFile.renameTo(new File(configFile.getParentFile(), configFile.getName() + ".bak"));
				OutputStream fis = new FileOutputStream(configFile);
				try {
					fis.write(cfg);
				} finally {
					StreamUtils.close(fis);
				}
			}
		}

	}

	public synchronized Configuration getConfiguration() {
		if (config == null) {
			config = loadConfiguration();
			if (config == null) {
				config = new Configuration();

			}
		}
		return config;
	}

	private Configuration loadConfiguration() {
		try {
			JsonObjectConverter jc = getJsonConverter();

			InputStream fis = new FileInputStream(configFile);
			try {
				String json = new String(StreamUtils.readStream(fis));
				return jc.decode(json, Configuration.class);
			} finally {
				StreamUtils.close(fis);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static JsonObjectConverter getJsonConverter() throws Exception {
		JsonObjectConverter jc = new JsonObjectConverter(false);
		jc.addHandler(ReflectionJsonHandler.build(Configuration.class));
		jc.addHandler(ReflectionJsonHandler.build(SensorConfiguration.class));
		jc.addHandler(ReflectionJsonHandler.build(ConfigurationStepList.class));
		jc.addHandler(ReflectionJsonHandler.build(ConfigurationHeaterStep.class));
		jc.addHandler(ReflectionJsonHandler.build(ConfigurationHeaterStepControlPoint.class));
		jc.addHandler(ReflectionJsonHandler.build(BreweryLayout.class));
		jc.addHandler(ReflectionJsonHandler.build(Tank.class));
		jc.addHandler(ReflectionJsonHandler.build(Sensor.class));
		jc.addHandler(ReflectionJsonHandler.build(HeatElement.class));
		jc.addHandler(ReflectionJsonHandler.build(Pump.class));
		return jc;
	}

}
