package org.delia;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;

import static org.junit.Assert.assertEquals;

public class OtherTests {

    @Test
    public void test() {
        assertEquals(1, 1);
        String s = StringUtils.substringBefore("abc:88", ":");
        assertEquals("abc", s);

        s = StringUtils.substringBefore("abc", ":");
        assertEquals("abc", s);

        String url = "http://localhost:8080/_api/v1/subscri";
    }

    @Test
    public void testdt() {
        String local_date = "2021-10-25";
        String local_time = "15:31:03";
        String utc_offset = "-05:00";

        String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

        //        return LocalDateTime.parse(str, formatter).atZone(ZoneId.of("UTC"));
        String s = String.format("%s %s", local_date, local_time);
        LocalDateTime ldt = LocalDateTime.parse(s, formatter);
        ZonedDateTime zdt = ldt.atZone(ZoneId.of(utc_offset));

        log(ldt.toString());
        log(zdt.toString());

        ZoneId australia = ZoneId.of("America/Los_Angeles");
        ZonedDateTime zdt2 = zdt.withZoneSameInstant(australia);
        log(zdt2.toString());

        zdt2 = zdt.withZoneSameInstant(ZoneId.of("UTC"));
        log(zdt2.toString());

        log(ZoneId.of("UTC").getId());
        log(australia.getId());
        log(ZoneId.of(utc_offset).getId());
    }

    @Test
    public void test2() {
        ZonedDateTime zdt = ZonedDateTime.now();
        int week = zdt.get ( IsoFields.WEEK_OF_WEEK_BASED_YEAR );
        int weekYear = zdt.get ( IsoFields.WEEK_BASED_YEAR );
        log(zdt.toString());
        log(String.format("%d and %d", week, weekYear));
    }

    private void log(String s) {
        System.out.println(s);
    }
}
