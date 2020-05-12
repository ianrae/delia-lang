package org.delia.core;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;


public class DateFormatServiceImpl implements DateFormatService {

	//http://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date
	private final DateFormat df1 = new SimpleDateFormat("yyyy");
	private final DateFormat df2 = new SimpleDateFormat("yyyy-MM");
	private final DateFormat df3 = new SimpleDateFormat("yyyy-MM-dd");
	private final DateFormat df4 = new SimpleDateFormat("yyyy-MM-dd'T'HH");
	private final DateFormat df5 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	private final DateFormat df6 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final DateFormat df6a = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private final DateFormat df7 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private final DateFormat dfFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                                                        //	yyyy-MM-dd'T'HH:mm:ss.SSSZ
	
	private TimeZoneService tzSvc;


	public DateFormatServiceImpl(TimeZoneService tzSvc) {
		this.tzSvc = tzSvc;
	}

	@Override
	public Date parse(String input) {
		//support all forms of iso 8601!!
		//        String string1 = "2001-07-04T12:08:56.235-0700";
		Date dt = null;
		try {
			DateFormat df = getDateFormat(input);
			applyTimeZone(df);
			dt = df.parse(input);
		} catch (ParseException e) {
			DeliaError err = new DeliaError("date-parse-error", e.getMessage());
			throw new DeliaException(err);
		}  
		return dt;
	}

	private void applyTimeZone(DateFormat df) {
		TimeZone tz = tzSvc.getDefaultTimeZone();
		df.setTimeZone(tz);
	}

	@Override
	public String format(Date dt) {
		applyTimeZone(dfFull);
		return dfFull.format(dt);
	}

	private DateFormat getDateFormat(String input) {
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
	public TimeZone detectTimezone(String input) {
		//		//        String string1 = "2001-07-04T12:08:56.235-0700";
		int n = input.length();
		if (n < 5) {
			return tzSvc.getDefaultTimeZone();
		} else {
			int index = input.lastIndexOf('-');
			if (index == n - 5) {
				String s = input.substring(index);
				String ss = String.format("GMT%s", s);
				TimeZone tz2 = TimeZone.getTimeZone(ss);
				return tz2;
			} else {
				return tzSvc.getDefaultTimeZone();
			}
		}
	}

	@Override
	public DateFormatter createFormatter(String input) {
//		DateFormat df = getDateFormat(input);
		TimeZone tz = detectTimezone(input);
		return new DateFormatter(tz, dfFull);
	}

	@Override
	public DateFormatter createFormatter() {
		TimeZone tz = tzSvc.getDefaultTimeZone();
		return new DateFormatter(tz, this.dfFull);
	}
}
