package com.mohaine.brewcontroller.android;

import roboguice.inject.InjectView;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ControllerGui;
import com.mohaine.brewcontroller.DisplayPage;

public class ControllerInterfaceAndroid implements ControllerGui {
	@Inject
	protected Context context;

	@InjectView(R.id.buttons)
	LinearLayout layout;

	private DisplayPage currentPage;

	@Override
	public void displayPage(DisplayPage page) {
		System.out.println("BrewCtrlAndroidActivity.displayPage("
				+ page.getTitle() + ")");

		// context.setTitle(page.getTitle());

		if (currentPage != null) {
			currentPage.hidePage();
			layout.removeAllViews();
		}
		currentPage = page;

		Object widget = page.getWidget();
		if (widget != null) {
			if (widget instanceof HasView) {
				View viewGroup = ((HasView) widget).getView();

				System.out.println("viewGroup: " + viewGroup);
				layout.addView(viewGroup);
			}
		}

		System.out.println("widget: " + widget);

		page.showPage();
	}

	@Override
	public void init() {

	}

}
