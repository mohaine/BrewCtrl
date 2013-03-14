package com.mohaine.brewcontroller.web.client;

import java.text.ParseException;

import com.google.gwt.i18n.client.NumberFormat;
import com.mohaine.brewcontroller.client.FormatterDefaults;

public class FormatterDefaultsGwt implements FormatterDefaults {
	

	private static final Df DF_WHOLE = new Df(NumberFormat.getFormat("0"));
	private static final Df DF_DEC = new Df(NumberFormat.getFormat("0.0"));

	private static final class Df implements Formatter<Number> {
		private final NumberFormat df;

		private Df(NumberFormat df) {
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
