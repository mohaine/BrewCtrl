package com.mohaine.brewcontroller.web.client;

import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
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
		
		System.out.println("BreweryDisplayDrawerGwt.redrawAll()");
		backBufferContext.setFillStyle(Colors.FOREGROUND);
		backBufferContext.fillRect(0, 0, 20, 20);
		
		
		for (BreweryComponentDisplay display : displays) {
			drawComponent(backBufferContext, display, true);
		}

		context.drawImage(backBufferContext.getCanvas(), 0, 0);

	}

	private void drawComponent(Context2d context2d,
			BreweryComponentDisplay display, boolean full) {
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

	private void drawHeatElement(Context2d context2d,
			BreweryComponentDisplay display) {
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

			g.fillText(name,
					(int) (left + (width / 2) - measureText.getWidth() / 2),
					(int) (display.getTop() + display.getHeight() - 3));
		}
	}

	private void drawPump(Context2d context2d, BreweryComponentDisplay display) {
		// TODO Auto-generated method stub

	}

	private void drawTank(Context2d context2d, BreweryComponentDisplay display) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getWidget() {
		return panel;
	}

}
