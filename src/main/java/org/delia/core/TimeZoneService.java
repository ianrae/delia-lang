package org.delia.core;

import java.time.ZoneId;

/**
 * Service that manages Delia's default timezone.
 * @author Ian Rae
 *
 */
public interface TimeZoneService {

	void setDefaultTimeZone(ZoneId tz);
	ZoneId getDefaultTimeZone();
}
