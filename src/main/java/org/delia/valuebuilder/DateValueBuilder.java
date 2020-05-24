package org.delia.valuebuilder;

import java.util.Date;

import org.delia.core.DateFormatService;
import org.delia.core.DateFormatter;
import org.delia.core.FactoryService;
import org.delia.type.DType;
import org.delia.type.DValueImpl;
import org.delia.type.Shape;
import org.delia.type.WrappedDate;

public class DateValueBuilder extends DValueBuilder {
	
	private FactoryService factorySvc;
	private DateFormatService fmtSvc;

	public DateValueBuilder(FactoryService factorySvc, DType type) {
		this.factorySvc = factorySvc;
		this.fmtSvc = factorySvc.getDateFormatService();
		
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

		Date dt = null;
		//TODO: fix!!
		dt = fmtSvc.parse(input);
		if (dt == null) {
			this.addParsingError(String.format("Can't convert '%s' to date", input), input);
			return;
		}
		
//		DateFormatter formatter = fmtSvc.createFormatter(input);
		DateFormatter formatter = fmtSvc.createFormatter(); //always use dfFull
		WrappedDate wdt = new WrappedDate(dt, formatter);
		this.newDVal = new DValueImpl(type, wdt);
	}
	public void buildFrom(Date dt) {
		if (dt == null) {
			addNoDataError("no data");
			return;
		}
		
		DateFormatter formatter = fmtSvc.createFormatter();
		WrappedDate wdt = new WrappedDate(dt, formatter);
		this.newDVal = new DValueImpl(type, wdt);
	}

	@Override
	protected void onFinish() {
	}
}