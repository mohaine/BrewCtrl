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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mohaine.brewcontroller.shared.util.StringUtils;

public abstract class JsonObjectHandler<T> {

	private Map<String, JsonObjectPropertyHandler<T, ?>> fieldHandlers = new HashMap<String, JsonObjectPropertyHandler<T, ?>>();

	{
		List<JsonObjectPropertyHandler<T, ?>> propertyHandlers = getPropertyHandlers();
		if (propertyHandlers != null) {
			for (JsonObjectPropertyHandler<T, ?> jsonObjectPropertyHandler : propertyHandlers) {
				fieldHandlers.put(jsonObjectPropertyHandler.getName(), jsonObjectPropertyHandler);
			}
		}
	}

	public abstract String getType();

	public abstract boolean handlesType(Class<?> value);

	public abstract List<JsonObjectPropertyHandler<T, ?>> getPropertyHandlers();

	public abstract T createNewObject() throws Exception;

	protected String encodeBoolean(Boolean bool) {
		if (bool != null && bool.booleanValue()) {
			return "true";
		}
		return "false";
	}

	protected Boolean decodeBoolean(String str) {
		return "true".equals(str) ? Boolean.TRUE : Boolean.FALSE;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processFromUnknown(T obj, JsonUnknownObject unknownObject, JsonConverterConfig config) {
		List<JsonObjectPropertyHandler<T, ?>> propertyHandlers = getPropertyHandlers();
		if (propertyHandlers != null) {
			for (JsonObjectPropertyHandler<T, ?> ph : propertyHandlers) {
				JsonObjectPropertyHandler<T, Object> phT = (JsonObjectPropertyHandler<T, Object>) ph;

				Object property = unknownObject.getProperty(phT.getName());
				if (property instanceof JsonUnknownObject) {
					if (phT.isJson()) {
						StringBuffer sb = new StringBuffer();
						new JsonEncoder(config).appendObject(sb, property);
						property = sb.toString();
					} else {
						Class<Object> expectedType = (Class<Object>) phT.getExpectedType();
						if (expectedType != null) {
							property = config.convertToObject((JsonUnknownObject) property, expectedType);
						}
					}
				} else if (property instanceof List) {
					Class<Object> expectedType = (Class<Object>) phT.getExpectedType();
					if (expectedType != null) {
						// if (!List.class.isAssignableFrom(expectedType)) {
						List list = (List) property;
						for (int i = 0; i < list.size(); i++) {
							Object listObj = list.get(i);
							if (listObj instanceof JsonUnknownObject) {
								listObj = config.convertToObject((JsonUnknownObject) listObj, expectedType);
								list.set(i, listObj);
							}
						}
					}
				}
				phT.setValue(obj, property);
			}
		}
	}

	public void processParameters(T value, JsonEncoder jsonEncoder, StringBuffer sb, boolean first) {
		List<JsonObjectPropertyHandler<T, ?>> propertyHandlers = getPropertyHandlers();
		if (propertyHandlers != null) {
			for (JsonObjectPropertyHandler<T, ?> ph : propertyHandlers) {
				@SuppressWarnings("unchecked")
				JsonObjectPropertyHandler<T, Object> phT = (JsonObjectPropertyHandler<T, Object>) ph;
				Object fieldValue = phT.getValue(value);

				if (!first) {
					sb.append(",");
				}
				first = false;
				if (phT.isJson() && fieldValue instanceof String) {

					String fv = (String) fieldValue;

					if (!StringUtils.hasLength(fv)) {
						fv = "null";
					}
					jsonEncoder.appendNamedJsonValue(sb, phT.getName(), fv);
				} else {
					jsonEncoder.appendNamedValue(sb, phT.getName(), fieldValue);
				}
			}
		}
	}

}
