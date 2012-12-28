package com.mohaine.brewcontroller.web.client;

import com.mohaine.brewcontroller.client.net.BrewJsonConverter;
import com.mohaine.brewcontroller.shared.json.JsonObjectConverter;
import com.mohaine.brewcontroller.shared.json.JsonUnknownObject;

public class BrewJsonConverterGwt implements BrewJsonConverter {

	protected JsonObjectConverter jc = new JsonObjectConverter(false);

	public BrewJsonConverterGwt() {
		super();

	}

	public JsonObjectConverter getJsonConverter() throws Exception {
		return jc;
	}

	@SuppressWarnings("unchecked")
	protected <F> F convertValue(Object value, Class<F> fieldType) {
		if (value != null) {
			if (fieldType.isEnum()) {
				Object[] enums = fieldType.getEnumConstants();
				for (Object enumValue : enums) {
					if (enumValue.toString().equals(value)) {
						value = (F) enumValue;
						break;
					}
				}
			}

			if (value instanceof JsonUnknownObject) {

				System.out.println("fieldType: " + fieldType.getName());
				// if (Map.class.isAssignableFrom(fieldType)) {
				// JsonUnknownObject juo = (JsonUnknownObject) value;
				// value = (F) juo.getProperties();
				// }
			}
		} else {
			if (Boolean.TYPE.equals(fieldType)) {
				value = (F) Boolean.FALSE;
			} else if (Integer.TYPE.equals(fieldType)) {
				value = (F) new Integer(0);
			} else if (Long.TYPE.equals(fieldType)) {
				value = (F) new Long(0);
			} else if (Double.TYPE.equals(fieldType)) {
				value = (F) new Double(0);
			} else if (Float.TYPE.equals(fieldType)) {
				value = (F) new Float(0);
			} else if (Byte.TYPE.equals(fieldType)) {
				value = (F) new Byte((byte) 0);
			}
		}
		return (F) value;
	}

}
