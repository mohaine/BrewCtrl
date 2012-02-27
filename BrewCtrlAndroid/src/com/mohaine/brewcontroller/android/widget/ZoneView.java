/*
    Copyright 2009-2011 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.mohaine.brewcontroller.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mohaine.brewcontroller.layout.Heater;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;

public class ZoneView extends LinearLayout {
	private static final String TAG = "ZoneView";
	private static final int STROKE_WIDTH = 2;
	@SuppressWarnings("unused")
	private Tank zone;

	public ZoneView(Context context) {
		super(context);
		setOrientation(LinearLayout.VERTICAL);

		this.setWillNotDraw(false);

	}

	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);

		int width = getWidth();
		int height = getHeight();

		Log.v(TAG, "onDraw: " + width + "," + height);

		// c.drawColor(Colors.BG_COLOR);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		// Borders
		paint.setColor(Colors.BORDER_COLOR);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(STROKE_WIDTH);

		c.drawRect(0, 0, width, height, paint);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	public void setZone(Tank zone) {
		this.zone = zone;
		this.removeAllViews();

		Log.v(TAG, "Set Zone: " + zone.getName());
		TextView textView = new TextView(getContext());
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

		textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		textView.setText(zone.getName());
		addView(textView);

		LinearLayout controlLayout = new LinearLayout(getContext());
		controlLayout.setOrientation(LinearLayout.HORIZONTAL);
		controlLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addView(controlLayout);

		Heater heater = zone.getHeater();
		if (heater != null) {
			ValueSlider slider = new ValueSlider(getContext(), 0, 0, 100);
			slider.setLayoutParams(new LayoutParams(100, 250));
			controlLayout.addView(slider);
		}
		Sensor sensor = zone.getSensor();
		if (sensor != null) {
			Log.v(TAG, "  Has Sensor");
			ValueSlider slider = new ValueSlider(getContext(), 0, 100, 210);
			slider.setLayoutParams(new LayoutParams(100, 250));
			controlLayout.addView(slider);
		}

	}
}
