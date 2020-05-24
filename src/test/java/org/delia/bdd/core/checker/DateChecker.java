package org.delia.bdd.core.checker;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.delia.bdd.core.ThenValue;
import org.delia.core.DateFormatService;
import org.delia.core.DateFormatServiceImpl;
import org.delia.core.TimeZoneService;
import org.delia.core.TimeZoneServiceImpl;
import org.delia.log.Log;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.WrappedDate;

public class DateChecker extends ValueCheckerBase {
		TimeZoneService tzSvc = new TimeZoneServiceImpl();
		private DateFormatService fmtSvc = new DateFormatServiceImpl(tzSvc);
		
		@Override
		public void chkShape(BDDResult bddres) {
			assertEquals(Shape.DATE, bddres.res.shape);
		}

		@Override
		public boolean compareObj(ThenValue thenVal, DValue dval, Log log) {
			tzSvc.setDefaultTimeZone(sess.getDefaultTimezone());
			
			if (! thenVal.expected.startsWith("date(")) {
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
			
			if (! compareTimezones(wdtExpected, wdt)) { 
				String s1 = wdtExpected.getTimeZone().getId();
				String s2 = wdt.getTimeZone().getId();
				String err = String.format("value-mismatch: TimeZone expected '%s' but got '%s'", s1, s2);
				log.logError(err);
				return false;
			} else if (! wdtExpected.getDate().equals(wdt.getDate())) {
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
			boolean b = tz1.equals(tz2); //TODO: or use normalize?
			return b;
		}

		private WrappedDate parseDateFn(String thenStr) {
			String ss = thenStr.trim();
			String input = ss.substring(5, ss.length() - 1);

			ZonedDateTime zdt = fmtSvc.parseDateTime(input);
			return new WrappedDate(zdt, fmtSvc.createFormatter(input));
		}
	}