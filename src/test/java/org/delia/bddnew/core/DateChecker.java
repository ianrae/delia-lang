package org.delia.bddnew.core;

import org.delia.DeliaSession;
import org.delia.core.DateFormatService;
import org.delia.core.DateFormatServiceImpl;
import org.delia.core.TimeZoneService;
import org.delia.core.TimeZoneServiceImpl;
import org.delia.log.DeliaLog;
import org.delia.type.DValue;
import org.delia.type.WrappedDate;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateChecker {
    TimeZoneService tzSvc = new TimeZoneServiceImpl();
    private DateFormatService fmtSvc = new DateFormatServiceImpl(tzSvc);

    public boolean compareObj(ThenValue thenVal, DValue dval, DeliaLog log, DeliaSession sess) {
        tzSvc.setDefaultTimeZone(sess.getDefaultTimezone());

        if (!thenVal.expected.startsWith("date(")) {
            String err = String.format("then must use date()", thenVal.expected);
            log.logError(err);
            return false;
        }

        WrappedDate wdtExpected = parseDateFn(thenVal.expected);
        WrappedDate wdt = (WrappedDate) dval.getObject();

//			String ss1 = wdtExpected.asString();
//			String ss2 = wdt.asString();
//			log.log(ss1);
//			log.log(ss2);

        if (!compareTimezones(wdtExpected, wdt)) {
            String s1 = wdtExpected.getTimeZone().getId();
            String s2 = wdt.getTimeZone().getId();
            String err = String.format("value-mismatch: TimeZone expected '%s' but got '%s'", s1, s2);
            log.logError(err);
            return false;
        } else if (!wdtExpected.asString().equals(wdt.asString())) {
            String s1 = wdtExpected.asString();
            String s2 = wdt.asString();
            String err = String.format("value-mismatch: Date expected '%s' but got '%s'", s1, s2);
            log.logError(err);
            return false;
        }
        return true;
    }

    private boolean compareTimezones(WrappedDate wdtExpected, WrappedDate wdt) {
        ZoneId tz1 = wdtExpected.getTimeZone();
        ZoneId tz2 = wdt.getTimeZone();
        tz1 = tz1.normalized();
        tz2 = tz1.normalized();
        boolean b = tz1.equals(tz2);
        return b;
    }

    private WrappedDate parseDateFn(String thenStr) {
        String ss = thenStr.trim();
        String input = ss.substring(5, ss.length() - 1);

        ZonedDateTime zdt = fmtSvc.parseDateTime(input);
        return new WrappedDate(zdt, fmtSvc.createFormatter(input));
    }
}