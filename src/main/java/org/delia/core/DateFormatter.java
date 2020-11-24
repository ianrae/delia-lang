package org.delia.core;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class DateFormatter {
	private ZoneId zoneId;
	private DateTimeFormatter formatter;

	public DateFormatter(ZoneId zoneId, DateTimeFormatter df) {
		this.zoneId = zoneId;
		this.formatter = df;
	}

	public String format(Date dt) {
		ZonedDateTime ldt = ZonedDateTime.ofInstant(dt.toInstant(), zoneId);
		return ldt.format(formatter);
	}
	public String format(ZonedDateTime zdt) {
		return zdt.format(formatter);
	}

	public ZoneId getTimeZone() {
		return zoneId;
	}

}
