package org.delia.db.memdb.filter.filterfn;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DateUtils;
import org.delia.valuebuilder.ScalarValueBuilder;

public class FilterFnRunner {
	private DTypeRegistry registry;

	public FilterFnRunner(DTypeRegistry registry) {
		this.registry = registry;
	}
	
	public boolean isDateFn(String fnName) {
		List<String> allFns = Arrays.asList("year", "month", "day", "hour", "minute", "second");
		return allFns.contains(fnName);
	}

	public DValue executeFilterFn(XNAFMultiExp multiexp, DValue fieldval) {
		XNAFSingleExp exp = multiexp.qfeL.get(1);
		String fnName = exp.funcName;
		
		switch(fnName) {
		case "year":
			return execYear(multiexp, fieldval);
		case "month":
			return execMonth(multiexp, fieldval);
		case "day":
			return execDate(multiexp, fieldval);
		case "hour":
			return execHour(multiexp, fieldval);
		case "minute":
			return execMinute(multiexp, fieldval);
		case "second":
			return execSecond(multiexp, fieldval);
		default:
		{
			DeliaError err = new DeliaError("filterfn-unknown", String.format("unknown filterfn '%s'", fnName));
			throw new DeliaException(err);
		}
		}
	}

	private DValue execMinute(XNAFMultiExp multiexp, DValue fieldval) {
		LocalDateTime ldt = convertDate(fieldval); 
		int n = ldt.getMinute();
		return buildIntDVal(n);
	}
	private DValue execSecond(XNAFMultiExp multiexp, DValue fieldval) {
		LocalDateTime ldt = convertDate(fieldval); 
		int n = ldt.getSecond();
		return buildIntDVal(n);
	}

	private DValue execHour(XNAFMultiExp multiexp, DValue fieldval) {
		LocalDateTime ldt = convertDate(fieldval); 
		int n = ldt.getHour(); //1-24
		return buildIntDVal(n);
	}

	private DValue execMonth(XNAFMultiExp multiexp, DValue fieldval) {
		LocalDateTime ldt = convertDate(fieldval); 
		int n = ldt.getMonthValue();
		return buildIntDVal(n);
	}

	private DValue execYear(XNAFMultiExp multiexp, DValue fieldval) {
		LocalDateTime ldt = convertDate(fieldval); 
		int n = ldt.getYear();
		return buildIntDVal(n);
	}

	private DValue execDate(XNAFMultiExp multiexp, DValue fieldval) {
		LocalDateTime ldt = convertDate(fieldval); 
		int n = ldt.getDayOfMonth();
		return buildIntDVal(n);
	}

	private DValue buildIntDVal(int n) {
		ScalarValueBuilder builder = new ScalarValueBuilder(null, registry); //don't need factorysvc for ints
		return builder.buildInt(n);
	}

	private LocalDateTime convertDate(DValue fieldval) {
		Date dt = fieldval.asDate();
		//TODO: need to take proper timezone into effect!1
		LocalDateTime ldt = DateUtils.convertToUTCLocalTime(dt);
		return ldt;
	}


}