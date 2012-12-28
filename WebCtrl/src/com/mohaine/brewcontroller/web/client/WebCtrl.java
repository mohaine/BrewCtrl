package com.mohaine.brewcontroller.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mohaine.brewcontroller.client.page.Overview;

public class WebCtrl implements EntryPoint {
	public void onModuleLoad() {
		WebCtrlInjector injector = GWT.create(WebCtrlInjectorDefault.class);

		Overview overview = injector.getOverview();

		FlowPanel fp = new FlowPanel();
		fp.add(new HTML("Brew Ctrl"));
		fp.add((Widget) overview.getWidget());
		RootPanel.get("brewCtrl").add(fp);

	}
}
