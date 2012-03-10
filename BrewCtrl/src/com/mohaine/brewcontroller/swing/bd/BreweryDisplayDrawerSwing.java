package com.mohaine.brewcontroller.swing.bd;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.Converter;
import com.mohaine.brewcontroller.UnitConversion;
import com.mohaine.brewcontroller.bd.BreweryComponentDisplay;
import com.mohaine.brewcontroller.bd.BreweryDisplay.BreweryDisplayDrawer;
import com.mohaine.brewcontroller.layout.BreweryComponent;
import com.mohaine.brewcontroller.layout.HeatElement;
import com.mohaine.brewcontroller.layout.Pump;
import com.mohaine.brewcontroller.layout.Sensor;
import com.mohaine.brewcontroller.layout.Tank;

public class BreweryDisplayDrawerSwing extends Canvas implements BreweryDisplayDrawer {
	private static final int TANK_TOP_HEIGHT = 20;
	private static final long serialVersionUID = 1L;
	private List<BreweryComponentDisplay> displays;
	private int PADDING = 5;
	private NumberFormat nf = new DecimalFormat("0.0");
	private UnitConversion conversion;

	@Inject
	public BreweryDisplayDrawerSwing(final UnitConversion conversion) {
		super();
		this.conversion = conversion;
	}

	@Override
	public void redrawBreweryComponent(BreweryComponent componentChanged) {
		Graphics2D g2 = (Graphics2D) getGraphics();
		for (BreweryComponentDisplay display : displays) {
			BreweryComponent displayComponent = display.getComponent();
			if (displayComponent == componentChanged) {
				drawComponent(g2, display);
			}
			if (displayComponent instanceof Tank) {
				Tank tank = (Tank) displayComponent;
				if (tank.getSensor() == componentChanged) {
					drawTankTemp(g2, display);
				} else if (tank.getHeater() == componentChanged) {
					drawHeatElement(g2, display);
				}
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
			drawComponent(g2, display);
		}

	}

	private void drawComponent(Graphics2D g2, BreweryComponentDisplay display) {
		BreweryComponent component = display.getComponent();
		if (Tank.TYPE.equals(component.getType())) {
			drawTank(g2, display);
		} else if (Pump.TYPE.equals(component.getType())) {
			drawPump(g2, display);
		} else {
			g2.drawRect(display.getLeft(), display.getTop(), display.getWidth(), display.getHeight());
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

		drawTankTemp(g, display);
		drawHeatElement(g, display);

		// if(display)

	}

	private void drawHeatElement(Graphics2D g, BreweryComponentDisplay display) {
		Tank component = (Tank) display.getComponent();
		HeatElement heater = component.getHeater();
		if (heater != null) {
			int duty = heater.getDuty();
			String text = Integer.toString(duty) + "%";

			int top = display.getTop();
			int left = display.getLeft() + display.getWidth() - 80;
			Rectangle rec = ((TankData) display.getDisplayInfo()).dutyRec;
			drawText(rec, g, top, left, display, text, Colors.FOREGROUND);
		}
	}

	private void drawTankTemp(Graphics2D g, BreweryComponentDisplay display) {
		Tank component = (Tank) display.getComponent();
		Sensor sensor = component.getSensor();
		if (sensor != null) {
			Double tempatureC = sensor.getTempatureC();
			if (tempatureC != null) {

				int top = display.getTop();
				int left = display.getLeft();

				final Converter<Double, Double> tempDisplayConveter = conversion.getTempDisplayConveter();
				String tempDisplay = nf.format(tempDisplayConveter.convertFrom(tempatureC)) + "\u00B0";
				Color textColor;
				if (sensor.isReading()) {
					textColor = Colors.FOREGROUND;
				} else {
					textColor = Colors.ERROR;
				}

				Rectangle rec = ((TankData) display.getDisplayInfo()).tempRec;

				drawText(rec, g, top, left, display, tempDisplay, textColor);
			}
		}
	}

	private void drawText(Rectangle rect, Graphics2D g, int top, int left, BreweryComponentDisplay display, String tempDisplay, Color textColor) {

		Graphics2D drawG;
		Image image = null;
		if (rect.width > 0) {
			image = createImage(rect.width, rect.height);
			drawG = (Graphics2D) image.getGraphics();
			drawG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			drawG.setColor(Colors.TANK);
			drawG.fillRect(0, 0, rect.width, rect.height);
		} else {
			drawG = g;
		}

		int oldX = rect.x;
		int oldY = rect.y;
		int oldHeight = rect.height;

		drawG.setFont(Colors.TEMP_FONT);
		drawG.setColor(textColor);

		FontMetrics fontMetrics = drawG.getFontMetrics();
		Rectangle2D stringBounds = fontMetrics.getStringBounds(tempDisplay, drawG);
		rect.x = left + 5;
		rect.y = (int) (top + stringBounds.getHeight() + TANK_TOP_HEIGHT);
		rect.width = (int) stringBounds.getWidth();
		rect.height = (int) -stringBounds.getMinY();

		if (image != null) {
			int drawX = rect.x - oldX;
			int drawY = rect.y - oldY + rect.height;
			drawG.drawString(tempDisplay, drawX, drawY);

			g.drawImage(image, oldX, oldY - oldHeight, null);
			drawG.dispose();
		} else {
			drawG.drawString(tempDisplay, rect.x, rect.y);
		}

	}

	private void drawPump(Graphics2D g, BreweryComponentDisplay display) {

		int top = display.getTop();
		int left = display.getLeft();
		int width = display.getWidth();
		int height = display.getHeight();

		Pump pump = (Pump) display.getComponent();
		boolean on = pump.isOn();

		Color backPaint = (on ? Colors.PUMP_ON : Colors.PUMP_OFF);
		Color strokePaint = Colors.FOREGROUND;

		int cirSize = (int) (Math.min(width, height) * 0.8f);

		int cirRadius = cirSize / 2;

		Shape square = new Rectangle2D.Double(left + cirRadius, top, width - cirRadius, cirRadius * 0.67);
		g.setColor(backPaint);
		g.fill(square);
		g.setColor(strokePaint);
		g.draw(square);

		Shape circle = new Ellipse2D.Float(left, top, cirSize, cirSize);
		g.setColor(backPaint);
		g.fill(circle);
		g.setColor(strokePaint);
		g.draw(circle);

		if (!on) {
			int subSize = (int) (cirRadius * 0.7f);
			Shape circle2 = new Ellipse2D.Float(left + cirRadius - subSize / 2, top + cirRadius - subSize / 2, subSize, subSize);
			g.fill(circle2);
			// c.drawCircle(cirCenterX, cirMidY, cirSize / 2, strokePaint);
		}
		drawName(g, display);
	}

	private void drawName(Graphics2D g, BreweryComponentDisplay display) {
		g.setFont(Colors.TEXT_FONT);
		g.setColor(Colors.FOREGROUND);
		int left = display.getLeft();
		int width = display.getWidth();
		FontMetrics fontMetrics = g.getFontMetrics();
		String name = display.getComponent().getName();
		Rectangle2D stringBounds = fontMetrics.getStringBounds(name, g);
		g.drawString(name, (int) (left + (width / 2) - stringBounds.getCenterX()), (int) (display.getTop() + display.getHeight() - 3));
	}

	@Override
	public void setDisplays(List<BreweryComponentDisplay> displays) {
		invalidate();
		this.displays = displays;

		for (BreweryComponentDisplay display : displays) {
			if (display.getDisplayInfo() == null) {
				if (Tank.TYPE.equals(display.getComponent().getType())) {
					display.setDisplayInfo(new TankData());
				}
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();

		int maxX = 0;
		int maxY = 0;
		for (BreweryComponentDisplay display : displays) {
			maxX = Math.max(maxX, display.getLeft() + display.getWidth());
			maxY = Math.max(maxY, display.getTop() + display.getHeight());
		}
		preferredSize.setSize(maxX + PADDING, maxY + PADDING);

		return preferredSize;
	}

	private static class TankData {

		public Rectangle dutyRec = new Rectangle(0, 0, 0, 0);
		public Rectangle tempRec = new Rectangle(0, 0, 0, 0);

	}

}
