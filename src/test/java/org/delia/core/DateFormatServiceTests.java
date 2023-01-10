package org.delia.core;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.delia.base.UnitTestLog;
import org.delia.log.Log;
import org.delia.type.WrappedDate;
import org.junit.Test;

public class DateFormatServiceTests {
	
	@Test
	public void testLegacy() {
		DateFormatService fmtSvc = createSvc();
		
		Date dt = new Date();
		String s = fmtSvc.format(dt);
		log(s);
		Date dt2 = fmtSvc.parseLegacy(s);
		assertEquals(dt, dt2);
	}
	@Test
	public void test() {
		DateFormatService fmtSvc = createSvc();
		
		ZonedDateTime zdt = getNow();
		String s = fmtSvc.format(zdt);
		log(s);
		ZonedDateTime zdt2 = fmtSvc.parseDateTime(s);
		ZonedDateTime zdt3 = clean(zdt);
		assertEquals(zdt3, zdt2);
	}
	@Test
	public void test2() {
		DateFormatService fmtSvc = createSvc();
		ZoneId tz = ZoneId.of("US/Pacific");
		tzSvc.setDefaultTimeZone(tz);
		ZonedDateTime zdt = getNow();
		String s = fmtSvc.format(zdt);
		log(s);
		ZonedDateTime zdt2 = fmtSvc.parseDateTime(s);
		ZonedDateTime zdt3 = clean(zdt);
		assertEquals(zdt3, zdt2);
	}
	
	@Test
	public void test3() {
		DateFormatService fmtSvc = createSvc();
		
		ZoneId tz = fmtSvc.detectTimezone("2001-07-04T12:08:56.235-0700");
		log.log(tz.getId());
		tzSvc.setDefaultTimeZone(tz);
		Date dt = new Date();
		String s = fmtSvc.format(dt);
		log(s);
		Date dt2 = fmtSvc.parseLegacy(s);
		assertEquals(dt, dt2);
	}

	@Test
	public void test4() {
		DateFormatService fmtSvc = createSvc();
		
		ZonedDateTime zdt = fmtSvc.parseDateTime("2019");
		DateFormatter formatter = fmtSvc.createFormatter("2019");
		WrappedDate wdt = new WrappedDate(zdt, formatter);
		String s = wdt.asString();
		log(s);
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

	private ZonedDateTime getNow() {
		ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.now(), tzSvc.getDefaultTimeZone());
		return zdt;
	}
	private ZonedDateTime clean(ZonedDateTime zdt) {
		int nn = zdt.getNano(); //delia uses msec so remove sub-msec info
		int nn2 = (nn - (nn % 1000000))/1000000;
		ZonedDateTime zdt3 = zdt.withNano(nn2*1000000);
		return zdt3;
	}
}
