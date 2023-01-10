package org.delia.dbimpl.mem.impl.filter;

import org.delia.core.DateFormatService;
import org.delia.core.TimeZoneService;
import org.delia.core.TimeZoneServiceImpl;
import org.delia.dbimpl.mem.impl.filter.filterfn.FilterFunctionService;
import org.delia.tok.Tok;
import org.delia.type.DValue;

import java.time.ZonedDateTime;

public class DateOpEvaluator extends OpEvaluatorBase {
	TimeZoneService tzSvc = new TimeZoneServiceImpl();
	private DateFormatService fmtSvc;

	public DateOpEvaluator(OP op, String fieldName, DateFormatService fmtSvc, FilterFunctionService filterFnSvc) {
		super(op, fieldName, filterFnSvc);
		this.fmtSvc = fmtSvc;
		this.tzSvc = fmtSvc.getTimezoneService();
	}

	@Override
	protected boolean doMatch(Object left) {
		DValue dval = (DValue) left;
		Boolean b = checkNull(dval, rightVar);
		if (b != null) {
			return b;
		}
		ZonedDateTime n1 = getFieldValue(dval).asDate();
		String s = ((Tok.DToken)rightVar).strValue();
		ZonedDateTime n2 = fmtSvc.parseDateTime(s);
		
		switch(op) {
		case LT:
			return n1.compareTo(n2) < 0; 
		case LE:
			return n1.compareTo(n2) <= 0; 
		case GT:
			return n1.compareTo(n2) > 0; 
		case GE:
			return n1.compareTo(n2) >= 0; 
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			return false; //err!
		}
	}
}