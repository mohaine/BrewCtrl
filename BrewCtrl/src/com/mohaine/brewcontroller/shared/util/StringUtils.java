package com.mohaine.brewcontroller.shared.util;

public class StringUtils {

	private StringUtils() {
	}

	public static String getFileExtension(String fileName) {
		int dotPos;
		dotPos = fileName.lastIndexOf('.');
		return fileName.substring(dotPos + 1);
	}

	public static String valueOf(Object string) {
		if (string == null) {
			return "";
		}
		return string.toString();
	}

	public static String toString(Object string) {
		if (string == null) {
			return null;
		}
		return string.toString();
	}

	public static boolean hasLength(Object string) {
		return hasLength(valueOf(string));
	}

	public static boolean hasLength(String string) {
		if (string == null || string.length() == 0) {
			return false;
		}
		int strLength = string.length();
		for (int i = 0; i < strLength; i++) {
			char charAt = string.charAt(i);
			if (charAt > ' ') {
				return true;
			}
		}
		return false;
	}

	public static String trim(String value) {
		if (value != null) {
			value = value.trim();
		}
		return value;

	}

}