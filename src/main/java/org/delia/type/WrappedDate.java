package org.delia.type;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.delia.core.DateFormatter;

/**
 * Used to hold a date value,
 * and its formatter.
 * The formatter is needed for DValueImpl.asString and
 * we don't want to have to store a formatter in every dval.
 * 
 * @author Ian Rae
 *
 */
public class WrappedDate {
	private ZonedDateTime zdt;
	private DateFormatter dateFormatter;

	public WrappedDate(ZonedDateTime zdt, DateFormatter dateFormatter) {
		this.zdt = zdt;
		this.dateFormatter = dateFormatter;
	}
	
	public String asString() {
		return dateFormatter.format(zdt);
	}
	public Date getLegacyDate() {
		Date dt = Date.from(zdt.toInstant());
		return dt;
	}
	public ZonedDateTime getDate() {
		return zdt;
	}
	public ZoneId getTimeZone() {
		return zdt.getZone();
	}
}
