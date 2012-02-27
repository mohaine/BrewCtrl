package com.mohaine.brewcontroller.bd;

import com.mohaine.brewcontroller.layout.BreweryComponent;

public class BreweryComponentDisplay {

	private BreweryComponent component;
	private int height;
	private int width;
	private int top;
	private int left;

	public BreweryComponentDisplay(BreweryComponent component) {
		this.component = component;
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public BreweryComponent getComponent() {
		return component;
	}

	public void setComponent(BreweryComponent component) {
		this.component = component;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

}
