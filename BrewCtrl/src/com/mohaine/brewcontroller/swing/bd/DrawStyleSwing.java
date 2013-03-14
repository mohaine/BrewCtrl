package com.mohaine.brewcontroller.swing.bd;

import java.awt.Color;
import java.awt.Font;

import com.mohaine.brewcontroller.client.display.DrawStyle.BColor;
import com.mohaine.brewcontroller.client.display.DrawStyle.BFont;

public class DrawStyleSwing {

	public static final Color PUMP_ON = Color.green;
	public static final Color PUMP_OFF = Color.red;
	public static final Color TANK = Color.white;
	public static final Color TANK_INSIDE = Color.lightGray;
	public static Color BACKGROUND = Color.lightGray;
	public static Color FOREGROUND = Color.black;
	public static final Color INACTIVE = Color.gray;
	public static final Color PENDING = Color.pink;
	public static Color ERROR = Color.red;

	public static final Font TEMP_FONT = new Font("Dialog", Font.PLAIN, 32);
	public static final Font TEXT_FONT = new Font("Dialog", Font.PLAIN, 12);
	public static final Font TEMP_TARGET_FONT = new Font("Dialog", Font.PLAIN, 20);

	public static Color mapColor(BColor c) {
		switch (c) {
		case BACKGROUND:
			return BACKGROUND;
		case FOREGROUND:
			return FOREGROUND;
		case INACTIVE:
			return INACTIVE;
		case ERROR:
			return ERROR;
		case PENDING:
			return PENDING;
		case PUMP_OFF:
			return PUMP_OFF;
		case PUMP_ON:
			return PUMP_ON;
		case TANK:
			return TANK;
		case TANK_INSIDE:
			return TANK_INSIDE;
		}

		return ERROR;
	}

	public static Font mapFont(BFont f) {
		switch (f) {
		case TEMP_FONT:
			return TEMP_FONT;
		case TEXT_FONT:
			return TEXT_FONT;
		case TEMP_TARGET_FONT:
			return TEMP_TARGET_FONT;
		}
		return TEMP_TARGET_FONT;
	}
}
