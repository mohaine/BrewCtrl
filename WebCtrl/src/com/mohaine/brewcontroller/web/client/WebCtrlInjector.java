package com.mohaine.brewcontroller.web.client;

import com.google.gwt.inject.client.Ginjector;
import com.mohaine.brewcontroller.client.page.Overview;

public interface WebCtrlInjector extends Ginjector {
	Overview getOverview();

	StatusPanel getStatusPanel();

}
