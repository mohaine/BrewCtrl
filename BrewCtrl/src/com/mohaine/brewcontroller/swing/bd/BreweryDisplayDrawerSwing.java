package com.mohaine.brewcontroller.swing.bd;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.Converter;
import com.mohaine.brewcontroller.client.UnitConversion;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.display.BreweryComponentDisplay;
import com.mohaine.brewcontroller.client.display.BreweryDisplay.BreweryDisplayDrawer;
import com.mohaine.brewcontroller.client.display.DrawerMouseListener;
import com.mohaine.brewcontroller.client.display.DrawerMouseListener.DrawerMouseEvent;
import com.mohaine.brewcontroller.client.layout.BreweryComponent;
import com.mohaine.brewcontroller.client.layout.HeatElement;
import com.mohaine.brewcontroller.client.layout.Pump;
import com.mohaine.brewcontroller.client.layout.Sensor;
import com.mohaine.brewcontroller.client.layout.Tank;

public class BreweryDisplayDrawerSwing extends Canvas implements BreweryDisplayDrawer {
	private static final int TANK_TOP_HEIGHT = 20;
	private static final long serialVersionUID = 1L;
	private List<BreweryComponentDisplay> displays;
	private NumberFormat numberFormat = new DecimalFormat("0.0");
	private NumberFormat numberFormatWhole = new DecimalFormat("0");
	private UnitConversion conversion;
	private ControllerHardware controller;

	@Inject
	public BreweryDisplayDrawerSwing(final UnitConversion conversion, ControllerHardware controller) {
		super();
		this.conversion = conversion;
		this.controller = controller;
	}

	@Override
	public void redrawBreweryComponent(final BreweryComponent componentChanged) {
		if (SwingUtilities.isEventDispatchThread()) {
			redrawBreweryComponentSwing(componentChanged);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					redrawBreweryComponentSwing(componentChanged);
				}
			});
		}
	}

	private void redrawBreweryComponentSwing(BreweryComponent componentChanged) {
		Graphics2D g2 = (Graphics2D) getGraphics();
		if (g2 != null) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			for (BreweryComponentDisplay display : displays) {
				BreweryComponent displayComponent = display.getComponent();
				if (displayComponent == componentChanged) {
					drawComponent(g2, display, false);
				}
			}
		}
	}

	@Override
	public void redrawAll() {
		if (SwingUtilities.isEventDispatchThread()) {
			redrawAllSwing();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					redrawAllSwing();
				}
			});
		}
	}

	private void redrawAllSwing() {
		Graphics2D g2 = (Graphics2D) getGraphics();
		if (g2 != null) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (BreweryComponentDisplay display : displays) {
				drawComponent(g2, display, false);
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Colors.BACKGROUND);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

		for (BreweryComponentDisplay display : displays) {
			drawComponent(g2, display, true);
		}
	}

	private void drawComponent(Graphics2D g2, BreweryComponentDisplay display, boolean full) {
		BreweryComponent component = display.getComponent();
		if (Tank.TYPE.equals(component.getType())) {
			if (full) {
				drawTank(g2, display);
			}
		} else if (Pump.TYPE.equals(component.getType())) {
			drawPump(g2, display);
			if (full) {
				drawName(g2, display);
			}
		} else if (Sensor.TYPE.equals(component.getType())) {
			drawSensor(g2, display);
		} else if (HeatElement.TYPE.equals(component.getType())) {
			drawHeatElement(g2, display);
		} else {
			g2.drawRect(display.getAbsLeft(), display.getAbsTop(), display.getWidth(), display.getHeight());
			drawName(g2, display);
		}
	}

	private void drawTank(Graphics2D g, BreweryComponentDisplay display) {
		drawName(g, display);

		int top = display.getTop();
		int left = display.getLeft();
		int width = display.getWidth();
		int height = display.getHeight();

		Color tankColor = Colors.TANK;
		Color strokePaint = Colors.FOREGROUND;

		int textHeight = g.getFontMetrics().getAscent();

		int boxHeight = height - TANK_TOP_HEIGHT - textHeight - 5;

		// bottom arc
		Shape circleBottom = new Ellipse2D.Float(left, top + boxHeight, width, TANK_TOP_HEIGHT);
		g.setColor(tankColor);
		g.fill(circleBottom);
		g.setColor(strokePaint);
		g.draw(circleBottom);

		// Center
		int boxTopLeft = top + TANK_TOP_HEIGHT / 2;
		Shape square = new Rectangle2D.Double(left, boxTopLeft, width, boxHeight);
		g.setColor(tankColor);
		g.fill(square);
		g.setColor(strokePaint);
		g.drawLine(left, boxTopLeft, left, boxTopLeft + boxHeight);
		g.drawLine(left + width, boxTopLeft, left + width, boxTopLeft + boxHeight);

		// Inside/Top of tank
		Shape circleTop = new Ellipse2D.Float(left, top, width, TANK_TOP_HEIGHT);
		g.setColor(Colors.TANK_INSIDE);
		g.fill(circleTop);
		g.setColor(strokePaint);
		g.draw(circleTop);

		// if(display)

	}

	private void drawHeatElement(Graphics2D g, BreweryComponentDisplay display) {
		HeatElement heater = (HeatElement) display.getComponent();
		int duty = heater.getDuty();
		String text = null;
		Color color = Colors.FOREGROUND;

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

	private void drawSensor(Graphics2D g, BreweryComponentDisplay display) {
		Sensor sensor = (Sensor) display.getComponent();
		Double tempatureC = sensor.getTempatureC();
		if (tempatureC != null) {
			final Converter<Double, Double> tempDisplayConveter = conversion.getTempDisplayConveter();
			String tempDisplay = numberFormat.format(tempDisplayConveter.convertFrom(tempatureC)) + "\u00B0";

			Color textColor;
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

	private void drawText(Graphics2D g, BreweryComponentDisplay display, String tempDisplay, Color textColor, boolean alignRight) {
		drawText(g, tempDisplay, textColor, alignRight, display.getAbsLeft(), display.getAbsTop(), display.getWidth(), display.getHeight(), Colors.TEMP_FONT);
	}

	private void drawText(Graphics2D g, String text, Color textColor, boolean alignRight, int left, int top, int width, int height, Font font) {
		Image image = createImage(width, height);
		Graphics2D drawG = (Graphics2D) image.getGraphics();
		drawG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawG.setColor(Colors.TANK);
		drawG.fillRect(0, 0, width, height);

		drawG.setColor(textColor);
		drawG.setFont(font);

		FontMetrics fontMetrics = drawG.getFontMetrics();
		Rectangle2D stringBounds = fontMetrics.getStringBounds(text, drawG);

		int x = alignRight ? (int) (width - stringBounds.getWidth()) : 0;
		int y = (int) (-stringBounds.getCenterY()) + height / 2;

		drawG.drawString(text, x, y);

		g.drawImage(image, left, top, null);
		drawG.dispose();
	}

	private void drawPump(Graphics2D g, BreweryComponentDisplay display) {
		g.setFont(Colors.TEXT_FONT);
		FontMetrics fontMetrics = g.getFontMetrics();

		int top = display.getTop();
		int left = display.getLeft();
		int width = display.getWidth();
		int height = display.getHeight() - fontMetrics.getHeight();

		Image image = createImage(width, height);
		Graphics2D drawG = (Graphics2D) image.getGraphics();
		drawG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// drawG.setColor(Colors.BACKGROUND);
		// drawG.fillRect(0, 0, width, height);

		Pump pump = (Pump) display.getComponent();
		boolean on = pump.isOn();

		Color backPaint = null;

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

		Color strokePaint = Colors.FOREGROUND;

		int cirSize = (int) (Math.min(width, height) - 1);

		int cirRadius = cirSize / 2;

		Shape square = new Rectangle2D.Double(cirRadius, 0, width - cirRadius - 1, cirRadius * 0.67);
		drawG.setColor(backPaint);
		drawG.fill(square);
		drawG.setColor(strokePaint);
		drawG.draw(square);

		Shape circle = new Ellipse2D.Float(0, 0, cirSize, cirSize);
		drawG.setColor(backPaint);
		drawG.fill(circle);
		drawG.setColor(strokePaint);
		drawG.draw(circle);

		if (!on) {
			int subSize = (int) (cirRadius * 0.7f);
			Shape circle2 = new Ellipse2D.Float(cirRadius - subSize / 2, cirRadius - subSize / 2, subSize, subSize);
			drawG.fill(circle2);
		}

		g.drawImage(image, left, top, null);
		drawG.dispose();
	}

	private void drawName(Graphics2D g, BreweryComponentDisplay display) {
		String name = display.getComponent().getName();
		if (name != null && name.length() > 0) {
			g.setFont(Colors.TEXT_FONT);
			g.setColor(Colors.FOREGROUND);
			int left = display.getLeft();
			int width = display.getWidth();
			FontMetrics fontMetrics = g.getFontMetrics();
			Rectangle2D stringBounds = fontMetrics.getStringBounds(name, g);
			g.drawString(name, (int) (left + (width / 2) - stringBounds.getCenterX()), (int) (display.getTop() + display.getHeight() - 3));
		}
	}

	@Override
	public void setDisplays(List<BreweryComponentDisplay> displays, int width, int height) {
		invalidate();

		setPreferredSize(new Dimension(width, height));

		this.displays = new ArrayList<BreweryComponentDisplay>(displays);
	}

	// @Override
	// public Dimension getPreferredSize() {
	// Dimension preferredSize = super.getPreferredSize();
	//
	// int maxX = 0;
	// int maxY = 0;
	// for (BreweryComponentDisplay display : displays) {
	// maxX = Math.max(maxX, display.getLeft() + display.getWidth());
	// maxY = Math.max(maxY, display.getTop() + display.getHeight());
	// }
	// preferredSize.setSize(maxX + PADDING, maxY + PADDING);
	//
	// return preferredSize;
	// }

	@Override
	public void addMouseListener(final DrawerMouseListener drawerMouseListener) {

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {

			}

			@Override
			public void mouseDragged(MouseEvent e) {
				drawerMouseListener.mouseDragged(createEvent(e));

			}
		});

		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				drawerMouseListener.mouseUp(createEvent(e));
			}

			@Override
			public void mousePressed(MouseEvent e) {
				drawerMouseListener.mouseDown(createEvent(e));
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {

			}
		});
	}

	private DrawerMouseEvent createEvent(MouseEvent e) {
		DrawerMouseEvent event = new DrawerMouseEvent(e.getX(), e.getY());
		return event;
	}

	@Override
	public Object getWidget() {
		return this;
	}
}
