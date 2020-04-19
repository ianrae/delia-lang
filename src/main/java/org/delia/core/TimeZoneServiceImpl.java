package org.delia.core;

import java.util.TimeZone;

public class TimeZoneServiceImpl implements TimeZoneService {
	public static final String DEFAULT_TIMEZONE = "UTC";
//	public static final String DEFAULT_TIMEZONE = "America/New_York"; //TODO switch to UTC

	private TimeZone defaultTz;
	
	public TimeZoneServiceImpl() {
		this.defaultTz = TimeZone.getTimeZone(DEFAULT_TIMEZONE);
	}
	
	@Override
	public void setDefaultTimeZone(TimeZone tz) {
		defaultTz = tz;
	}

	@Override
	public TimeZone getDefaultTimeZone() {
		return defaultTz;
	}

}
