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
import com.mohaine.brewcontroller.page.MainMenu.MainMenuDisplay;
import com.mohaine.event.ClickHandler;

public class MainMenuDisplayAndroid implements MainMenuDisplay, HasView {

	@Inject
	protected Context context;
	private LinearLayout layout;

	@Override
	public void addClickable(String name, final ClickHandler ch) {

		initLayout();

		// TODO Auto-generated method stub
		if (layout != null) {
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
		} else {
			System.out.println("MainMenuDisplayAndroid.addClickable() " + name
					+ " NO LAYOUT");
		}
	}

	private void initLayout() {
		if (layout == null) {
			layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
		}
	}

	@Override
	public View getView() {
		return layout;
	}

}
