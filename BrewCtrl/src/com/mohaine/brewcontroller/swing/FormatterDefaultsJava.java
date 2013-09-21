package com.mohaine.brewcontroller.swing;

import java.text.DecimalFormat;
import java.text.ParseException;

import com.mohaine.brewcontroller.client.FormatterDefaults;

public class FormatterDefaultsJava implements FormatterDefaults {

	private static final Df DF_WHOLE = new Df(new DecimalFormat("0"));
	private static final Df DF_DEC = new Df(new DecimalFormat("0.0"));

	private static final class Df implements Formatter<Number> {
		private final DecimalFormat df;

		private Df(DecimalFormat df) {
			this.df = df;
		}

		@Override
		public String format(Number value) {
			return df.format(value);
		}

		@Override
		public Number parse(String value) throws ParseException {
			return df.parse(value);
		}
	}

	@Override
	public Formatter<Number> getWholeFormatter() {
		return DF_WHOLE;
	}

	@Override
	public Formatter<Number> getDecimalFormatter() {
		return DF_DEC;
	}

}
