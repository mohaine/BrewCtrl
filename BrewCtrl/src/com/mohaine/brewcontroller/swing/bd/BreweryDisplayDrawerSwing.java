package com.mohaine.brewcontroller.swing.bd;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
		
//		if(display)
		

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

				Rectangle lastTextRect = (Rectangle) display.getDisplayInfo();
				if (lastTextRect != null) {
					g.setColor(Colors.TANK);
					g.fillRect(lastTextRect.x, lastTextRect.y - lastTextRect.height, lastTextRect.width, lastTextRect.height);
				}

				if (sensor.isReading()) {
					g.setColor(Colors.FOREGROUND);
				} else {
					g.setColor(Colors.ERROR);
				}
				g.setFont(Colors.TEMP_FONT);
				FontMetrics fontMetrics = g.getFontMetrics();
				Rectangle2D stringBounds = fontMetrics.getStringBounds(tempDisplay, g);
				Rectangle textRect = new Rectangle(left + 5, (int) (top + stringBounds.getHeight() + TANK_TOP_HEIGHT), (int) stringBounds.getWidth(), (int) -stringBounds.getMinY());
				g.drawString(tempDisplay, textRect.x, textRect.y);
				display.setDisplayInfo(textRect);
			}
		}
	}

	private void drawPump(Graphics2D g, BreweryComponentDisplay display) {

		int top = display.getTop();
		int left = display.getLeft();
		int width = display.getWidth();
		int height = display.getHeight();

		boolean on = false;

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

}
