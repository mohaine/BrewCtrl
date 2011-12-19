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
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

import com.mohaine.brewcontroller.Converter;
import com.mohaine.event.ChangeEvent;
import com.mohaine.event.ChangeHandler;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.HasValue;

public class ValueSlider extends View implements HasValue<Double> {
	private static final long serialVersionUID = 1L;

	private final class MouseDownRunnable implements Runnable {
		private boolean up = false;
		private boolean running = false;
		private boolean holdingMouseDown = false;

		@Override
		public void run() {
			synchronized (this) {
				running = true;

				try {
					wait(500);
				} catch (InterruptedException e) {
				}
				while (holdingMouseDown) {

					try {
						wait(50);
					} catch (InterruptedException e) {
					}

					if (holdingMouseDown) {
						// SwingUtilities.invokeLater(new Runnable() {
						// @Override
						// public void run() {
						// updateValue(value + (up ? step : -step));
						// }
						// });
					}
				}
				running = false;
			}
		}
	}

	private static final int SLIDER_HEIGHT = 3;
	private static final int BUTTON_HEIGHT = 20;

	private NumberFormat nf = new DecimalFormat("0.#");
	private Paint valueColor = new Paint(Color.LTGRAY);
	private double value = 50;
	private double minValue = 0;
	private double maxValue = 100;
	private int step = 1;

	private boolean drawOtherValue = false;
	private double otherValue = 0;
	private Paint otherValueColor = new Paint(Color.RED);

	private ArrayList<ChangeHandler> changeHandlers = new ArrayList<ChangeHandler>();

	private MouseDownRunnable mouseDownRunable = new MouseDownRunnable();
	private Converter<Double, Double> displayConverter;

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

		// setPreferredSize(new Dimension(50, 200));
		// addMouseListener(this);
		// addMouseMotionListener(this);
		// addMouseWheelListener(this);
	}

	@Override
	protected void onDraw(Canvas g) {
		super.onDraw(g);

		int width = getWidth();
		int height = getHeight();

		// g.drawColor(Color.WHITE);
		g.drawColor(Color.BLACK);

		Paint paint = new Paint(Color.WHITE);

		g.drawRect(new Rect(0, 0, width - 1, BUTTON_HEIGHT - 1), paint);

		int arrowTopBottom = 5;
		int arrowLeftRight = width / 3;
		Path p = new Path();

		p.moveTo(width / 2, arrowTopBottom);
		p.moveTo(arrowLeftRight, BUTTON_HEIGHT - arrowTopBottom);
		p.moveTo(width - arrowLeftRight, BUTTON_HEIGHT - arrowTopBottom);
		g.drawPath(p, paint);
		p.reset();
		p.moveTo(width / 2, height - arrowTopBottom);
		p.moveTo(arrowLeftRight, height - (BUTTON_HEIGHT - arrowTopBottom));
		p.moveTo(width - arrowLeftRight + 1, height - (BUTTON_HEIGHT - arrowTopBottom));
		g.drawPath(p, paint);

		g.drawRect(new Rect(0, height - BUTTON_HEIGHT, width - 1, BUTTON_HEIGHT - 1), paint);

		// Slider Bottom Fill
		int offset = getLocation(value, height);
		g.drawRect(1, offset, width - 2, height - offset - BUTTON_HEIGHT, valueColor);

		// Other Value Fill
		if (drawOtherValue) {
			int otherOvalueOffset = getLocation(otherValue, height);
			g.drawRect(new Rect(1, otherOvalueOffset, width - 2, height - otherOvalueOffset - BUTTON_HEIGHT), otherValueColor);
			drawValue(g, width, otherValue, otherOvalueOffset);

		}

		// Slider
		g.drawRect(new Rect(0, offset - SLIDER_HEIGHT, width, SLIDER_HEIGHT), paint);
		drawValue(g, width, value, offset);
	}

	private void drawValue(Canvas g, int width, double displayValue, int displayOffset) {
		// FontMetrics fontMetrics = g.getFontMetrics();
		// String string = nf.format(displayValue);
		// int stringWidth = fontMetrics.stringWidth(string);
		// int textOffset = displayOffset - fontMetrics.getDescent() - 1;
		//
		// if (textOffset - fontMetrics.getAscent() < BUTTON_HEIGHT) {
		// textOffset = displayOffset + fontMetrics.getAscent() + 1;
		// }
		// Graphics2D g2 = (Graphics2D) g;
		// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		// g.drawString(string, (width - stringWidth) / 2, textOffset);
		// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_OFF);
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

	// @Override
	// public void mouseClicked(MouseEvent event) {
	// updateForEvent(event);
	// }
	//
	// private void updateForEvent(MouseEvent event) {
	// int y = event.getY();
	// if (y < BUTTON_HEIGHT) {
	// updateValue(value + step);
	// } else if (y > getHeight() - BUTTON_HEIGHT) {
	// updateValue(value - step);
	// } else {
	// updateValue(getValue(y));
	// }
	// }
	//
	//
	// @Override
	// public void mouseExited(MouseEvent event) {
	// cancelThread();
	// }
	//
	// private void cancelThread() {
	// synchronized (mouseDownRunable) {
	// mouseDownRunable.holdingMouseDown = false;
	// mouseDownRunable.notify();
	// }
	// }
	//
	// @Override
	// public void mousePressed(MouseEvent event) {
	// int y = event.getY();
	// boolean upButton = y < BUTTON_HEIGHT;
	// boolean downButton = y > getHeight() - BUTTON_HEIGHT;
	// if (upButton || downButton) {
	// synchronized (mouseDownRunable) {
	// if (!mouseDownRunable.running) {
	// mouseDownRunable.up = upButton;
	// mouseDownRunable.holdingMouseDown = true;
	// mouseDownRunable.running = true;
	// new Thread(mouseDownRunable).start();
	// }
	// }
	// }
	// }
	//
	// @Override
	// public void mouseReleased(MouseEvent event) {
	// cancelThread();
	// }
	//
	// @Override
	// public void mouseDragged(MouseEvent event) {
	// updateForEvent(event);
	// }
	//
	// @Override
	// public void mouseMoved(MouseEvent event) {
	// int y = event.getY();
	// if (!(y < BUTTON_HEIGHT || y > getHeight() - BUTTON_HEIGHT)) {
	// cancelThread();
	// }
	//
	// }
	//
	// @Override
	// public void mouseWheelMoved(MouseWheelEvent event) {
	// updateValue(value + -event.getWheelRotation() * step);
	// }

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
		// TODO
		refreshDrawableState();
	}

	public boolean isDrawOtherValue() {
		return drawOtherValue;
	}

	public void setDrawOtherValue(boolean drawOtherValue) {
		this.drawOtherValue = drawOtherValue;
	}

	public Paint getOtherValueColor() {
		return otherValueColor;
	}

	public void setOtherValueColor(Paint otherValueColor) {
		this.otherValueColor = otherValueColor;
	}

}
