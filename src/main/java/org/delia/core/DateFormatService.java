package org.delia.core;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Service for parsing and formatting dates.
 * TimeZone can be adjusted
 * @author Ian Rae
 *
 */
public interface DateFormatService {

    Date parseLegacy(String input);
    ZonedDateTime parseDateTime(String input);
    ZonedDateTime parseTime(String input);
    boolean isTimeOnly(String input);
    String format(Date dt); //legacy
    String format(ZonedDateTime ldt);
    String format(LocalDateTime ldt, ZoneId zoneId);
    ZoneId detectTimezone(String input);
    DateFormatter createFormatter(String input);
    DateFormatter createFormatter(); //use default tz
    TimeZoneService getTimezoneService();
}
