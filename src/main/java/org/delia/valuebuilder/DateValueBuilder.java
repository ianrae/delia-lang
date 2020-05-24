package org.delia.valuebuilder;

import java.time.ZonedDateTime;
import java.util.Date;

import org.delia.core.DateFormatService;
import org.delia.core.DateFormatter;
import org.delia.core.FactoryService;
import org.delia.core.TimeZoneService;
import org.delia.type.DType;
import org.delia.type.DValueImpl;
import org.delia.type.Shape;
import org.delia.type.WrappedDate;

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

		ZonedDateTime zdt = null;
		//TODO: fix!!
		zdt = fmtSvc.parseDateTime(input);
		if (zdt == null) {
			this.addParsingError(String.format("Can't convert '%s' to date", input), input);
			return;
		}
		
//		DateFormatter formatter = fmtSvc.createFormatter(input);
		DateFormatter formatter = fmtSvc.createFormatter(); //always use dfFull
		WrappedDate wdt = new WrappedDate(zdt, formatter);
		this.newDVal = new DValueImpl(type, wdt);
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