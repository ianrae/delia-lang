package org.delia.compiler.parser;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.db.QuerySpec;

public class FilterHelper {
	public static boolean isFilterExpression(QuerySpec spec) {
		if (spec.queryExp.filter.cond instanceof FilterOpFullExp) {
			return true;
		}
		return false;
	}
	public static boolean isFilterAllRows(QuerySpec spec) {
		if (spec.queryExp.filter.cond instanceof BooleanExp) {
			return true;
		}
		return false;
	}

	//extracts the first fieldname, such as x==55 or 55==x. will return 'x'
	public static String extractFieldNameFromFilterExpression(QuerySpec spec) {
		if (spec.queryExp.filter.cond instanceof FilterOpFullExp) {
			FilterOpFullExp fullexp = (FilterOpFullExp) spec.queryExp.filter.cond;
			if (fullexp.opexp1 instanceof FilterOpExp) {
				FilterOpExp foexp = (FilterOpExp) fullexp.opexp1;
				String fieldName = extractFieldNameFromFilterOp(foexp.op1);
				if (fieldName != null) {
					return fieldName;
				}
				return extractFieldNameFromFilterOp(foexp.op2);
			}
		}
		return null;
	}

	public static String extractFieldNameFromFilterOp(Exp op1) {
		if (op1 instanceof XNAFMultiExp) {
			XNAFMultiExp xx = (XNAFMultiExp) op1;
			if (!xx.qfeL.isEmpty() && xx.qfeL.get(0) instanceof XNAFNameExp) {
				XNAFNameExp xne = (XNAFNameExp) xx.qfeL.get(0);
				if (xne.argL.isEmpty()) {
					return xne.funcName;
				}
			}
		}
		return null;
	}

}
