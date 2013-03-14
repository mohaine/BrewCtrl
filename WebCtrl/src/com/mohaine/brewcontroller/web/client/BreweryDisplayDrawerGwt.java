/*
 Copyright 2009-2013 Michael Graessle

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

package com.mohaine.brewcontroller.web.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.display.BreweryDisplayDrawer.DrawerCanvas;
import com.mohaine.brewcontroller.client.display.BreweryDisplayDrawer.RedrawHook;
import com.mohaine.brewcontroller.client.display.DrawStyle.BColor;
import com.mohaine.brewcontroller.client.display.DrawStyle.BFont;
import com.mohaine.brewcontroller.client.display.DrawStyle.HAlign;
import com.mohaine.brewcontroller.client.display.DrawerMouseListener;
import com.mohaine.brewcontroller.client.display.DrawerMouseListener.DrawerMouseEvent;

public class BreweryDisplayDrawerGwt implements DrawerCanvas<Context2d> {
	private static final int TANK_TOP_HEIGHT = 20;

	static final String UPGRADE_MESSAGE = "Your browser does not support the HTML5 Canvas.";
	private FlowPanel panel = new FlowPanel();

	private Canvas canvas;
	private Canvas backBuffer;
	private Context2d context;
	private Context2d backBufferContext;
	private MouseAdaptor mouseAdaptor = new MouseAdaptor();

	@Inject
	public BreweryDisplayDrawerGwt() {

		canvas = Canvas.createIfSupported();
		if (canvas == null) {
			panel.add(new Label(UPGRADE_MESSAGE));
			return;
		}
		backBuffer = Canvas.createIfSupported();
		canvas.addMouseDownHandler(mouseAdaptor);
		canvas.addMouseUpHandler(mouseAdaptor);
		canvas.addMouseMoveHandler(mouseAdaptor);

		panel.add(canvas);

	}

	private class MouseAdaptor implements MouseDownHandler, MouseUpHandler, MouseMoveHandler {

		List<DrawerMouseListener> listeners = new ArrayList<DrawerMouseListener>();
		private boolean leftDown;

		@Override
		public void onMouseDown(MouseDownEvent e) {
			if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
				leftDown = true;
				for (DrawerMouseListener drawerMouseListener : listeners) {
					drawerMouseListener.mouseDown(new DrawerMouseEvent(e.getX(), e.getY()));
				}
			}
		}

		@Override
		public void onMouseUp(MouseUpEvent e) {
			if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
				leftDown = false;
				for (DrawerMouseListener drawerMouseListener : listeners) {
					drawerMouseListener.mouseUp(new DrawerMouseEvent(e.getX(), e.getY()));
				}
			}
		}

		public void onMouseMove(MouseMoveEvent e) {
			for (DrawerMouseListener drawerMouseListener : listeners) {
				if (leftDown) {
					drawerMouseListener.mouseDragged(new DrawerMouseEvent(e.getX(), e.getY()));
				}
			}
		}
	}

	@Override
	public void addMouseListener(final DrawerMouseListener drawerMouseListener) {
		mouseAdaptor.listeners.add(drawerMouseListener);
	}

	private void drawEllipse(Context2d ctx, int x, int y, int w, int h) {
		double kappa = .5522848;
		double ox = (w / 2) * kappa; // control point offset horizontal
		double oy = (h / 2) * kappa; // control point offset vertical
		double xe = x + w; // x-end
		double ye = y + h; // y-end
		double xm = x + w / 2; // x-middle
		double ym = y + h / 2; // y-middle

		ctx.beginPath();
		ctx.moveTo(x, ym);
		ctx.bezierCurveTo(x, ym - oy, xm - ox, y, xm, y);
		ctx.bezierCurveTo(xm + ox, y, xe, ym - oy, xe, ym);
		ctx.bezierCurveTo(xe, ym + oy, xm + ox, ye, xm, ye);
		ctx.bezierCurveTo(xm - ox, ye, x, ym + oy, x, ym);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();
	}

	@Override
	public Object getWidget() {
		return panel;
	}

	@Override
	public void init(RedrawHook<Context2d> hook) {

	}

	@Override
	public void updateWidths(int width, int height) {
		canvas.setWidth(width + "px");
		canvas.setHeight(height + "px");
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
		backBuffer.setCoordinateSpaceWidth(width);
		backBuffer.setCoordinateSpaceHeight(height);

		context = canvas.getContext2d();
		backBufferContext = backBuffer.getContext2d();
	}

	@Override
	public Context2d getContext() {
		return backBufferContext;
	}

	@Override
	public void displayContext(Context2d context) {
		this.context.drawImage(context.getCanvas(), 0, 0);
	}

	@Override
	public void fillRect(Context2d context, int left, int top, int width, int height, BColor background) {
		context.setFillStyle(DrawStyleGwt.mapColor(background));
		context.fillRect(left, top, width, height);

	}

	@Override
	public void drawText(Context2d context, String text, BColor textColor, BColor bgColor, HAlign align, int left, int top, int width, int height, BFont font) {
		//TODO ALIGN
		
		int x;
		switch (align) {

		case RIGHT:
			x = (int) (width - stringBounds.getWidth());
			break;
		case CENTER:
			x = (int) (width / 2 - (stringBounds.getWidth() / 2));
			break;
		default:
			x = 0;
			break;
		}
		
		context.setFillStyle(DrawStyleGwt.mapColor(bgColor));
		context.fillRect(left, top, width, height);
		context.setFont(DrawStyleGwt.mapFont(font));
		context.setFillStyle(DrawStyleGwt.mapColor(textColor));
		context.fillText(text, left, top + height);
	}


	@Override
	public void drawPump(Context2d g, int left, int top, int width, int height, BColor backPaint, boolean on) {
		CssColor strokePaint = DrawStyleGwt.FOREGROUND;

		int cirSize = (int) (Math.min(width, height) - 1);

		int cirRadius = cirSize / 2;
		// g.strokeRect(display.getLeft(), display.getTop(), display.getWidth(),
		// display.getHeight());
		g.save();
		g.translate(left, top);

		g.setStrokeStyle(strokePaint);
		g.setFillStyle(DrawStyleGwt.mapColor(backPaint));
		g.fillRect(cirRadius, 0, width - cirRadius - 1, cirRadius * 0.67);
		g.strokeRect(cirRadius, 0, width - cirRadius - 1, cirRadius * 0.67);

		drawEllipse(g, 0, 0, cirSize, cirSize);

		if (!on) {
			int subSize = (int) (cirRadius * 0.7f);
			g.setFillStyle(strokePaint);
			drawEllipse(g, cirRadius - subSize / 2, cirRadius - subSize / 2, subSize, subSize);
		}

		g.restore();
	}

	@Override
	public void drawTank(Context2d g, int left, int top, int width, int height) {

		g.setFillStyle(DrawStyleGwt.TANK);
		g.setStrokeStyle(DrawStyleGwt.FOREGROUND);

		int boxTopLeft = top + TANK_TOP_HEIGHT / 2;

		// Bottom round
		drawEllipse(g, left, top + height - (TANK_TOP_HEIGHT), width, TANK_TOP_HEIGHT);
		
		// Middle
		g.fillRect(left, boxTopLeft, width, height-TANK_TOP_HEIGHT);
		g.setFillStyle(DrawStyleGwt.TANK_INSIDE);
		g.beginPath();
		
		g.moveTo(left, boxTopLeft);
		g.lineTo(left, boxTopLeft + height-TANK_TOP_HEIGHT);
		g.moveTo(left + width, boxTopLeft);
		g.lineTo(left + width, boxTopLeft + height-TANK_TOP_HEIGHT);
		g.stroke();

		// Top
		drawEllipse(g, left, top, width, TANK_TOP_HEIGHT);
	}

}
