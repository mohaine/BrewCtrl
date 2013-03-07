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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JsonEncoder {
	private JsonConverterConfig config;

	public JsonEncoder() {

	}

	public JsonEncoder(JsonConverterConfig config) {
		super();
		this.config = config;
	}

	public void appendList(StringBuffer sb, Collection values) {
		sb.append("[");
		int count = 0;
		if (values != null) {
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				Object value = iter.next();
				int lengthBefore = sb.length();
				if (count > 0) {
					sb.append(',');
				}
				boolean appendObject = appendObject(sb, value);
				if (appendObject) {
					count++;
				} else {
					sb.setLength(lengthBefore);
				}
			}
		}
		sb.append("]");
	}

	private void appendArray(StringBuffer sb, Object[] values) {
		sb.append("[");
		int count = 0;
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				Object value = values[i];
				int lengthBefore = sb.length();
				if (count > 0) {
					sb.append(',');
				}
				boolean appendObject = appendObject(sb, value);
				if (appendObject) {
					count++;
				} else {
					sb.setLength(lengthBefore);
				}
			}
		}
		sb.append("]");
	}

	public void appendMap(StringBuffer sb, Map map) {
		sb.append("{");
		int count = 0;
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			if (count > 0) {
				sb.append(',');
			}
			appendString(sb, key);
			sb.append(':');
			if (!appendObject(sb, map.get(key))) {
				sb.append("null");
			}
			count++;

		}
		sb.append("}");
	}

	public void appendNamedValue(StringBuffer sb, String name, List values) {
		if (values == null) {
			sb.append('"');
			escapeStringForJson(sb, name);
			sb.append("\":null");
		} else {
			sb.append('"');
			escapeStringForJson(sb, name);
			sb.append("\":");
			appendList(sb, values);
		}

	}

	public void appendNamedValue(StringBuffer sb, String name, Map map) {
		if (map == null) {
			sb.append('"');
			escapeStringForJson(sb, name);
			sb.append("\":null");
		} else {
			sb.append('"');
			escapeStringForJson(sb, name);
			sb.append("\":");
			appendMap(sb, map);
		}

	}

	public void appendNamedValue(StringBuffer sb, String name, Object value) {
		appendString(sb, name);
		sb.append(':');
		if (!appendObject(sb, value)) {
			sb.append("null");
		}
	}

	public void appendNamedJsonValue(StringBuffer sb, String name, String value) {
		appendString(sb, name);
		sb.append(':');
		sb.append(value);
	}

	public boolean appendObject(StringBuffer sb, Object value) {
		if (value == null) {
			sb.append("null");
		} else {
			if (value instanceof String) {
				appendString(sb, (String) value);
			} else if (value instanceof Number) {
				sb.append(value.toString());
			} else if (value instanceof Boolean) {
				appendBoolean(sb, value);
			} else if (value instanceof Collection) {
				appendList(sb, (Collection) value);
			} else if (value instanceof Object[]) {
				appendArray(sb, (Object[]) value);
			} else if (value instanceof Map) {
				appendMap(sb, (Map) value);
			} else if (value instanceof Date) {
				appendDate(sb, (Date) value);
			} else {
				return toJson(sb, value);
			}
		}
		return true;
	}

	private void appendDate(StringBuffer sb, Date date) {

		if (date == null) {
			sb.append("null");
		}
		sb.append("\"\\/Date(");
		sb.append(date.getTime());
		sb.append(")\\/\"");

	}

	private void appendBoolean(StringBuffer sb, Object value) {
		if (((Boolean) value).booleanValue()) {
			sb.append("true");
		} else {
			sb.append("false");
		}
	}

	public boolean toJson(StringBuffer sb, Object value) {
		ArrayList<JsonObjectHandler<?>> objectHandlers = config.getObjectHandlers();
		if (objectHandlers != null) {
			for (JsonObjectHandler handler : objectHandlers) {
				if (handler.handlesType(value.getClass())) {
					sb.append('{');
					boolean first = true;
					if (config.isAddTypes()) {
						appendNamedValue(sb, JsonObjectConverter.TYPE, handler.getType());
						first = false;
					}
					handler.processParameters(value, this, sb, first);
					sb.append("}");
					return true;
				}
			}
		}

		if (value instanceof JsonUnknownObject) {
			JsonUnknownObject jso = (JsonUnknownObject) value;
			appendMap(sb, jso.getProperties());
			return true;
		}
		// if (value instanceof JsonEncodeable) {
		// return ((JsonEncodeable) value).appendJson(sb);
		// }
		return false;
	}

	public void appendNamedValue(StringBuffer sb, String name, boolean value) {
		appendString(sb, name);
		sb.append(':');
		appendBoolean(sb, value ? Boolean.TRUE : Boolean.FALSE);
	}

	// public void appendAnotherNamedValue(StringBuffer sb, String name,
	// Date value) {
	// sb.append(',');
	// appendNamedValue(sb, name, value);
	// }

	// public void appendNamedValue(StringBuffer sb, String name, Date
	// value) {
	// appendString(sb, name);
	// sb.append(':');
	// if (value == null) {
	// sb.append("null");
	// } else {
	// sb.append('"');
	// sb.append(DateUtils.getDateString(value));
	// sb.append('"');
	// }
	// }

	public void appendAnotherNamedValue(StringBuffer sb, String name, Number value) {
		sb.append(',');
		appendNamedValue(sb, name, value);
	}

	public void appendNamedValue(StringBuffer sb, String name, Number value) {
		appendString(sb, name);
		sb.append(':');
		if (value == null) {
			sb.append("null");
		} else {
			sb.append(value.toString());
		}
	}

	public void appendAnotherNamedValue(StringBuffer sb, String name, boolean value) {
		sb.append(',');
		appendNamedValue(sb, name, value);
	}

	public void appendAnotherNamedValue(StringBuffer sb, String name, Object value) {
		sb.append(',');
		appendNamedValue(sb, name, value);
	}

	public void appendAnotherNamedValue(StringBuffer sb, String name, List value) {
		sb.append(',');
		appendNamedValue(sb, name, value);
	}

	public void appendAnotherNamedValue(StringBuffer sb, String name, String value) {
		sb.append(',');
		appendNamedValue(sb, name, value);
	}

	public void appendNamedValue(StringBuffer sb, String name, String value) {
		appendString(sb, name);
		sb.append(':');
		appendString(sb, value);
	}

	public void appendString(StringBuffer sb, String name) {
		if (name == null) {
			sb.append("null");
		} else {
			sb.append('"');
			escapeStringForJson(sb, name);
			sb.append("\"");
		}
	}

	private void escapeStringForJson(StringBuffer sb, String value) {

		if (value == null) {
			sb.append("null");
		} else {
			char[] toCharArray = value.toCharArray();
			for (int i = 0; i < toCharArray.length; i++) {
				char curChar = toCharArray[i];

				if (curChar == '"') {
					sb.append('\\');
					sb.append(curChar);
				} else if (curChar == '\'') {
					sb.append('\\');
					sb.append(curChar);
				} else if (curChar == '\\') {
					sb.append('\\');
					sb.append(curChar);
				} else if (curChar == '\b') {
					sb.append("\\b");
				} else if (curChar == '\f') {
					sb.append("\\f");
				} else if (curChar == '\n') {
					sb.append("\\n");
				} else if (curChar == '\r') {
					sb.append("\\r");
				} else if (curChar == '\t') {
					sb.append("\\t");
				} else if (curChar > 255) {
					sb.append("\\u");
					String hex = Integer.toHexString(curChar);
					for (int offset = hex.length(); offset < 4; offset++) {
						sb.append('0');
					}
					sb.append(hex);
				} else {
					sb.append(curChar);
				}

			}
		}
	}

}
