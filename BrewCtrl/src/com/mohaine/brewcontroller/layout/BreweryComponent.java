package com.mohaine.brewcontroller.layout;

public abstract class BreweryComponent {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public abstract String getType();

	@Override
	public String toString() {
		return name;
	}

}
