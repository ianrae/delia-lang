package org.delia.type;

import java.util.Date;
import java.util.TimeZone;

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
	private Date dt;
	private DateFormatter dateFormatter;

	public WrappedDate(Date dt, DateFormatter dateFormatter) {
		this.dt = dt;
		this.dateFormatter = dateFormatter;
	}
	
	public String asString() {
		return dateFormatter.format(dt);
	}
	public Date getDate() {
		return dt;
	}
	public TimeZone getTimeZone() {
		return dateFormatter.getTimeZone();
	}
}
