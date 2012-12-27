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
package com.mohaine.brewcontroller.shared.json;

import java.util.ArrayList;
import java.util.Collection;

public class JsonConverterConfig {

	private ArrayList<JsonObjectHandler<?>> objectHandlers = null;

	private boolean addTypes = true;

	public ArrayList<JsonObjectHandler<?>> getObjectHandlers() {
		return objectHandlers;
	}

	public void setObjectHandlers(ArrayList<JsonObjectHandler<?>> objectHandlers) {
		this.objectHandlers = objectHandlers;
	}

	public boolean isAddTypes() {
		return addTypes;
	}

	public void setAddTypes(boolean addTypes) {
		this.addTypes = addTypes;
	}

	public void addHandler(JsonObjectHandler<?> jsonObjectHandler) {
		if (objectHandlers == null) {
			objectHandlers = new ArrayList<JsonObjectHandler<?>>();
		}
		objectHandlers.add(jsonObjectHandler);
	}

	public void addHandlers(Collection<JsonObjectHandler<?>> jsonObjectHandlers) {
		if (objectHandlers == null) {
			objectHandlers = new ArrayList<JsonObjectHandler<?>>();
		}
		objectHandlers.addAll(jsonObjectHandlers);
	}

	@SuppressWarnings("unchecked")
	public <T> T convertToObject(JsonUnknownObject unknownObject, Class<? extends T> class1) {
		for (JsonObjectHandler<?> handler : objectHandlers) {
			if (handler.handlesType(class1)) {
				return (T) convertToObject(unknownObject, handler);
			}
		}
		return null;
	}

	public Object convertToObject(JsonUnknownObject unknownObject) {
		if (objectHandlers != null) {
			Object property = unknownObject.getProperty(JsonObjectConverter.TYPE);
			if (property != null && property instanceof String) {
				String typeString = (String) property;
				for (JsonObjectHandler<?> handler : objectHandlers) {
					if (typeString.equals(handler.getType())) {
						return convertToObject(unknownObject, handler);
					}
				}
			}
		}
		return unknownObject;
	}

	public <T> T convertToObject(JsonUnknownObject unknownObject, JsonObjectHandler<T> handler) {
		try {
			T obj = handler.createNewObject();
			handler.processFromUnknown(obj, unknownObject, this);
			return obj;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
