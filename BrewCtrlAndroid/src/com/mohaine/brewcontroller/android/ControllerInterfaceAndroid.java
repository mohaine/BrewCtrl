package com.mohaine.brewcontroller.android;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.DisplayPage;

public class ControllerInterfaceAndroid implements ControllerGui {
	@Inject
	protected Context context;
	@Inject
	protected Provider<Activity> providerActivity;

	@InjectView(R.id.pageDisplay)
	LinearLayout layout;

	private DisplayPage currentPage;

	@Override
	public void displayPage(DisplayPage page) {

		if (currentPage != null) {
			currentPage.hidePage();
			layout.removeAllViews();
		}
		currentPage = page;

		Activity activity = providerActivity.get();
		if (activity != null) {
			activity.setTitle(page.getTitle());
		}

		Object widget = page.getWidget();
		if (widget != null) {
			if (widget instanceof HasView) {
				View viewGroup = ((HasView) widget).getView();
				layout.addView(viewGroup);
			}
		}
		page.showPage();
	}

	@Override
	public void init() {

	}

}
