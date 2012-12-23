/*
    Copyright 2009-2012 Michael Graessle

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

package com.mohaine.brewcontroller.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JsonUnknownObject {

	private Map<String, Object> properties = new HashMap<String, Object>();

	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getProperty(String name) {
		return (T) properties.get(name);
	}

	public Set<String> getPropertyNames() {
		return properties.keySet();
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

}
