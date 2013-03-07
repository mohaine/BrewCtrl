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
import java.util.Date;
import java.util.List;

public class JsonDecoder {

	private int offset = 0;
	private String jsonString;
	private JsonConverterConfig config;

	public JsonDecoder(String jsonString) {
		this.jsonString = jsonString;
	}

	public JsonDecoder(JsonConverterConfig config, String jsonString) {
		this.config = config;
		this.jsonString = jsonString;
	}

	public Object parseJson() {
		bypassWhitespace();
		int startOffset = offset;

		if (offset >= jsonString.length()) {
			return null;
		}

		char startChar = jsonString.charAt(offset);

		if (startChar == '\"') {
			int startIndex = offset;
			String str = parseString();

			if (validateStringAt("\\/Date(", startIndex + 1) && validateStringAt(")\\/", offset - 4)) {
				String epoch = str.substring(6, str.length() - 2);

				boolean validDate = true;
				for (int i = 0; i < epoch.length(); i++) {
					char value = epoch.charAt(i);
					if (value < '0' || value > '9') {
						validDate = false;
						break;
					}
				}
				if (validDate) {
					return new Date(Long.parseLong(epoch));
				}
			}

			return str;
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
		throw new RuntimeException("JSON: invalid entry '" + startChar + "' at " + getLineChar(startOffset));
	}

	private boolean validateStringAt(String string, int startIndex) {
		if (jsonString.length() < startIndex + string.length()) {
			return false;
		}
		for (int i = 0; i < string.length(); i++) {
			if (jsonString.charAt(i + startIndex) != string.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	private String getLineChar(int offset) {
		int lineCount = 1;
		int lineCharCount = 0;

		int lastChar = -1;
		for (int i = 0; i <= offset; i++) {
			char charAt = jsonString.charAt(i);
			if (charAt == '\r') {
				lineCharCount = 0;
				lineCount++;
			} else if (charAt == '\n') {
				if (lastChar != '\r') {
					lineCharCount = 0;
					lineCount++;
				}
			} else {
				lineCharCount++;
			}

			lastChar = charAt;

		}
		return lineCount + ":" + lineCharCount;
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
		int startIndex = offset;
		offset++;
		JsonUnknownObject unknownObject = new JsonUnknownObject();
		while (offset < jsonString.length()) {
			bypassWhitespace();
			char startChar = jsonString.charAt(offset);
			if (startChar == '}') {
				offset++;
				return config.convertToObject(unknownObject);
			} else if (startChar == ',') {
				offset++;
				String name = parseName();
				unknownObject.setProperty(name, parseJson());
			} else {
				String name = parseName();
				unknownObject.setProperty(name, parseJson());
			}
		}

		throw new RuntimeException("JSON: invalid object. missing } at " + getLineChar(startIndex));
	}

	private String parseName() {
		bypassWhitespace();
		String name = parseString();
		if (name == null) {
			throw new RuntimeException("JSON: invalid ojbect. missing name at " + getLineChar(offset));
		}
		bypassWhitespace();

		char valueChar = jsonString.charAt(offset);
		if (valueChar != ':') {
			throw new RuntimeException("JSON: invalid ojbect. missing : at " + getLineChar(offset));
		}
		offset++;
		bypassWhitespace();
		return name;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<?> parseArray() {
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
		throw new RuntimeException("JSON: invalid array. missing ] at " + getLineChar(offset));
	}

	private Boolean parseNull() {
		if (jsonString.length() >= offset + 4) {
			String nullString = jsonString.substring(offset, offset + 4);
			offset += nullString.length();
			if ("null".equals(nullString)) {
				return null;
			}
		}
		throw new RuntimeException("JSON: invalid null at " + getLineChar(offset));
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
		throw new RuntimeException("JSON: invalid boolean at " + getLineChar(offset));
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
					throw new RuntimeException("JSON: Too many . in number at " + getLineChar(offset));
				}
				number.append(curChar);
				hasDot = true;
			} else if (curChar == 'E' || curChar == 'e') {
				offset++;
				if (hasE) {
					throw new RuntimeException("JSON: Too many E in number at " + getLineChar(offset));
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
		int startOffet = offset;
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
		throw new RuntimeException("JSON Untermeniated String at " + getLineChar(startOffet));
	}
}
