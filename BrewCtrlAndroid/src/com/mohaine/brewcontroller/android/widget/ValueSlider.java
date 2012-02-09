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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mohaine.brewcontroller.Converter;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;

public class ValueSlider extends View implements HasValue<Double> {

	private static final int FG_COLOR = Color.BLACK;
	private static final int STROKE_WIDTH = 2;
	private static final int SLIDER_HEIGHT = 5;
	private static final int BUTTON_HEIGHT = 25;

	private final class MouseDownRunnable implements Runnable {
		private boolean up = false;
		private boolean running = false;
		private boolean run = false;

		@Override
		public void run() {
			synchronized (this) {
				running = true;

				try {
					wait(500);
				} catch (InterruptedException e) {
				}
				while (run) {
					Log.v(TAG, "TOUCH DOWN THREAD");

					try {
						wait(100);
					} catch (InterruptedException e) {
					}

					if (run) {

						Log.v(TAG, "ValueSlider PROCESS TICK");

						post(new Runnable() {
							@Override
							public void run() {

								double valueBefore = value;
								updateValue(value + (up ? step : -step));

								if (valueBefore == value) {
									Log.v(TAG, "ValueSlider Stop Thread VALUE");

									run = false;
								}
							}
						});
					}
				}
				running = false;
			}
		}
	}

	private static final String TAG = "ValueSlider";

	private NumberFormat nf = new DecimalFormat("0.#");
	private int valueColor = Color.rgb(175, 175, 175);
	private double value = 50;
	private double minValue = 0;
	private double maxValue = 100;
	private int step = 1;

	private boolean drawOtherValue = false;
	private double otherValue = 0;
	private int otherValueColor = Color.RED;

	private ArrayList<ChangeHandler> changeHandlers = new ArrayList<ChangeHandler>();

	private MouseDownRunnable mouseDownRunable = new MouseDownRunnable();
	private Converter<Double, Double> displayConverter;
	private boolean inDrag;

	public ValueSlider(Context context) {
		super(context);
	}

	public ValueSlider(Context context, double value, double minValue, double maxValue) {
		super(context);
		this.value = value;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public ValueSlider(Context context, double value, double minValue, double maxValue, Converter<Double, Double> displayConverter) {
		super(context);
		this.value = value;
		this.minValue = displayConverter.convertFrom(minValue);
		this.maxValue = displayConverter.convertFrom(maxValue);
		this.displayConverter = displayConverter;
	}

	{

		this.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return updateForEvent(event);
			}
		});

	}

	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);

		int width = getWidth();
		int height = getHeight();

		c.drawColor(Color.WHITE);
		c.drawColor(Color.LTGRAY);

		Paint paint = new Paint(FG_COLOR);
		paint.setAntiAlias(true);

		// Arrows
		int arrowTopBottom = 5;
		int arrowLeftRight = width / 3;
		Path p = new Path();
		paint.setStyle(Paint.Style.FILL);
		int center = width / 2;

		p.moveTo(center, arrowTopBottom);
		p.lineTo(arrowLeftRight, BUTTON_HEIGHT - arrowTopBottom);
		p.lineTo(width - arrowLeftRight, BUTTON_HEIGHT - arrowTopBottom);
		c.drawPath(p, paint);
		p.reset();
		p.moveTo(center, height - arrowTopBottom);
		p.lineTo(arrowLeftRight, height - (BUTTON_HEIGHT - arrowTopBottom));
		p.lineTo(width - arrowLeftRight + 1, height - (BUTTON_HEIGHT - arrowTopBottom));
		c.drawPath(p, paint);

		int offset = getLocation(value, height);

		// Area under slider
		paint.setStyle(Paint.Style.FILL);
		if (drawOtherValue) {
			paint.setColor(otherValueColor);
		} else {
			paint.setColor(valueColor);
		}
		c.drawRect(0, offset, width, height - BUTTON_HEIGHT, paint);

		// Slider
		paint.setColor(FG_COLOR);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		c.drawRect(0, offset, width, offset - SLIDER_HEIGHT, paint);
		drawValue(c, paint, width, value, offset);

		// Borders
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(STROKE_WIDTH);

		c.drawRect(0, 0, width, height, paint);
		c.drawRect(0, BUTTON_HEIGHT, width, height - BUTTON_HEIGHT, paint);

	}

	private void drawValue(Canvas c, Paint paint, int width, double displayValue, int displayOffset) {

		FontMetrics fontMetrics = paint.getFontMetrics();
		String string = nf.format(displayValue);
		float stringWidth = paint.measureText(string);

		float textOffset = (int) displayOffset - fontMetrics.descent - SLIDER_HEIGHT;

		float topOfText = textOffset + fontMetrics.ascent - 2;

		if (topOfText <= BUTTON_HEIGHT) {
			textOffset = displayOffset - fontMetrics.ascent + SLIDER_HEIGHT;
		}
		c.drawText(string, (width - stringWidth) / 2, textOffset, paint);
	}

	private int getLocation(double lValue, int height) {
		double range = maxValue - minValue;
		double percent = (lValue - minValue) / range;
		return height - (int) ((height - SLIDER_HEIGHT - BUTTON_HEIGHT - BUTTON_HEIGHT) * percent) - BUTTON_HEIGHT;
	}

	private double getValue(int y) {

		double rangeHeight = getHeight() - SLIDER_HEIGHT - BUTTON_HEIGHT - BUTTON_HEIGHT - 2;

		double percent = (rangeHeight - (y - BUTTON_HEIGHT - 2)) / (rangeHeight);
		double range = maxValue - minValue;

		double newTarget = minValue + (range * percent);

		double value = minValue;
		while (value < maxValue && value <= newTarget) {
			value += step;
		}
		return value;
	}

	private boolean updateForEvent(MotionEvent event) {
		Log.v(TAG, "ValueSlider.updateForEvent() at: " + event.getX() + "," + event.getY() + " Action: " + event.getAction());
		boolean down = event.getAction() == MotionEvent.ACTION_DOWN;
		boolean move = event.getAction() == MotionEvent.ACTION_MOVE;

		if (event.getAction() == MotionEvent.ACTION_CANCEL) {
			cancelTouchActions();
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			cancelTouchActions();
			return true;
		}

		int y = (int) event.getY();
		boolean upButton = y < BUTTON_HEIGHT;
		boolean downButton = y > getHeight() - BUTTON_HEIGHT;
		if (!inDrag && (upButton || downButton)) {
			synchronized (mouseDownRunable) {
				if (down) {
					if (upButton) {
						updateValue(value + step);
					} else {
						updateValue(value - step);
					}
					if (!mouseDownRunable.running) {
						Log.v(TAG, "ValueSlider.updateForEvent() Start Thread");
						mouseDownRunable.up = upButton;
						mouseDownRunable.run = true;
						mouseDownRunable.running = true;
						new Thread(mouseDownRunable).start();
					}
				} else if (move) {
				} else {
					cancelTouchActions();
				}
			}
		} else if (inDrag || down) {
			if (down || move) {
				inDrag = true;
				updateValue(getValue(y));
			} else {
				cancelTouchActions();
			}
		} else {
			cancelTouchActions();
		}
		return true;
	}

	private void cancelTouchActions() {
		Log.v(TAG, "ValueSlider.cancelTouchActions() ");
		inDrag = false;

		synchronized (mouseDownRunable) {
			if (mouseDownRunable.run) {
				Log.v(TAG, "ValueSlider.cancelTouchActions() Stop Thread");
				mouseDownRunable.run = false;
				mouseDownRunable.notify();
			}
		}
	}

	@Override
	public HandlerRegistration addChangeHandler(final ChangeHandler handler) {
		changeHandlers.add(handler);
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				changeHandlers.remove(handler);
			}
		};
	}

	@Override
	public void fireEvent(ChangeEvent event) {
		for (ChangeHandler handler : changeHandlers) {
			handler.onChange(event);
		}

	}

	@Override
	public Double getValue() {
		return displayConverter != null ? displayConverter.convertTo(value) : value;
	}

	@Override
	public void setValue(Double value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Double newValue, boolean fireEvents) {
		newValue = displayConverter != null ? displayConverter.convertFrom(newValue) : newValue;
		updateLocalValue(newValue, fireEvents);
	}

	private void updateValue(double newValue) {
		updateLocalValue(newValue, true);
	}

	private void updateLocalValue(double newValue, boolean fireEvents) {
		if (newValue < minValue) {
			newValue = minValue;
		}
		if (newValue > maxValue) {
			newValue = maxValue;
		}
		boolean dirty = newValue != value;
		if (dirty) {
			value = newValue;
			repaint();
			if (fireEvents) {
				fireEvent(new ChangeEvent());
			}
		}

	}

	public double getOtherValue() {
		return otherValue;
	}

	public void setOtherValue(double otherValue) {
		this.otherValue = otherValue;
		repaint();

	}

	private void repaint() {
		invalidate();
	}

	public boolean isDrawOtherValue() {
		return drawOtherValue;
	}

	public void setDrawOtherValue(boolean drawOtherValue) {
		this.drawOtherValue = drawOtherValue;
	}

	public int getOtherValueColor() {
		return otherValueColor;
	}

	public void setOtherValueColor(int otherValueColor) {
		this.otherValueColor = otherValueColor;
	}

}
