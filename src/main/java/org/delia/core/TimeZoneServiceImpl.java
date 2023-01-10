package org.delia.core;

import java.time.ZoneId;

public class TimeZoneServiceImpl implements TimeZoneService {
	public static final String DEFAULT_TIMEZONE = "UTC";

	private ZoneId defaultTz;
	
	public TimeZoneServiceImpl() {
		this.defaultTz = ZoneId.of(DEFAULT_TIMEZONE);
	}

	@Override
	public void setDefaultTimeZone(ZoneId tz) {
		defaultTz = tz;
	}

	@Override
	public ZoneId getDefaultTimeZone() {
		return defaultTz;
	}

}
