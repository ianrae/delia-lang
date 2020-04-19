package org.delia.core;

import java.util.TimeZone;

/**
 * Service that manages Delia's default timezone.
 * @author Ian Rae
 *
 */
public interface TimeZoneService {

	void setDefaultTimeZone(TimeZone tz);
	TimeZone getDefaultTimeZone();
}
