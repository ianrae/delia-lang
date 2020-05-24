package org.delia.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.TimeZone;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;


public class DateFormatServiceImpl implements DateFormatService {
	//new version using Java 8 time
    private DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy");
    private DateTimeFormatter df2 = DateTimeFormatter.ofPattern("yyyy-MM");
    private DateTimeFormatter df3 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter df4 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");
    private DateTimeFormatter df5 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private DateTimeFormatter df6 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private DateTimeFormatter df6a = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private DateTimeFormatter df7 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private DateTimeFormatter dfFull = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	//http://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date
    
	private final DateFormat dfFullOld = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private TimeZoneService tzSvc;


	public DateFormatServiceImpl(TimeZoneService tzSvc) {
		this.tzSvc = tzSvc;
	}

	@Override
	public Date parse(String input) {
		try {
			DateTimeFormatter formatter = getDateFormat(input);
			if (containsTimeZone(formatter)) {
				ZonedDateTime ldt = null;
				ldt = ZonedDateTime.parse(input, formatter);
				return java.util.Date.from(ldt.toInstant());
			} else if (isYearOnly(formatter)) {
				LocalDate ldt = null;
				TemporalAccessor parsed = formatter.parse(input);	
				Year yr = Year.from(parsed);
				ldt = LocalDate.of(yr.getValue(), 1, 1);
				ZoneId zone = tzSvc.getDefaultTimeZone();
				return java.util.Date.from(ldt.atStartOfDay(zone).toInstant());
			} else if (isMonthDayOnly(formatter)) {
				LocalDate ldt = null;
				TemporalAccessor parsed = formatter.parse(input);	
				Year yr = Year.from(parsed);
				if (formatter == df2) {
					Month month = Month.from(parsed);
					ldt = LocalDate.of(yr.getValue(), month.getValue(), 1);
				} else {
					MonthDay md = MonthDay.from(parsed);
					ldt = LocalDate.of(yr.getValue(), md.getMonthValue(), md.getDayOfMonth());
				}
				ZoneId zone = tzSvc.getDefaultTimeZone();
				return java.util.Date.from(ldt.atStartOfDay(zone).toInstant());
			} else {
				LocalDateTime ldt = null;
				
				ldt = LocalDateTime.parse(input, formatter);
				ZoneId zone = tzSvc.getDefaultTimeZone();
				ZoneOffset zo = zone.getRules().getOffset(ldt); 
				return java.util.Date.from(ldt.toInstant(zo));
			}
		} catch (DateTimeParseException  e) {
			DeliaError err = new DeliaError("date-parse-error", e.getMessage());
			throw new DeliaException(err);
		}  
	}

	private boolean containsTimeZone(DateTimeFormatter formatter) {
		if (formatter == dfFull || formatter == df6a) {
			return true;
		}
		return false;
	}
	private boolean isYearOnly(DateTimeFormatter formatter) {
		if (formatter == df1) {
			return true;
		}
		return false;
	}
	private boolean isMonthDayOnly(DateTimeFormatter formatter) {
		if (formatter == df2 || formatter == df3) {
			return true;
		}
		return false;
	}

	private DateTimeFormatter getDateFormat(String input) {
		int len = input.length();
		switch(len) {
		case 4:
			return df1;
		case 7:
			return df2;
		case 10:
			return df3;
		case 13:
			return df4;
		case 16:
			return df5;
		case 19:
			return df6;
		case 23:
			return df6;
		case 24:
			return df6a;
		default:
			return dfFull;
		}
	}

	@Override
	public String format(Date dt) {
		String s = null;
		try {
			ZonedDateTime ldt = ZonedDateTime.ofInstant(dt.toInstant(), ZoneId.systemDefault());
		    s = ldt.format(dfFull);
		}
		catch (DateTimeException exc) {
		    System.out.printf("%s can't be formatted!", dt);
		    throw exc;
		}			
		return s;
	}

	@Override
	public ZoneId detectTimezone(String input) {
		ZonedDateTime ldt = null;
		try {
			DateTimeFormatter formatter = getDateFormat(input);
			ldt = ZonedDateTime.parse(input, formatter);
		} catch (DateTimeParseException  e) {
			DeliaError err = new DeliaError("date-parse-error", e.getMessage());
			throw new DeliaException(err);
		}  
		return ldt == null ? null : ldt.getZone();
	}

	@Override
	public DateFormatter createFormatter(String input) {
		//TODO fix
		return createFormatter();
	}

	@Override
	public DateFormatter createFormatter() {
		return new DateFormatter(TimeZone.getDefault(), this.dfFullOld);
	}
}
