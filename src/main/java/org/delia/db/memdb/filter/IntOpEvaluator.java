package org.delia.db.memdb.filter;

import org.delia.compiler.ast.IntegerExp;
import org.delia.type.DValue;

public class IntOpEvaluator extends OpEvaluatorBase {

	public IntOpEvaluator(OP op, String fieldName) {
		super(op, fieldName);
	}

	@Override
	protected boolean doMatch(Object left) {
		DValue dval = (DValue) left;
		Boolean b = checkNull(dval, rightVar);
		if (b != null) {
			return b;
		}
		Integer n1 = getFieldValue(dval).asInt();
		Integer n2 = ((IntegerExp)rightVar).val;

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