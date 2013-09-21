package com.mohaine.brewcontroller.client;

import java.text.ParseException;

public interface FormatterDefaults {
	public interface Formatter<T> {

		String format(T value);

		T parse(String value) throws ParseException;

	}

	public Formatter<Number> getWholeFormatter();

	public Formatter<Number> getDecimalFormatter();
}
