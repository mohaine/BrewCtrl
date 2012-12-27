package com.mohaine.brewcontroller;

import java.util.List;

import com.mohaine.brewcontroller.shared.json.ListType;

public class ConfigurationStepList {
	private String name;

	@ListType(ConfigurationHeaterStep.class)
	private List<ConfigurationHeaterStep> steps;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ConfigurationHeaterStep> getSteps() {
		return steps;
	}

	public void setSteps(List<ConfigurationHeaterStep> steps) {
		this.steps = steps;
	}

}
