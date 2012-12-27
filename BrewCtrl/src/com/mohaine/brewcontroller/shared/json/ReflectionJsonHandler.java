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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ReflectionJsonHandler {

	private static final class ReflectionJsonPropertyHandler<T, F> extends JsonObjectPropertyHandler<T, F> {
		private Field field;

		public ReflectionJsonPropertyHandler(Field field) {
			this.field = field;
			field.setAccessible(true);
		}

		@Override
		public String getName() {
			return field.getName();
		}

		@Override
		public Class<F> getExpectedType() {
			ListType annotation = field.getAnnotation(ListType.class);
			if (annotation != null) {
				Class<?>[] value = annotation.value();
				if (value != null && value.length == 1) {
					return (Class<F>) value[0];
				}
			}
			return (Class<F>) field.getType();
		}

		@Override
		public boolean isJson() {
			return field.isAnnotationPresent(JsonString.class);
		}

		@Override
		public F getValue(T object) {
			try {
				Object value = field.get(object);
				if (value instanceof Enum) {
					return (F) value.toString();
				}
				return (F) value;
			} catch (Exception e) {
				throw new RuntimeException("Failed to get value " + object + " on " + field.getDeclaringClass().getName() + "." + field.getName(), e);
			}
		}

		@Override
		public void setValue(T object, F value) {
			try {

				if (value != null) {
					if (field.getType().isEnum()) {
						Object[] enums = field.getType().getEnumConstants();
						for (Object enumValue : enums) {
							if (enumValue.toString().equals(value)) {
								value = (F) enumValue;
								break;
							}
						}
					}

					if (value instanceof JsonUnknownObject) {
						if (Map.class.isAssignableFrom(field.getType())) {
							JsonUnknownObject juo = (JsonUnknownObject) value;
							value = (F) juo.getProperties();
						}
					}
				} else {
					if (Boolean.TYPE.equals(field.getType())) {
						value = (F) Boolean.FALSE;
					} else if (Integer.TYPE.equals(field.getType())) {
						value = (F) new Integer(0);
					} else if (Long.TYPE.equals(field.getType())) {
						value = (F) new Long(0);
					} else if (Double.TYPE.equals(field.getType())) {
						value = (F) new Double(0);
					} else if (Float.TYPE.equals(field.getType())) {
						value = (F) new Float(0);
					} else if (Byte.TYPE.equals(field.getType())) {
						value = (F) new Byte((byte) 0);
					}
				}

				field.set(object, value);
			} catch (Exception e) {
				throw new RuntimeException("Failed to set value " + value + " on " + field.getDeclaringClass().getName() + "." + field.getName(), e);
			}

		}
	}

	private static final class ReflectionJsonObjectHandler<T> extends JsonObjectHandler<T> {
		private Class<T> classToBuild;
		ArrayList<JsonObjectPropertyHandler<T, ?>> phs = new ArrayList<JsonObjectPropertyHandler<T, ?>>();

		public ReflectionJsonObjectHandler(Class<T> classToBuild) {
			this.classToBuild = classToBuild;
		}

		@Override
		public String getType() {
			return classToBuild.getSimpleName();
		}

		@Override
		public boolean handlesType(Class<?> value) {
			return classToBuild.isAssignableFrom(value);
		}

		@Override
		public T createNewObject() throws Exception {
			return classToBuild.newInstance();
		}

		@Override
		public List<JsonObjectPropertyHandler<T, ?>> getPropertyHandlers() {
			return phs;
		}

	}

	public static <T> JsonObjectHandler<T> build(final Class<T> classToBuild) throws Exception {
		ReflectionJsonObjectHandler<T> joh = new ReflectionJsonObjectHandler<T>(classToBuild);
		addFields(joh, classToBuild);
		return joh;
	}

	private static <T> void addFields(ReflectionJsonObjectHandler<T> joh, Class<?> objClass) throws Exception {
		Class<?> superClass = objClass.getSuperclass();

		if (superClass != null) {
			addFields(joh, superClass);
		}

		Field[] fields = objClass.getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			if (Modifier.isTransient(field.getModifiers())) {
				continue;
			}
			joh.phs.add(new ReflectionJsonPropertyHandler<T, Object>(field));
		}
	}
}
