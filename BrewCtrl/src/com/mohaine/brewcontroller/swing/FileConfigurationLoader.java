package com.mohaine.brewcontroller.swing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.mohaine.brewcontroller.BrewJsonConverterRefection;
import com.mohaine.brewcontroller.ConfigurationLoader;
import com.mohaine.brewcontroller.client.bean.Configuration;
import com.mohaine.brewcontroller.server.util.FileUtils;
import com.mohaine.brewcontroller.server.util.StreamUtils;
import com.mohaine.brewcontroller.shared.json.JsonObjectConverter;
import com.mohaine.brewcontroller.shared.json.JsonPrettyPrint;

public class FileConfigurationLoader implements ConfigurationLoader {

	private File configFile;

	public FileConfigurationLoader(File configFile) {
		this.configFile = configFile;
	}

	public void saveConfiguration(Configuration config) throws Exception {
		JsonObjectConverter jc = new BrewJsonConverterRefection().getJsonConverter();
		String json = jc.encode(config);

		JsonPrettyPrint jpp = new JsonPrettyPrint();
		jpp.setStripNullAttributes(true);
		byte[] cfg = jpp.prettyPrint(json).getBytes();
		boolean dirty = false;

		if (configFile.exists()) {
			String newSha1 = FileUtils.getSHA1(new ByteArrayInputStream(cfg));
			String existingSha1 = FileUtils.getSHA1(configFile);
			if (!newSha1.equals(existingSha1)) {
				configFile.renameTo(new File(configFile.getParentFile(), configFile.getName() + ".bak"));
				dirty = true;
			}
		} else {
			dirty = true;
		}

		if (dirty) {
			OutputStream fis = new FileOutputStream(configFile);
			try {
				fis.write(cfg);
			} finally {
				StreamUtils.close(fis);
			}
		}

	}

	public synchronized Configuration getConfiguration() {

		return loadConfiguration();
	}

	private Configuration loadConfiguration() {
		try {

			InputStream fis = new FileInputStream(configFile);
			return readCfg(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Configuration readCfg(InputStream fis) throws IOException, Exception {
		try {
			String json = new String(StreamUtils.readStream(fis));
			JsonObjectConverter jc = new BrewJsonConverterRefection().getJsonConverter();
			return jc.decode(json, Configuration.class);
		} finally {
			StreamUtils.close(fis);
		}
	}
}
