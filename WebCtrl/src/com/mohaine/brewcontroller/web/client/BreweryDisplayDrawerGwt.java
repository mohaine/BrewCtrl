package com.mohaine.brewcontroller.web.client;

import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.display.BreweryComponentDisplay;
import com.mohaine.brewcontroller.client.display.BreweryDisplay.BreweryDisplayDrawer;
import com.mohaine.brewcontroller.client.display.DrawerMouseListener;
import com.mohaine.brewcontroller.client.layout.BreweryComponent;
import com.mohaine.brewcontroller.client.layout.HeatElement;
import com.mohaine.brewcontroller.client.layout.Pump;
import com.mohaine.brewcontroller.client.layout.Sensor;
import com.mohaine.brewcontroller.client.layout.Tank;

public class BreweryDisplayDrawerGwt implements BreweryDisplayDrawer {
	private static final int TANK_TOP_HEIGHT = 20;
	private int PADDING = 5;
	// private UnitConversion conversion;
	// private ControllerHardware controller;

	static final String UPGRADE_MESSAGE = "Your browser does not support the HTML5 Canvas.";
	private FlowPanel panel = new FlowPanel();

	private Canvas canvas;
	private Canvas backBuffer;
	private List<BreweryComponentDisplay> displays;
	private Context2d context;
	private Context2d backBufferContext;

	@Inject
	public BreweryDisplayDrawerGwt() {

		canvas = Canvas.createIfSupported();
		if (canvas == null) {
			panel.add(new Label(UPGRADE_MESSAGE));
			return;
		}
		backBuffer = Canvas.createIfSupported();

		// init the canvases
		int width = getWidth();
		int height = getHeight();

		canvas.setWidth(width + "px");
		canvas.setHeight(height + "px");
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
		backBuffer.setCoordinateSpaceWidth(width);
		backBuffer.setCoordinateSpaceHeight(height);

		panel.add(canvas);
		context = canvas.getContext2d();
		backBufferContext = backBuffer.getContext2d();

	}

	private int getHeight() {
		return 800;
	}

	private int getWidth() {
		return 800;
	}

	@Override
	public void setDisplays(List<BreweryComponentDisplay> displays) {
		this.displays = displays;
	}

	@Override
	public void redrawBreweryComponent(BreweryComponent component) {
		// TODO Auto-generated method stub
		System.out.println("BreweryDisplayDrawerGwt.redrawBreweryComponent()");
	}

	@Override
	public void addMouseListener(DrawerMouseListener drawerMouseListener) {
		// TODO Auto-generated method stub
		System.out.println("BreweryDisplayDrawerGwt.addMouseListener()");
	}

	@Override
	public void redrawAll() {

		backBufferContext.setFillStyle(Colors.BACKGROUND);
		backBufferContext.fillRect(0, 0, getWidth(), getHeight());

		for (BreweryComponentDisplay display : displays) {
			drawComponent(backBufferContext, display, true);
		}

		context.drawImage(backBufferContext.getCanvas(), 0, 0);

	}

	private void drawComponent(Context2d context2d, BreweryComponentDisplay display, boolean full) {
		BreweryComponent component = display.getComponent();
		if (Tank.TYPE.equals(component.getType())) {
			if (full) {
				drawTank(context2d, display);
			}
		} else if (Pump.TYPE.equals(component.getType())) {
			drawPump(context2d, display);
			if (full) {
				drawName(context2d, display);
			}
		} else if (Sensor.TYPE.equals(component.getType())) {
			drawSensor(context2d, display);
		} else if (HeatElement.TYPE.equals(component.getType())) {
			drawHeatElement(context2d, display);
		} else {
			// g2.drawRect(display.getAbsLeft(), display.getAbsTop(),
			// display.getWidth(), display.getHeight());
			drawName(context2d, display);
		}
	}

	private void drawHeatElement(Context2d context2d, BreweryComponentDisplay display) {
		// TODO Auto-generated method stub

	}

	private void drawSensor(Context2d context2d, BreweryComponentDisplay display) {
		// TODO Auto-generated method stub

	}

	private void drawName(Context2d g, BreweryComponentDisplay display) {
		String name = display.getComponent().getName();
		if (name != null && name.length() > 0) {
			g.setFont(Colors.TEXT_FONT);
			g.setFillStyle(Colors.FOREGROUND);
			int left = display.getLeft();
			int width = display.getWidth();
			TextMetrics measureText = g.measureText(name);
			g.fillText(name, (int) (left + (width / 2) - measureText.getWidth() / 2), (int) (display.getTop() + display.getHeight() - 3));
		}
	}

	private void drawPump(Context2d context2d, BreweryComponentDisplay display) {
		// TODO Auto-generated method stub

	}

	private void drawTank(Context2d g, BreweryComponentDisplay display) {
		drawName(g, display);

		int top = display.getTop();
		int left = display.getLeft();
		int width = display.getWidth();
		int height = display.getHeight();

		CssColor tankColor = Colors.TANK;
		CssColor strokePaint = Colors.FOREGROUND;

		int textHeight = 10;

		int boxHeight = height - TANK_TOP_HEIGHT - textHeight - 5;
		int boxR = boxHeight / 2;
		// bottom arc
		// Shape circleBottom = new Ellipse2D.Float(left, top + boxHeight,
		// width, TANK_TOP_HEIGHT);
		g.setFillStyle(tankColor);
		g.setStrokeStyle(strokePaint);

		int boxTopLeft = top + TANK_TOP_HEIGHT / 2;

		drawEllipse(g, left, top + boxHeight, width, TANK_TOP_HEIGHT);

		g.fillRect(left, boxTopLeft, width, boxHeight);

		g.setFillStyle(Colors.TANK_INSIDE);

		g.beginPath();
		g.moveTo(left, boxTopLeft);
		g.lineTo(left, boxTopLeft + boxHeight);
		g.moveTo( left+ width, boxTopLeft);
		g.lineTo(left+ width, boxTopLeft + boxHeight);
		g.stroke();

		drawEllipse(g, left, top, width, TANK_TOP_HEIGHT);

		// // Center
		// int boxTopLeft = top + TANK_TOP_HEIGHT / 2;
		// Shape square = new Rectangle2D.Double(left, boxTopLeft, width,
		// boxHeight);
		// g.setFillStyle(tankColor);
		// g.fill(square);
		// g.setFillStyle(strokePaint);
		// g.drawLine(left, boxTopLeft, left, boxTopLeft + boxHeight);
		// g.drawLine(left + width, boxTopLeft, left + width, boxTopLeft +
		// boxHeight);
		//
		// // Inside/Top of tank
		// Shape circleTop = new Ellipse2D.Float(left, top, width,
		// TANK_TOP_HEIGHT);
		// g.setFillStyle(Colors.TANK_INSIDE);
		// g.fill(circleTop);
		// g.setFillStyle(strokePaint);
		// g.draw(circleTop);
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

}
