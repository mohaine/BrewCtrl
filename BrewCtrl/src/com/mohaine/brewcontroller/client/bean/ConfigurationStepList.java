package com.mohaine.brewcontroller.client.bean;

import java.util.List;

import com.mohaine.brewcontroller.shared.json.ListType;

public class ConfigurationStepList {
	private String name;

	@ListType(ConfigurationStep.class)
	private List<ConfigurationStep> steps;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ConfigurationStep> getSteps() {
		return steps;
	}

	public void setSteps(List<ConfigurationStep> steps) {
		this.steps = steps;
	}

}
