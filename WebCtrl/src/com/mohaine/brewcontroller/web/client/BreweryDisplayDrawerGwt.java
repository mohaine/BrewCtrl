package com.mohaine.brewcontroller.web.client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.Converter;
import com.mohaine.brewcontroller.client.UnitConversion;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.display.BreweryComponentDisplay;
import com.mohaine.brewcontroller.client.display.BreweryDisplay.BreweryDisplayDrawer;
import com.mohaine.brewcontroller.client.display.DrawerMouseListener.DrawerMouseEvent;
import com.mohaine.brewcontroller.client.display.DrawerMouseListener;
import com.mohaine.brewcontroller.client.layout.BreweryComponent;
import com.mohaine.brewcontroller.client.layout.HeatElement;
import com.mohaine.brewcontroller.client.layout.Pump;
import com.mohaine.brewcontroller.client.layout.Sensor;
import com.mohaine.brewcontroller.client.layout.Tank;

public class BreweryDisplayDrawerGwt implements BreweryDisplayDrawer {
	private static final int TANK_TOP_HEIGHT = 20;
	private int PADDING = 5;

	private UnitConversion conversion;
	private ControllerHardware controller;
	private NumberFormat numberFormat = NumberFormat.getFormat("0.0");
	private NumberFormat numberFormatWhole = NumberFormat.getFormat("0");
	static final String UPGRADE_MESSAGE = "Your browser does not support the HTML5 Canvas.";
	private FlowPanel panel = new FlowPanel();

	private Canvas canvas;
	private Canvas backBuffer;
	private List<BreweryComponentDisplay> displays;
	private Context2d context;
	private Context2d backBufferContext;

	@Inject
	public BreweryDisplayDrawerGwt(ControllerHardware controller, UnitConversion conversion) {
		this.controller = controller;
		this.conversion = conversion;

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
		for (BreweryComponentDisplay display : displays) {
			BreweryComponent displayComponent = display.getComponent();
			if (displayComponent == component) {
				drawComponent(backBufferContext, display, true);
				context.drawImage(backBufferContext.getCanvas(), 0, 0);
			}
		}
	}

	@Override
	public void addMouseListener(final DrawerMouseListener drawerMouseListener) {
		canvas.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent e) {
				drawerMouseListener.mouseDown(new DrawerMouseEvent(e.getX(), e.getY()));
			}
		});
		canvas.addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent e) {
				drawerMouseListener.mouseUp(new DrawerMouseEvent(e.getX(), e.getY()));
			}
		});

		canvas.addMouseMoveHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent e) {
				if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
					drawerMouseListener.mouseDragged(new DrawerMouseEvent(e.getX(), e.getY()));
				}
			}
		});

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
		g.fillText(text, left, top + height);
	}

	private void drawSensor(Context2d g, BreweryComponentDisplay display) {
		Sensor sensor = (Sensor) display.getComponent();
		Double tempatureC = sensor.getTempatureC();
		if (tempatureC != null) {
			final Converter<Double, Double> tempDisplayConveter = conversion.getTempDisplayConveter();
			String tempDisplay = numberFormat.format(tempDisplayConveter.convertFrom(tempatureC)) + "\u00B0";

			CssColor textColor;
			if (sensor.isReading()) {
				textColor = Colors.FOREGROUND;
			} else {
				textColor = Colors.ERROR;
			}
			drawText(g, tempDisplay, textColor, false, display.getAbsLeft(), display.getAbsTop(), display.getWidth(), 30, Colors.TEMP_FONT);

			boolean clearText = true;
			ControlStep selectedStep = controller.getSelectedStep();
			if (selectedStep != null) {
				ControlPoint cp = selectedStep.getControlPointForAddress(sensor.getAddress());
				if (cp != null && cp.isAutomaticControl()) {
					// if (selectedStep.isActive()) {
					// if (cp.getTargetTemp() != sensor.getTargetTemp()) {
					// textColor = Colors.PENDING;
					// }
					// }
					clearText = false;
					tempDisplay = numberFormatWhole.format(tempDisplayConveter.convertFrom(cp.getTargetTemp())) + "\u00B0";
					drawText(g, "(" + tempDisplay + ")", textColor, false, display.getAbsLeft(), display.getAbsTop() + 30, display.getWidth(), 30, Colors.TEMP_TARGET_FONT);
				}
			}

			if (clearText) {
				drawText(g, "", textColor, false, display.getAbsLeft(), display.getAbsTop() + 30, display.getWidth(), 30, Colors.TEMP_TARGET_FONT);
			}
		}
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

	private void drawPump(Context2d g, BreweryComponentDisplay display) {
		g.setFont(Colors.TEXT_FONT);
		drawName(g, display);

		int top = display.getTop();
		int left = display.getLeft();
		int width = display.getWidth();
		int height = display.getHeight() - 15;

		// drawG.setColor(Colors.BACKGROUND);
		// drawG.fillRect(0, 0, width, height);

		Pump pump = (Pump) display.getComponent();
		boolean on = pump.isOn();

		CssColor backPaint = null;

		ControlStep selectedStep = controller.getSelectedStep();
		if (selectedStep != null) {
			ControlPoint controlPointForPin = selectedStep.getControlPointForPin(pump.getPin());
			if (controlPointForPin != null) {
				int cpDuty = controlPointForPin.getDuty();

				if (controlPointForPin.isAutomaticControl()) {
					backPaint = Colors.INACTIVE;
				}

				if (selectedStep.isActive()) {

					if (controlPointForPin.isAutomaticControl()) {

					} else {
						if (on != cpDuty > 0) {
							on = cpDuty > 0;
							backPaint = Colors.PENDING;
						}
					}
				} else {
					on = cpDuty > 0;
				}
			}
		}

		if (backPaint == null) {
			backPaint = (on ? Colors.PUMP_ON : Colors.PUMP_OFF);
		}

		CssColor strokePaint = Colors.FOREGROUND;

		int cirSize = (int) (Math.min(width, height) - 1);

		int cirRadius = cirSize / 2;
		// g.strokeRect(display.getLeft(), display.getTop(), display.getWidth(),
		// display.getHeight());
		g.save();
		g.translate(left, top);

		g.setStrokeStyle(strokePaint);
		g.setFillStyle(backPaint);
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
