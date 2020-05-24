package org.delia.core;

import static org.junit.Assert.assertEquals;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

import org.delia.base.UnitTestLog;
import org.delia.error.DeliaError;
import org.delia.log.Log;
import org.delia.runner.DeliaException;
import org.delia.type.WrappedDate;
import org.junit.Test;

public class DateFormatServiceTests {
	
	public static class NewDateFormatServiceImpl implements DateFormatService {
	    DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy");
	    DateTimeFormatter df2 = DateTimeFormatter.ofPattern("yyyy-MM");
	    DateTimeFormatter df3 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    DateTimeFormatter df4 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH");
	    DateTimeFormatter df5 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
	    DateTimeFormatter df6 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	    DateTimeFormatter df6a = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV");
	    DateTimeFormatter df7 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
	    DateTimeFormatter dfFull = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV");
		//http://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date

		@Override
		public Date parse(String input) {
//			ZoneId zoneId = ZoneId.systemDefault();
			ZonedDateTime ldt = null;
			try {
				DateTimeFormatter formatter = getDateFormat(input);
				ldt = ZonedDateTime.parse(input, formatter);
			} catch (DateTimeParseException  e) {
				DeliaError err = new DeliaError("date-parse-error", e.getMessage());
				throw new DeliaException(err);
			}  
			return java.util.Date.from(ldt.toInstant());
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
		public TimeZone detectTimezone(String input) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DateFormatter createFormatter(String input) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DateFormatter createFormatter() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	@Test
	public void test() {
		DateFormatService fmtSvc = createSvc();
		
		Date dt = new Date();
		String s = fmtSvc.format(dt);
		log(s);
		Date dt2 = fmtSvc.parse(s);
		assertEquals(dt, dt2);
	}
	
	@Test
	public void test2() {
		DateFormatService fmtSvc = createSvc();
		TimeZone tz = TimeZone.getTimeZone("US/Pacific");
		tzSvc.setDefaultTimeZone(tz);
		Date dt = new Date();
		String s = fmtSvc.format(dt);
		log(s);
		Date dt2 = fmtSvc.parse(s);
		assertEquals(dt, dt2);
	}
	
	@Test
	public void test3() {
		DateFormatService fmtSvc = createSvc();
		
		TimeZone tz = fmtSvc.detectTimezone("2001-07-04T12:08:56.235-0700");
		tzSvc.setDefaultTimeZone(tz);
		Date dt = new Date();
		String s = fmtSvc.format(dt);
		log(s);
		Date dt2 = fmtSvc.parse(s);
		assertEquals(dt, dt2);
	}

	@Test
	public void test4() {
		DateFormatService fmtSvc = createSvc();
		
		Date dt = fmtSvc.parse("2019");
		DateFormatter formatter = fmtSvc.createFormatter();
		WrappedDate wdt = new WrappedDate(dt, formatter);
		String s = wdt.asString();
		log(s);
//		
//		Date dt = fmtSvc.parse("2019");
//		DateFormatter formatter = fmtSvc.createFormatter();
//		WrappedDate wdt = new WrappedDate(dt, formatter);
//		String s = wdt.asString();
//		log(s);
	}

	// --
	protected Log log = new UnitTestLog();
	TimeZoneService tzSvc = new TimeZoneServiceImpl();

	private DateFormatService createSvc() {
//		return new DateFormatServiceImpl(tzSvc);
		return new NewDateFormatServiceImpl();
	}

	protected void log(String s) {
		log.log(s);
	}

}
