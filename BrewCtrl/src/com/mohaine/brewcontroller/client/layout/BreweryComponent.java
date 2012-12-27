package com.mohaine.brewcontroller.client.layout;

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
		if (name != null) {
			return name;
		}
		return super.toString();
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof BreweryComponent) {
			BreweryComponent breweryComponent = (BreweryComponent) obj;
			return name.equals(breweryComponent.name);
		}

		return super.equals(obj);
	}
}
