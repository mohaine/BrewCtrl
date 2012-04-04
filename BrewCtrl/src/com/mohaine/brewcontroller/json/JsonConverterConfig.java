package com.mohaine.brewcontroller.json;

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
			if (handler.getObjectType().isAssignableFrom(class1)) {
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

	@SuppressWarnings({ "unchecked" })
	public <T> T convertToObject(JsonUnknownObject unknownObject, JsonObjectHandler<T> handler) {
		try {
			Class<?> objectType = handler.getObjectType();
			Object obj = objectType.newInstance();
			handler.processFromUnknown((T) obj, unknownObject, this);
			return (T) obj;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
