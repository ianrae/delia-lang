package org.delia.core;

import java.time.ZoneId;
import java.util.Date;

/**
 * Service for parsing and formatting dates.
 * TimeZone can be adjusted
 * @author Ian Rae
 *
 */
public interface DateFormatService {

    Date parse(String input);
    String format(Date dt);
    ZoneId detectTimezone(String input);
    DateFormatter createFormatter(String input);
    DateFormatter createFormatter(); //use default tz
}
