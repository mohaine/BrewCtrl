package com.mohaine.brewcontroller;

import java.util.prefs.Preferences;

public class ControllerUrlLoader {
	private static final String BREW_CTRL_URL = "BrewCtrlUrl";

	public static int DEFAULT_PORT = 2739;

	private String url = getPrefs().get(BREW_CTRL_URL, "http://raspberrypi:" + DEFAULT_PORT + "/");

	private Preferences getPrefs() {
		return Preferences.userNodeForPackage(getClass());
	}

	public synchronized String getUrl() {
		return url;
	}

	public synchronized void setUrl(String url) {
		this.url = url;
		getPrefs().put(BREW_CTRL_URL, url);
	}
}
