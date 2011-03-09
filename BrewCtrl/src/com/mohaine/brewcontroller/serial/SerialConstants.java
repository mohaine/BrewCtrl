/*
    Copyright 2009-2011 Michael Graessle

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

package com.mohaine.brewcontroller.serial;

public class SerialConstants {
	public static final byte DATA_START = 0x1;
	public static final byte STATUS_CONTROL = 0x11;
	public static final byte SENSOR_CONTROL = 0x12;
	public static final byte HARDWARE_CONTROL = 0x13;
	public static final byte TRUE = 0x1;
	public static final byte FALSE = 0x0;
	public static final byte DATA_END = '\r';
}
