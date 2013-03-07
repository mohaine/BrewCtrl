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
package com.mohaine.brewcontroller.shared.json;

public abstract class JsonObjectPropertyHandler<T, F> {
	public abstract String getName();

	public abstract F getValue(T object);

	public abstract void setValue(T object, F value);

	public Class<?> getExpectedType() {
		return null;
	}

	public boolean isJson() {
		return false;
	}

}
