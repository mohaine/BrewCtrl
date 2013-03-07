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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonObjectConverter {
	public static final String TYPE = "__type__";

	private JsonConverterConfig config = new JsonConverterConfig();

	public JsonObjectConverter() {
		this(true);
	}

	public JsonObjectConverter(boolean addTypes) {
		super();
		config.setAddTypes(addTypes);
	}

	@SuppressWarnings("unchecked")
	public <T> T decode(String jsonString) {
		return (T) new JsonDecoder(config, jsonString).parseJson();
	}

	public String encode(Object obj) {
		JsonEncoder jc = new JsonEncoder(config);
		StringBuffer sb = new StringBuffer();
		jc.appendObject(sb, obj);
		return sb.toString();
	}

	public void addHandler(JsonObjectHandler<?> jsonObjectHandler) {
		config.addHandler(jsonObjectHandler);
	}

	public void addHandlers(Collection<JsonObjectHandler<?>> jsonObjectHandlers) {
		config.addHandlers(jsonObjectHandlers);
	}

	@SuppressWarnings("unchecked")
	public <T> T decode(String json, Class<? extends T> class1) {
		JsonDecoder jsonDecoder = new JsonDecoder(config, json);
		Object parseJson = jsonDecoder.parseJson();
		if (parseJson instanceof JsonUnknownObject) {
			parseJson = config.convertToObject((JsonUnknownObject) parseJson, class1);
		}
		return (T) parseJson;
	}

	public <T> List<T> decodeList(String json, Class<? extends T> class1) {
		JsonDecoder jsonDecoder = new JsonDecoder(config, json);
		Object parseJson = jsonDecoder.parseJson();
		if (parseJson instanceof List) {
			List<T> results = new ArrayList<T>();
			@SuppressWarnings("unchecked")
			List<Object> parseList = (List<Object>) parseJson;
			for (Object object : parseList) {
				if (object instanceof JsonUnknownObject) {
					results.add(config.convertToObject((JsonUnknownObject) object, class1));
				}
			}
			return results;

		}

		return null;
	}
}
