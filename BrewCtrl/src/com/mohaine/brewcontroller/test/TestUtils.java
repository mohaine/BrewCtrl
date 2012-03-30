package com.mohaine.brewcontroller.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class TestUtils {

	public static String displayFields(Object object) throws Exception {
		return displayFields(object, true);
	}

	private static String displayFields(Object object, boolean newLineFields) throws Exception {
		StringBuffer sb = new StringBuffer();
		appendValue(sb, newLineFields, object);
		return sb.toString();
	}

	private static void displayCustomObjectFields(StringBuffer sb, Object object, boolean newLineFields) throws Exception {

		if (object != null) {
			sb.append(object.getClass().getName());
			sb.append("{");
			sb.append("\r\n");

			List<Field> fields = new ArrayList<Field>();

			Class<?> oClass = object.getClass();

			while (!(oClass.isAssignableFrom(Object.class))) {

				Field[] declaredFields = oClass.getDeclaredFields();
				for (int i = 0; i < declaredFields.length; i++) {
					Field field = declaredFields[i];
					if (Modifier.isStatic(field.getModifiers())) {
						continue;
					}
					fields.add(field);
				}

				oClass = oClass.getSuperclass();
			}
			Field[] declaredFields = fields.toArray(new Field[fields.size()]);

			Arrays.sort(declaredFields, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					Field m1 = (Field) o1;
					Field m2 = (Field) o2;
					return String.CASE_INSENSITIVE_ORDER.compare(m1.getName(), m2.getName());
				}
			});

			int count = 0;

			for (int i = 0; i < declaredFields.length; i++) {
				Field field = declaredFields[i];
				if ("originalDigest".equals(field.getName())) {
					continue;
				}
				if (field.getName().equals("fieldValues")) {
					continue;
				}

				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				if (count++ > 0) {
					sb.append(", ");
					if (newLineFields) {
						sb.append("\r\n");
					}
				}

				sb.append(field.getName());
				sb.append(':');
				appendValue(sb, newLineFields, field.get(object));
			}
			if (newLineFields && count > 0) {
				sb.append("\r\n");
			}
			sb.append("}");
		} else {
			sb.append("null");
		}
	}

	@SuppressWarnings("rawtypes")
	private static void appendValue(StringBuffer sb, boolean newLineFields, Object value) throws Exception {
		if (value == null) {
			sb.append("null");
		} else if (value instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss:SS");
			sdf.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
			Date date = (Date) value;
			long l = Math.round((double) date.getTime() / 10) * 10;

			date = new Date(l);

			sb.append(sdf.format(date));
		} else if (value instanceof Collection<?>) {
			Collection<?> col = (Collection<?>) value;
			sb.append("[\r\n");
			for (Iterator<?> iterator = col.iterator(); iterator.hasNext();) {
				appendValue(sb, newLineFields, iterator.next());
				sb.append(",\r\n");
			}
			sb.append("]");
		} else if (value instanceof Map) {
			displayMap(newLineFields, sb, (Map) value);
		} else if (value instanceof Number) {
			String valueOf = valueOf(value);
			sb.append(valueOf);
		} else if (value instanceof byte[]) {
			byte[] byteArray = (byte[]) value;
			for (int index = 0; index < byteArray.length; index++) {
				byte b = byteArray[index];
				sb.append(Integer.toHexString(0xFF & b));
			}
		} else if (value.getClass().getName().startsWith("com.")) {
			displayCustomObjectFields(sb, value, newLineFields);
		} else {
			String valueOf = valueOf(value);
			if (valueOf.length() != 0) {
				sb.append('"');
				sb.append(valueOf);
				sb.append('"');
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private static void displayMap(boolean newLineFields, StringBuffer sb, Map map) throws Exception {
		sb.append("{\r\n");

		for (Object key : map.keySet()) {
			sb.append(displayFields(key, newLineFields));
			sb.append("=");
			sb.append(displayFields(map.get(key), newLineFields));
			sb.append(",\r\n");
		}
		sb.append("}");

	}

	private static String valueOf(Object string) {
		if (string == null) {
			return "";
		}
		return string.toString();
	}
}
