package org.delia.core;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;


public class DateFormatter {
	private TimeZone tz;
	private DateFormat formatter;

	public DateFormatter(TimeZone tz, DateFormat df) {
		this.tz = tz;
		this.formatter = df;
		this.formatter.setTimeZone(tz);
	}

	public String format(Date dt) {
		return formatter.format(dt);
	}

	public TimeZone getTimeZone() {
		return tz;
	}

}
