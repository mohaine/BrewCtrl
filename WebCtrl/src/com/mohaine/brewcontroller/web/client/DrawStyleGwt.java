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

import com.google.gwt.canvas.dom.client.CssColor;
import com.mohaine.brewcontroller.client.display.DrawStyle.BColor;
import com.mohaine.brewcontroller.client.display.DrawStyle.BFont;

public class DrawStyleGwt {

	public static final CssColor PUMP_ON = CssColor.make("green");
	public static final CssColor PUMP_OFF = CssColor.make("red");
	public static final CssColor TANK = CssColor.make("white");
	public static final CssColor TANK_INSIDE = CssColor.make("darkGray");
	public static CssColor BACKGROUND = CssColor.make("lightGray");
	public static CssColor FOREGROUND = CssColor.make("black");
	public static final CssColor INACTIVE = CssColor.make("gray");
	public static final CssColor PENDING = CssColor.make("pink");
	public static CssColor ERROR = CssColor.make("red");

	public static final String TEMP_FONT = "48px sans-serif";
	public static final String TEXT_FONT = "12px sans-serif";
	public static final String TEMP_TARGET_FONT = "20px sans-serif";

	public static CssColor mapColor(BColor c) {
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
	public static String mapFont(BFont f) {
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
