package com.mohaine.brewcontroller.android.display;

import android.view.View;
import android.widget.TextView;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.layout.BreweryLayout;
import com.mohaine.brewcontroller.page.Overview.OverviewDisplay;

public class OverviewDisplayAndroid extends ControlPanelAndroid implements OverviewDisplay {
	private static final String TAG = "OverviewDisplayAndroid";
	private BreweryDisplayAndroid breweryDisplay;

	@Inject
	public OverviewDisplayAndroid() {

	}

	@Override
	public void init() {
		super.init();
		layout.addView(createLabel("Name"));
		breweryDisplay = new BreweryDisplayAndroid(getView().getContext());
		layout.addView(breweryDisplay);

	}

	@Override
	public void setBreweryLayout(BreweryLayout brewLayout) {
		breweryDisplay.setBreweryLayout(brewLayout);
	}

	private View createLabel(String string) {
		TextView tv = new TextView(context);
		tv.setText(string);
		return tv;
	}

	@Override
	public void cleanup() {

	}

}
