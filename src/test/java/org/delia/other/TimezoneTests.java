package org.delia.other;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;


public class TimezoneTests {


	@Test
	public void test() {
		String[] id = TimeZone.getAvailableIDs();        
		System.out.println("In TimeZone class available Ids are: ");  
		for (int i=0; i<id.length; i++) {  
			System.out.println(id[i]);
		}
		//US/Eastern
		
		TimeZone tz = TimeZone.getDefault();
		System.out.println("default: " + tz);
		
	    DateFormat dfFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	    Date dt = new Date();
		String s = dfFull.format(dt);
		System.out.println(s);
		
		dfFull.setTimeZone(tz);
		s = dfFull.format(dt);
		System.out.println(s);
		
		tz = TimeZone.getTimeZone("US/Pacific");
		dfFull.setTimeZone(tz);
		s = dfFull.format(dt);
		System.out.println(s);
	}
	
	@Test
	public void test2() throws ParseException {
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String string1 = "2001-07-04T12:08:56.235-0700";
		Date result1 = df1.parse(string1);

		DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		String string2 = "2001-07-04T12:08:56.235-07:00";
		Date result2 = df2.parse(string2);
		System.out.println(result2.toString());
	}
	
	//https://stackoverflow.com/questions/2891361/how-to-set-time-zone-of-a-java-util-date
	@Test
	public void test3() throws ParseException {
		DateFormat df1 = new SimpleDateFormat("yyyy");
		String string1 = "2001";
		Date result1 = df1.parse(string1);
		System.out.println("dt:  " + result1.toString()); //date.toString uses system locale
		
		DateFormat df7 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		String s = df7.format(result1);
		System.out.println("cal: " + s);
		
		DateFormat df2 = new SimpleDateFormat("yyyy");
		TimeZone tz = TimeZone.getTimeZone("UTC");
		df2.setTimeZone(tz);
		String string2 = "2001";
		Date result2 = df2.parse(string2);
//		System.out.println(result2.toString());
		df7.setTimeZone(tz);
		s = df7.format(result2);
		System.out.println("cal: " + s);
		
		SimpleDateFormat dfFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		dfFull.setTimeZone(tz);
		s = dfFull.format(result2);
		System.out.println("cxl: " + s);
	}
	
	@Test
	public void test4() throws ParseException {
		DateFormat df1 = new SimpleDateFormat("yyyy");
		String string1 = "2001";
		Date result1 = df1.parse(string1);
		System.out.println("xdt:  " + result1.toString()); //date.toString uses system locale
		
		DateFormat df7 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		String s = df7.format(result1);
		System.out.println("cal: " + s);
		
		DateFormat df2 = new SimpleDateFormat("yyyy");
		TimeZone tz = TimeZone.getTimeZone("America/Monterrey");
		df2.setTimeZone(tz);
		String string2 = "2001";
		Date result2 = df2.parse(string2);
//		System.out.println(result2.toString());
		df7.setTimeZone(tz);
		s = df7.format(result2);
		System.out.println("cal: " + s);
		
		SimpleDateFormat dfFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dfFull.setTimeZone(tz);
		s = dfFull.format(result2);
		System.out.println("cxl: " + s);
	}
	
	@Test
	public void test5() throws ParseException {
		DateFormat df1 = new SimpleDateFormat("yyyy");
		String string1 = "2001";
		Date result1 = df1.parse(string1);
		System.out.println("xdt:  " + result1.toString()); //date.toString uses system locale
		
		DateFormat df7 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		String s = df7.format(result1);
		System.out.println("cal: " + s);
		
		TimeZone tz = TimeZone.getTimeZone("US/Pacific");
		TimeZone tz2 = TimeZone.getTimeZone("GMT-0800");
		DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		df2.setTimeZone(tz);
		s = df2.format(result1);
		System.out.println("cxa: " + s);
		
		SimpleDateFormat dfFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dfFull.setTimeZone(tz2);
		s = dfFull.format(result1);
		System.out.println("cxb: " + s);
	}
	
	@Test
	public void test6() throws ParseException {
		TimeZone tz = TimeZone.getTimeZone("US/SOMEWHEREPacific");
		assertNotNull(tz);
		
		System.out.println("cxb: " + tz.getDisplayName());
		
		SimpleDateFormat dfFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String s ="2020-01-31T09:59:51.000-0000"; //;date(2020-01-31T09:59:51.000Z)
		Date dt = dfFull.parse(s);
		System.out.println("cxb: " + dt.toString());
	}
	@Test
	public void testJava8() throws ParseException {
		
		java.util.Date dt = Date.from( Instant.parse( "2014-12-12T10:39:40Z" ));
		System.out.println("cxb: " + dt.toString());
		
		OffsetDateTime odt = OffsetDateTime.parse( "2010-01-01T12:00:00+01:00" );
		Instant instant = odt.toInstant();  // Instant is always in UTC.
		dt = java.util.Date.from( instant );
		System.out.println("cxb: " + dt.toString());

		dt = new Date();
		instant = Instant.now();
		long n1 = instant.getEpochSecond();
		int nano = instant.getNano();
		System.out.println(String.format("instant: %d %d", n1, nano));
		System.out.println(String.format("     dt: %d ", dt.getTime()));
		
		dt = Date.from(instant);
		System.out.println("dt: " + dt.toString());
	}
	
	@Test
	public void testJava8b() throws ParseException {
		
		for(String zone: ZoneId.getAvailableZoneIds()) {
			System.out.println(zone);
		}
	}
	
}
