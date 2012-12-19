package com.mohaine.brewcontroller.comm;

import java.io.InputStream;
import java.io.OutputStream;

public interface SerialConnection {

	/**
	 * 
	 * @return Error message - null is success
	 */
	public abstract String reconnectIfNeeded();

	public abstract void disconnect();

	public abstract OutputStream getOutputStream();

	public abstract InputStream getInputStream();

}