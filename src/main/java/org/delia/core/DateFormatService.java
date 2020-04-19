package org.delia.core;

import java.util.Date;
import java.util.TimeZone;

/**
 * Service for parsing and formatting dates.
 * TimeZone can be adjusted
 * @author Ian Rae
 *
 */
public interface DateFormatService {

    Date parse(String input);
    String format(Date dt);
    TimeZone detectTimezone(String input);
    DateFormatter createFormatter(String input);
    DateFormatter createFormatter(); //use default tz
}
