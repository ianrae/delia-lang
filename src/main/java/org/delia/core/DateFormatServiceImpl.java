package org.delia.core;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;


public class DateFormatServiceImpl implements DateFormatService {
	private static abstract class ParseSpec {
		public DateTimeFormatter formatter;
		public abstract ZonedDateTime parse(String input, TimeZoneService tzSvc);
		
		public ParseSpec(String pattern) {
			this.formatter = DateTimeFormatter.ofPattern(pattern);
		}
	}
	public static class YearOnlyParseSpec extends ParseSpec {
		public YearOnlyParseSpec(String pattern) {
			super(pattern);
		}
		@Override
		public ZonedDateTime parse(String input, TimeZoneService tzSvc) {
			LocalDate ldt = null;
			TemporalAccessor parsed = formatter.parse(input);	
			Year yr = Year.from(parsed);
			ldt = LocalDate.of(yr.getValue(), 1, 1);
			ZoneId zone = tzSvc.getDefaultTimeZone();
			return ZonedDateTime.of(ldt.atStartOfDay(), zone);
		}
	}
	public static class YearMonthParseSpec extends ParseSpec {
		public YearMonthParseSpec(String pattern) {
			super(pattern);
		}
		@Override
		public ZonedDateTime parse(String input, TimeZoneService tzSvc) {
			LocalDate ldt = null;
			TemporalAccessor parsed = formatter.parse(input);	
			Year yr = Year.from(parsed);
			Month month = Month.from(parsed);
			ldt = LocalDate.of(yr.getValue(), month.getValue(), 1);
			ZoneId zone = tzSvc.getDefaultTimeZone();
			return ZonedDateTime.of(ldt.atStartOfDay(), zone);
		}
	}
	public static class YearMonthDayParseSpec extends ParseSpec {
		public YearMonthDayParseSpec(String pattern) {
			super(pattern);
		}
		@Override
		public ZonedDateTime parse(String input, TimeZoneService tzSvc) {
			LocalDate ldt = null;
			TemporalAccessor parsed = formatter.parse(input);	
			Year yr = Year.from(parsed);
			MonthDay md = MonthDay.from(parsed);
			ldt = LocalDate.of(yr.getValue(), md.getMonthValue(), md.getDayOfMonth());
			ZoneId zone = tzSvc.getDefaultTimeZone();
			return ZonedDateTime.of(ldt.atStartOfDay(), zone);
		}
	}
	public static class LocalDateTimeParseSpec extends ParseSpec {
		public LocalDateTimeParseSpec(String pattern) {
			super(pattern);
		}
		@Override
		public ZonedDateTime parse(String input, TimeZoneService tzSvc) {
			LocalDateTime ldt = null;
			ldt = LocalDateTime.parse(input, formatter);
			ZoneId zone = tzSvc.getDefaultTimeZone();
			return ZonedDateTime.of(ldt, zone);
		}
	}
	public static class ZonedDateTimeParseSpec extends ParseSpec {
		public ZonedDateTimeParseSpec(String pattern) {
			super(pattern);
		}
		@Override
		public ZonedDateTime parse(String input, TimeZoneService tzSvc) {
			ZonedDateTime zdt = null;
			zdt = ZonedDateTime.parse(input, formatter);

			//https://stackoverflow.com/questions/39506891/why-is-zoneoffset-utc-zoneid-ofutc
			//sometimes zdt has zone 'Z' which is not considered equal to 'UTC'.
			//so we'll normalize zdt's zone and compare to tzSvc's zone
			//and use zone if they are equal. 
			ZoneId normalized1 = zdt.getZone().normalized();
			ZoneId zone = tzSvc.getDefaultTimeZone();
			ZoneId normalized2 = zone.normalized();
			if (normalized1.equals(normalized2)) {
				return ZonedDateTime.of(zdt.toLocalDateTime(), zone);
			} else {
				ZoneOffset zo1 = zdt.getOffset();
				ZoneOffset zo2 = zone.getRules().getOffset(zdt.toInstant());
				if (zo1.equals(zo2)) {
					return ZonedDateTime.of(zdt.toLocalDateTime(), zone);
				}
			}
			return zdt;
		}
	}
	//time-only
	public static class LocalTimeParseSpec extends ParseSpec {
		public LocalTimeParseSpec(String pattern) {
			super(pattern);
		}
		@Override
		public ZonedDateTime parse(String input, TimeZoneService tzSvc) {
			LocalTime lt = null;
			lt = LocalTime.parse(input, formatter);
			ZoneId zone = tzSvc.getDefaultTimeZone();
			LocalDate ld = LocalDate.of(1970,1,1); //TODO is this a good value?
			return ZonedDateTime.of(ld, lt, zone);
		}
	}


	//new version using Java 8 time
//	private DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy");
//	private DateTimeFormatter df2 = DateTimeFormatter.ofPattern("yyyy-MM");
//	private DateTimeFormatter df3 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//	private DateTimeFormatter df4 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");
//	private DateTimeFormatter df5 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
//	private DateTimeFormatter df6 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//	private DateTimeFormatter df6a = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
//	private DateTimeFormatter df7 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
//	private DateTimeFormatter dfFull = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	//http://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date

	//	private final DateFormat dfFullOld = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private TimeZoneService tzSvc;
	private ParseSpec pspec1 = new YearOnlyParseSpec("yyyy");
	private ParseSpec pspec2 = new YearMonthParseSpec("yyyy-MM");
	private ParseSpec pspec3 = new YearMonthDayParseSpec("yyyy-MM-dd");
	private ParseSpec pspec4 = new LocalDateTimeParseSpec("yyyy-MM-dd'T'HH");
	private ParseSpec pspec5 = new LocalDateTimeParseSpec("yyyy-MM-dd'T'HH:mm");
	private ParseSpec pspec6 = new LocalDateTimeParseSpec("yyyy-MM-dd'T'HH:mm:ss");
	private ParseSpec pspec6a = new ZonedDateTimeParseSpec("yyyy-MM-dd'T'HH:mm:ssZ");
	private ParseSpec pspec7 = new LocalDateTimeParseSpec("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private ParseSpec pspecFull = new ZonedDateTimeParseSpec("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	//time-only
	private ParseSpec pspecTime1 = new LocalTimeParseSpec("HH");
	private ParseSpec pspecTime2 = new LocalTimeParseSpec("HH:mm");
	private ParseSpec pspecTime3 = new LocalTimeParseSpec("HH:mm:ss");
	private ParseSpec pspecTimeFull = new LocalTimeParseSpec("HH:mm:ss.SSS");
	//TODO add timezone one for time-only

	public DateFormatServiceImpl(TimeZoneService tzSvc) {
		this.tzSvc = tzSvc;
	}

	@Override
	public Date parseLegacy(String input) {
		//		input = input.trim();
		try {
			ParseSpec parseSpec = getDateFormat(input);
			ZonedDateTime zdt = parseSpec.parse(input, tzSvc);
			return Date.from(zdt.toInstant());
		} catch (DateTimeParseException  e) {
			DeliaError err = new DeliaError("date-parse-error", e.getMessage());
			throw new DeliaException(err);
		}  
	}

//	private boolean containsTimeZone(DateTimeFormatter formatter) {
//		if (formatter == dfFull || formatter == df6a) {
//			return true;
//		}
//		return false;
//	}
//	private boolean isYearOnly(DateTimeFormatter formatter) {
//		if (formatter == df1) {
//			return true;
//		}
//		return false;
//	}
//	private boolean isMonthDayOnly(DateTimeFormatter formatter) {
//		if (formatter == df2 || formatter == df3) {
//			return true;
//		}
//		return false;
//	}

	private ParseSpec getDateFormat(String input) {
		int len = input.length();
		switch(len) {
		case 4:
			return pspec1;
		case 7:
			return pspec2;
		case 10:
			return pspec3;
		case 13:
			return pspec4;
		case 16:
			return pspec5;
		case 19:
			return pspec6;
		case 23:
			return pspec6;
		case 24:
			return pspec6a;
		case 27:
			return pspec7;
		default:
			return pspecFull;
		}
	}

	@Override
	public String format(Date dt) {
		String s = null;
		try {
			ZonedDateTime ldt = ZonedDateTime.ofInstant(dt.toInstant(), ZoneId.systemDefault());
			s = ldt.format(pspecFull.formatter);
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
			ParseSpec pspec = getDateFormat(input);
			ldt = ZonedDateTime.parse(input, pspec.formatter);
		} catch (DateTimeParseException  e) {
			DeliaError err = new DeliaError("date-parse-error", e.getMessage());
			throw new DeliaException(err);
		}  
		return ldt == null ? null : ldt.getZone();
	}

	@Override
	public DateFormatter createFormatter(String input) {
		ParseSpec pspec = getDateFormat(input);
		return new DateFormatter(tzSvc.getDefaultTimeZone(), pspec.formatter);
	}

	@Override
	public DateFormatter createFormatter() {
		return new DateFormatter(tzSvc.getDefaultTimeZone(), this.pspecFull.formatter);
	}

	@Override
	public ZonedDateTime parseDateTime(String input) {
		try {
			ParseSpec pspec = getDateFormat(input);
			ZonedDateTime zdt = pspec.parse(input, tzSvc);
			return zdt;
		} catch (DateTimeParseException  e) {
			DeliaError err = new DeliaError("date-parse-error", e.getMessage());
			throw new DeliaException(err);
		}  
	}
	@Override
	public ZonedDateTime parseTime(String input) {
		try {
			ParseSpec pspec = getTimeFormat(input);
			ZonedDateTime zdt = pspec.parse(input, tzSvc);
			return zdt;
		} catch (DateTimeParseException  e) {
			DeliaError err = new DeliaError("date-parse-error", e.getMessage());
			throw new DeliaException(err);
		}
	}
	@Override
	public boolean isTimeOnly(String input) {
		try {
			ParseSpec pspec = getTimeFormat(input);
			ZonedDateTime zdt = pspec.parse(input, tzSvc);
			return true;
		} catch (DateTimeParseException  e) {
			return false;
		}
	}

	private ParseSpec getTimeFormat(String input) {
		int len = input.length();
		//TODO: should support when no leading 0, such as 9:30
		switch(len) {
			case 2:
				return this.pspecTime1;
			case 5:
				return pspecTime2;
			case 8:
				return pspecTime3;
			default:
				return pspecTimeFull;
		}
	}

	@Override
	public String format(ZonedDateTime ldt) {
		return ldt.format(pspecFull.formatter);
	}

	@Override
	public String format(LocalDateTime ldt, ZoneId zoneId) {
		ZonedDateTime zdt = ZonedDateTime.of(ldt, zoneId);
		return zdt.format(pspecFull.formatter);
	}

	@Override
	public TimeZoneService getTimezoneService() {
		return tzSvc;
	}

}
