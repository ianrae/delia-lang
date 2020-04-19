package org.delia.db.memdb.filter;

import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.type.DValue;

public class NumberOpEvaluator extends OpEvaluatorBase {

	public NumberOpEvaluator(OP op, String fieldName) {
		super(op, fieldName);
	}

	@Override
	protected boolean doMatch(Object left) {
		DValue dval = (DValue) left;
		Boolean b = checkNull(dval, rightVar);
		if (b != null) {
			return b;
		}
		Double n1 = getFieldValue(dval).asNumber();
		Double n2;
		//auto-promote int values
		if (rightVar instanceof IntegerExp) {
			n2 = ((IntegerExp)rightVar).val.doubleValue();
		} else if (rightVar instanceof LongExp) {
			n2 = ((LongExp)rightVar).val.doubleValue();
		} else {
			n2 = ((NumberExp)rightVar).val;
		}

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