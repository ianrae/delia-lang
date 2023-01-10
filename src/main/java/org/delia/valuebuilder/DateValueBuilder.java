package org.delia.valuebuilder;

import org.delia.core.DateFormatService;
import org.delia.core.DateFormatter;
import org.delia.core.FactoryService;
import org.delia.core.TimeZoneService;
import org.delia.type.*;

import java.time.ZonedDateTime;
import java.util.Date;

public class DateValueBuilder extends DValueBuilder {

	
	private DateFormatService fmtSvc;
	private TimeZoneService tzSvc;

	public DateValueBuilder(FactoryService factorySvc, DType type) {
		this.fmtSvc = factorySvc.getDateFormatService();
		this.tzSvc = factorySvc.getTimeZoneService();
		
		if (!type.isShape(Shape.DATE)) {
			addWrongTypeError("expecting number");
			return;
		}
		this.type = type;
	}

	public void buildFromString(String input) {
		if (input == null) {
			addNoDataError("no data");
			return;
		}

		//for time-only we support normal delia date strings, and also time strings such as "09:30"
		ZonedDateTime zdt;
		if (getTimeOnlyFlag() && fmtSvc.isTimeOnly(input)) {
			zdt = fmtSvc.parseTime(input);
		} else {
			zdt = fmtSvc.parseDateTime(input);
		}

		if (zdt == null) {
			this.addParsingError(String.format("Can't convert '%s' to date", input), input);
			return;
		}
		
		DateFormatter formatter = fmtSvc.createFormatter(); //always use dfFull
		WrappedDate wdt = new WrappedDate(zdt, formatter);
		this.newDVal = new DValueImpl(type, wdt);
	}

	private boolean getTimeOnlyFlag() {
		return EffectiveShape.EFFECTIVE_TIME_ONLY.equals(type.getEffectiveShape());
	}

	public void buildFrom(ZonedDateTime zdt) {
		if (zdt == null) {
			addNoDataError("no data");
			return;
		}
		
		DateFormatter formatter = fmtSvc.createFormatter();
		WrappedDate wdt = new WrappedDate(zdt, formatter);
		this.newDVal = new DValueImpl(type, wdt);
	}
	public void buildFromLegacy(Date dt) {
		ZonedDateTime zdt = ZonedDateTime.ofInstant(dt.toInstant(), tzSvc.getDefaultTimeZone());
		buildFrom(zdt);
	}

	@Override
	protected void onFinish() {
	}
}