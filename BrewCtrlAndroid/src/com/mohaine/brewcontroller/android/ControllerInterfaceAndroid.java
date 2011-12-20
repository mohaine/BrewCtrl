package com.mohaine.brewcontroller.android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.DisplayPage;

public class ControllerInterfaceAndroid implements ControllerGui {
	private static final String TAG = "ControllerInterfaceAndroid";
	@Inject
	protected Context context;
	@Inject
	protected Provider<Activity> providerActivity;

	private DisplayPage currentPage;

	@Override
	public void displayPage(DisplayPage page) {
		Log.v(TAG, "ControllerInterfaceAndroid.displayPage(" + page.getTitle() + ")");

		Activity activity = providerActivity.get();
		LinearLayout layout = (LinearLayout) activity.findViewById(R.id.pageDisplay);
		if (currentPage != null) {
			currentPage.hidePage();
		}
		currentPage = page;

		activity.setTitle(page.getTitle());

		Object widget = page.getWidget();
		if (widget != null) {
			if (widget instanceof HasView) {
				layout.removeAllViews();
				View viewGroup = ((HasView) widget).getView();
				layout.addView(viewGroup);
				layout.invalidate();
			}
		}
		page.showPage();
	}

	@Override
	public void init() {

	}

}
