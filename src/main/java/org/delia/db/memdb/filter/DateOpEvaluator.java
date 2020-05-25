package org.delia.db.memdb.filter;

import java.time.ZonedDateTime;

import org.delia.compiler.ast.StringExp;
import org.delia.core.DateFormatService;
import org.delia.core.DateFormatServiceImpl;
import org.delia.core.TimeZoneService;
import org.delia.core.TimeZoneServiceImpl;
import org.delia.type.DValue;

public class DateOpEvaluator extends OpEvaluatorBase {
	//TODO: need to inject tzSvc!!
	TimeZoneService tzSvc = new TimeZoneServiceImpl();
	private DateFormatService fmtSvc = new DateFormatServiceImpl(tzSvc);

	public DateOpEvaluator(OP op, String fieldName) {
		super(op, fieldName);
	}

	@Override
	protected boolean doMatch(Object left) {
		DValue dval = (DValue) left;
		Boolean b = checkNull(dval, rightVar);
		if (b != null) {
			return b;
		}
		ZonedDateTime n1 = getFieldValue(dval).asDate();
		String s = ((StringExp)rightVar).strValue();
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