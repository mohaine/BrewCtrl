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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mohaine.brewcontroller.layout.Pump;

public class PumpView extends LinearLayout {
	private static final String TAG = "PumpView";
	private static final int STROKE_WIDTH = 2;
	@SuppressWarnings("unused")
	private Pump pump;

	public PumpView(Context context) {
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

	public void setPump(Pump pump) {
		this.pump = pump;
		this.removeAllViews();

		Log.v(TAG, "Set Pump: " + pump.getName());
		TextView textView = new TextView(getContext());
		textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		textView.setText(pump.getName());
		addView(textView);

	}
}
