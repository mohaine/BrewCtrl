package com.mohaine.brewcontroller;

public class TimeParser {

	private String zeroDescription = null;

	public TimeParser() {
		super();
	}

	public long parse(String strValue) {

		if (zeroDescription != null && zeroDescription.equalsIgnoreCase(strValue)) {
			return 0;
		}

		String[] split = strValue.split(":");

		long seconds = 0;
		if (split.length == 1) {
			seconds = Long.parseLong(split[0]);
		} else if (split.length == 2) {
			seconds = Long.parseLong(split[0]) * 60;
			seconds += Long.parseLong(split[1]);
		} else if (split.length == 3) {
			seconds = Long.parseLong(split[0]) * 60 * 60;
			seconds += Long.parseLong(split[1]) * 60;
			seconds += Long.parseLong(split[2]);
		}

		return (seconds) * 1000;
	}

	public String format(long time) {
		if (time <= 0 && zeroDescription != null) {
			return zeroDescription;
		}

		time = time / 1000;

		int minutes = (int) (time / 60);
		int seconds = (int) time - (minutes * 60);

		return String.format("%d:%02d", minutes, seconds);
	}

	public void setZeroDescription(String negativeDescription) {
		this.zeroDescription = negativeDescription;
	}

}
