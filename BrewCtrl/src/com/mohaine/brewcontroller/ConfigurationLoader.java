package com.mohaine.brewcontroller;

import com.mohaine.brewcontroller.client.bean.Configuration;

public interface ConfigurationLoader {

	public Configuration getConfiguration();

	public void saveConfiguration() throws Exception;
}
