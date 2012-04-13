package com.mohaine.brewcontroller;

public interface ConfigurationLoader {

	public Configuration getConfiguration();

	public void saveConfiguration() throws Exception;
}
