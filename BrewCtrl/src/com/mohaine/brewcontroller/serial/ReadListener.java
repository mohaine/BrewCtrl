package com.mohaine.brewcontroller.serial;

public interface ReadListener<T> {
	public void onRead(T t);
}
