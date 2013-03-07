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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JsonParser {

	private int offset = 0;
	String jsonString;

	public JsonParser(String jsonString) {
		this.jsonString = jsonString;
	}

	public Object parseJson() {
		bypassWhitespace();
		char startChar = jsonString.charAt(offset);

		if (startChar == '\"') {
			return parseString();
		} else if (startChar == '-' || (startChar >= '0' && startChar <= '9')) {
			return parseNumber();
		} else if (startChar == 't' || startChar == 'f') {
			return parseBoolean();
		} else if (startChar == 'n') {
			return parseNull();
		} else if (startChar == '[') {
			return parseArray();
		} else if (startChar == '{') {
			return parseObject();
		}
		throw new RuntimeException("JSON: invalid entry at " + offset);
	}

	private void bypassWhitespace() {
		while (offset < jsonString.length() && isWhitespace(jsonString.charAt(offset))) {
			offset++;
		}
	}

	private boolean isWhitespace(char c) {
		return c == ' ' || c == '\r' || c == '\t' || c == '\n' || c == '\b' || c == '\f';
	}

	private Object parseObject() {
		offset++;
		Map<String, Object> values = createMap();
		while (offset < jsonString.length()) {
			bypassWhitespace();
			char startChar = jsonString.charAt(offset);
			if (startChar == '}') {
				offset++;
				return values;
			} else if (startChar == ',') {
				offset++;
				String name = parseName();
				values.put(name, parseJson());
			} else {
				String name = parseName();
				values.put(name, parseJson());
			}
		}

		throw new RuntimeException("JSON: invalid object. missing }");
	}

	protected Map<String, Object> createMap() {
		return new HashMap<String, Object>();
	}

	private String parseName() {
		bypassWhitespace();
		String name = parseString();
		if (name == null) {
			throw new RuntimeException("JSON: invalid ojbect. missing name");
		}
		bypassWhitespace();

		char valueChar = jsonString.charAt(offset);
		if (valueChar != ':') {
			throw new RuntimeException("JSON: invalid ojbect. missing :");
		}
		offset++;
		bypassWhitespace();
		return name;
	}

	private List parseArray() {
		offset++;
		List values = new ArrayList();
		while (offset < jsonString.length()) {
			bypassWhitespace();
			char startChar = jsonString.charAt(offset);
			if (startChar == ']') {
				offset++;
				return values;
			} else if (startChar == ',') {
				offset++;
				values.add(parseJson());
			} else {
				values.add(parseJson());
			}
		}
		throw new RuntimeException("JSON: invalid array. missing ]");
	}

	private Boolean parseNull() {
		if (jsonString.length() >= offset + 4) {
			String nullString = jsonString.substring(offset, offset + 4);
			offset += nullString.length();
			if ("null".equals(nullString)) {
				return null;
			}
		}
		throw new RuntimeException("JSON: invalid null");
	}

	private Boolean parseBoolean() {
		char curChar = jsonString.charAt(offset);

		if (curChar == 'f' && jsonString.length() >= offset + 5) {
			String falseString = jsonString.substring(offset, offset + 5);
			offset += falseString.length();
			if ("false".equals(falseString)) {
				return Boolean.FALSE;
			}
		} else if (curChar == 't' && jsonString.length() >= offset + 4) {
			String trueString = jsonString.substring(offset, offset + 4);
			offset += trueString.length();
			if ("true".equals(trueString)) {
				return Boolean.TRUE;
			}
		}
		throw new RuntimeException("JSON: invalid boolean");
	}

	private Number parseNumber() {

		boolean hasDot = false;
		boolean hasE = false;

		StringBuffer number = new StringBuffer();
		while (offset < jsonString.length()) {
			char curChar = jsonString.charAt(offset);
			if (curChar == '-') {
				offset++;
				number.append(curChar);
			} else if (curChar == '.') {
				offset++;
				if (hasDot) {
					throw new RuntimeException("JSON: Too many . in number");
				}
				number.append(curChar);
				hasDot = true;
			} else if (curChar == 'E' || curChar == 'e') {
				offset++;
				if (hasE) {
					throw new RuntimeException("JSON: Too many E in number");
				}
				hasE = true;
				number.append('e');
			} else if (curChar >= '0' && curChar <= '9') {
				offset++;
				number.append(curChar);
			} else {
				break;
			}
		}
		if (hasDot || hasE) {
			return new Double(number.toString());
		} else {
			return new Integer(number.toString());
		}
	}

	private String parseString() {
		// String
		StringBuffer sb = new StringBuffer();
		boolean lastWasEscape = false;
		offset++;
		while (offset < jsonString.length()) {
			char curChar = jsonString.charAt(offset);
			if (!lastWasEscape && curChar == '"') {
				offset++;
				return sb.toString();
			}
			if (lastWasEscape) {

				if (curChar == '"') {
					sb.append(curChar);
				} else if (curChar == '\'') {
					sb.append(curChar);
				} else if (curChar == '\\') {
					sb.append(curChar);
				} else if (curChar == '/') {
					sb.append(curChar);
				} else if (curChar == 'b') {
					sb.append("\b");
				} else if (curChar == 'f') {
					sb.append("\f");
				} else if (curChar == 'n') {
					sb.append("\n");
				} else if (curChar == 'r') {
					sb.append("\r");
				} else if (curChar == 't') {
					sb.append("\t");
				} else if (curChar == 'u') {
					String unicode = jsonString.substring(offset + 1, offset + 5);
					offset += 4;
					int value = Integer.parseInt(unicode, 16);
					sb.append((char) value);
				}
				lastWasEscape = false;
			} else {
				lastWasEscape = curChar == '\\';
				if (!lastWasEscape) {
					sb.append(curChar);
				}
			}
			offset++;
		}
		throw new RuntimeException("JSON Untermeniated String");
	}
}
