package com.mohaine.brewcontroller.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class WebCtrl implements EntryPoint {
	public void onModuleLoad() {
		WebCtrlInjector injector = GWT.create(WebCtrlInjectorDefault.class);
		HorizontalPanel panel = new HorizontalPanel();
		panel.add((Widget) injector.getOverview().getWidget());
		panel.add((Widget) injector.getStatusPanel());
		RootPanel.get("brewCtrl").add(panel);

	}
}
