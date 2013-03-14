package com.mohaine.brewcontroller.swing.bd;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
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

import javax.swing.SwingUtilities;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.display.BreweryDisplayDrawer.DrawerCanvas;
import com.mohaine.brewcontroller.client.display.BreweryDisplayDrawer.RedrawHook;
import com.mohaine.brewcontroller.client.display.DrawStyle.BColor;
import com.mohaine.brewcontroller.client.display.DrawStyle.BFont;
import com.mohaine.brewcontroller.client.display.DrawerMouseListener;
import com.mohaine.brewcontroller.client.display.DrawerMouseListener.DrawerMouseEvent;
import com.mohaine.brewcontroller.swing.bd.BreweryDisplayDrawerSwing.GraphicContext;

public class BreweryDisplayDrawerSwing extends Canvas implements DrawerCanvas<GraphicContext> {
	private static final int TANK_TOP_HEIGHT = 20;
	private static final long serialVersionUID = 1L;
	private RedrawHook<GraphicContext> hook;

	private int width;
	private int height;
	private GraphicContext offscreenGc;

	@Inject
	public BreweryDisplayDrawerSwing() {
		super();
	}

	@Override
	public void paint(Graphics g) {
		// super.paint(g);
		if (hook != null) {
			GraphicContext gc = getContext();
			if (gc != null) {
				hook.redraw(gc);
				displayContext(gc);
			}
		}
	}

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

	@Override
	public void init(RedrawHook<GraphicContext> hook) {
		this.hook = hook;
	}

	@Override
	public void updateWidths(int width, int height) {
		this.width = width;
		this.height = height;

		if (SwingUtilities.isEventDispatchThread()) {
			updateWidthsSwing(width, height);
		} else {
			try {
				updateWidthsSwing(width, height);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void updateWidthsSwing(int width, int height) {
		setPreferredSize(new Dimension(width, height));
		this.invalidate();

		if (this.offscreenGc != null) {
			GraphicContext offscreenGc2 = this.offscreenGc;
			this.offscreenGc = null;
			if (offscreenGc2 != null) {
				offscreenGc2.g.dispose();
			}
		}
	}

	private static class ContextHolder {
		protected GraphicContext context;
	}

	@Override
	public GraphicContext getContext() {
		if (this.offscreenGc != null) {
			return this.offscreenGc;
		}

		synchronized (this) {
			if (this.offscreenGc == null) {
				GraphicContext createGc = createGc();
				this.offscreenGc = createGc;
			}
		}
		return this.offscreenGc;
	}

	private GraphicContext createGc() {
		if (SwingUtilities.isEventDispatchThread()) {
			return getContextSwing();
		} else {
			try {
				final ContextHolder ch = new ContextHolder();
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						ch.context = getContextSwing();
					}
				});
				return ch.context;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class GraphicContext {
		private Image image;
		private Graphics2D g;
	}

	private GraphicContext getContextSwing() {
		GraphicContext gc = new GraphicContext();
		gc.image = createImage(width, height);
		if (gc.image == null) {
			return null;
		}
		gc.g = (Graphics2D) gc.image.getGraphics();
		gc.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		return gc;
	}

	@Override
	public void displayContext(GraphicContext gc) {
		Graphics g = getGraphics();
		g.drawImage(gc.image, 0, 0, null);
	}

	@Override
	public void fillRect(final GraphicContext g, final int left, final int top, final int width, final int height, final BColor background) {

		if (SwingUtilities.isEventDispatchThread()) {
			fillRectSwing(g, left, top, width, height, background);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						fillRectSwing(g, left, top, width, height, background);
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void fillRectSwing(GraphicContext gc, int left, int top, int width, int height, BColor background) {
		gc.g.setColor(Colors.mapColor(background));
		gc.g.fillRect(left, top, width, height);
	}

	@Override
	public void drawText(final GraphicContext g, final String text, final BColor textColor, final BColor bgColor, final boolean alignRight, final int left, final int top, final int width,
			final int height, final BFont font) {
		if (SwingUtilities.isEventDispatchThread()) {
			drawTextSwing(g, text, textColor, bgColor, alignRight, left, top, width, height, font);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						drawTextSwing(g, text, textColor, bgColor, alignRight, left, top, width, height, font);
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void drawTextSwing(GraphicContext gc, String text, BColor textColor, BColor bgColor, boolean alignRight, int left, int top, int width, int height, BFont font) {

		gc.g.setColor(Colors.mapColor(bgColor));

		gc.g.fillRect(left, top, width, height);

		gc.g.setColor(Colors.mapColor(textColor));
		gc.g.setFont(Colors.mapFont(font));

		FontMetrics fontMetrics = gc.g.getFontMetrics();
		Rectangle2D stringBounds = fontMetrics.getStringBounds(text, gc.g);

		int x = alignRight ? (int) (width - stringBounds.getWidth()) : 0;
		int y = (int) (-stringBounds.getCenterY()) + height / 2;

		gc.g.drawString(text, left + x, top + y);

	}

	@Override
	public void drawPump(final GraphicContext gc, final int left, final int top, final int width, final int height, final BColor backPaint, final boolean on) {
		if (SwingUtilities.isEventDispatchThread()) {
			drawPumpSwing(gc, left, top, width, height, backPaint, on);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						drawPumpSwing(gc, left, top, width, height, backPaint, on);
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void drawPumpSwing(GraphicContext gc, int left, int top, int width, int height, BColor backPaint, boolean on) {

		Color strokePaint = Colors.mapColor(BColor.FOREGROUND);

		int cirSize = (int) (Math.min(width, height) - 1);

		int cirRadius = cirSize / 2;
		Shape square = new Rectangle2D.Double(cirRadius + left, top, width - cirRadius - 1, cirRadius * 0.67);
		gc.g.setColor(Colors.mapColor(backPaint));
		gc.g.fill(square);
		gc.g.setColor(strokePaint);
		gc.g.draw(square);

		Shape circle = new Ellipse2D.Float(left, top, cirSize, cirSize);
		gc.g.setColor(Colors.mapColor(backPaint));
		gc.g.fill(circle);
		gc.g.setColor(strokePaint);
		gc.g.draw(circle);

		if (!on) {
			int subSize = (int) (cirRadius * 0.7f);
			Shape circle2 = new Ellipse2D.Float(cirRadius - subSize / 2 + left, cirRadius - subSize / 2 + top, subSize, subSize);
			gc.g.fill(circle2);
		}

	}

	@Override
	public void drawTank(final GraphicContext gc, final int left, final int top, final int width, final int height) {
		if (SwingUtilities.isEventDispatchThread()) {
			drawTankSwing(gc, left, top, width, height);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						drawTankSwing(gc, left, top, width, height);
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void drawTankSwing(GraphicContext gc, int left, int top, int width, int height) {
		int boxHeight = height - TANK_TOP_HEIGHT;

		Color tankColor = Colors.TANK;
		Color strokePaint = Colors.FOREGROUND;
		// bottom arc
		Shape circleBottom = new Ellipse2D.Float(left, top + boxHeight, width, TANK_TOP_HEIGHT);
		gc.g.setColor(tankColor);
		gc.g.fill(circleBottom);
		gc.g.setColor(strokePaint);
		gc.g.draw(circleBottom);

		// Center
		int boxTopLeft = top + TANK_TOP_HEIGHT / 2;
		Shape square = new Rectangle2D.Double(left, boxTopLeft, width, boxHeight);
		gc.g.setColor(tankColor);
		gc.g.fill(square);
		gc.g.setColor(strokePaint);
		gc.g.drawLine(left, boxTopLeft, left, boxTopLeft + boxHeight);
		gc.g.drawLine(left + width, boxTopLeft, left + width, boxTopLeft + boxHeight);

		// Inside/Top of tank
		Shape circleTop = new Ellipse2D.Float(left, top, width, TANK_TOP_HEIGHT);
		gc.g.setColor(Colors.TANK_INSIDE);
		gc.g.fill(circleTop);
		gc.g.setColor(strokePaint);
		gc.g.draw(circleTop);
	}
}
