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

package com.mohaine.brewcontroller.client;


public class TempFConverter implements Converter<Double, Double> {

	public static double convertC2F(double tempC) {
		return (9.0 / 5.0) * tempC + 32;
	}

	public static double convertF2C(double tempF) {
		return (5.0 / 9.0) * (tempF - 32);
	}

	@Override
	public Double convertFrom(Double value) {
		return convertC2F(value);
	}

	@Override
	public Double convertTo(Double value) {
		return convertF2C(value);
	}
}