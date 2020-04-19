package org.delia.core;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.TimeZone;

import org.delia.base.UnitTestLog;
import org.delia.core.DateFormatService;
import org.delia.core.DateFormatServiceImpl;
import org.delia.core.DateFormatter;
import org.delia.core.TimeZoneService;
import org.delia.core.TimeZoneServiceImpl;
import org.delia.log.Log;
import org.delia.type.WrappedDate;
import org.junit.Test;

public class DateFormatServiceTests {
	
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
		return new DateFormatServiceImpl(tzSvc);
	}

	protected void log(String s) {
		log.log(s);
	}

}
