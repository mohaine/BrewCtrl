package com.mohaine.brewcontroller.android.display;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.ClickEvent;
import com.mohaine.brewcontroller.android.HasView;
import com.mohaine.event.ClickHandler;

public class ControlPanelAndroid implements HasView {

	@Inject
	protected Context context;
	protected LinearLayout layout;

	public void addClickable(String name, final ClickHandler ch) {

		Button button = new Button(context);
		button.setText(name);
		button.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ch.onClick(new ClickEvent() {
				});
			}
		});
		layout.addView(button);

	}

	public void init() {
		layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
	}

	@Override
	public View getView() {
		return layout;
	}

}
