package com.mohaine.brewcontroller.web.client;

import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
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
	private ControllerHardware controller;

	static final String UPGRADE_MESSAGE = "Your browser does not support the HTML5 Canvas.";
	private FlowPanel panel = new FlowPanel();

	private Canvas canvas;
	private Canvas backBuffer;
	private List<BreweryComponentDisplay> displays;
	private Context2d context;
	private Context2d backBufferContext;

	@Inject
	public BreweryDisplayDrawerGwt(ControllerHardware controller) {
		this.controller = controller;

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

	private void drawHeatElement(Context2d g, BreweryComponentDisplay display) {
		HeatElement heater = (HeatElement) display.getComponent();
		int duty = heater.getDuty();
		String text = null;
		CssColor color = Colors.FOREGROUND;

		ControlStep selectedStep = controller.getSelectedStep();
		if (selectedStep != null) {

			ControlPoint controlPointForPin = selectedStep.getControlPointForPin(heater.getPin());
			if (controlPointForPin != null) {
				int cpDuty = controlPointForPin.getDuty();

				if (controlPointForPin.isAutomaticControl()) {
					color = Colors.INACTIVE;
				}

				if (selectedStep.isActive()) {
					if (controlPointForPin.isAutomaticControl()) {
						text = Integer.toString(duty) + "%";
					} else {
						if (duty != cpDuty) {
							color = Colors.PENDING;
						}
						text = Integer.toString(cpDuty) + "%";
					}

				} else {
					if (controlPointForPin.isAutomaticControl()) {
						text = "Auto";
					} else {
						text = Integer.toString(cpDuty) + "%";
					}
				}
			}
		}

		if (text == null) {
			text = Integer.toString(duty) + "%";
		}
		drawText(g, display, text, color, true);
	}

	private void drawText(Context2d g, BreweryComponentDisplay display, String tempDisplay, CssColor textColor, boolean alignRight) {
		drawText(g, tempDisplay, textColor, alignRight, display.getAbsLeft(), display.getAbsTop(), display.getWidth(), display.getHeight(), Colors.TEMP_FONT);
	}

	private void drawText(Context2d g, String text, CssColor textColor, boolean alignRight, int left, int top, int width, int height, String font) {
		g.setFont(font);
		g.setFillStyle(textColor);
		g.fillText(text, left, top);
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

		int textHeight = 10;

		int boxHeight = height - TANK_TOP_HEIGHT - textHeight - 5;

		g.setFillStyle(Colors.TANK);
		g.setStrokeStyle(Colors.FOREGROUND);

		int boxTopLeft = top + TANK_TOP_HEIGHT / 2;

		// Bottom round
		drawEllipse(g, left, top + boxHeight, width, TANK_TOP_HEIGHT);
		// Middle
		g.fillRect(left, boxTopLeft, width, boxHeight);
		g.setFillStyle(Colors.TANK_INSIDE);
		g.beginPath();
		g.moveTo(left, boxTopLeft);
		g.lineTo(left, boxTopLeft + boxHeight);
		g.moveTo(left + width, boxTopLeft);
		g.lineTo(left + width, boxTopLeft + boxHeight);
		g.stroke();

		// Top
		drawEllipse(g, left, top, width, TANK_TOP_HEIGHT);

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
