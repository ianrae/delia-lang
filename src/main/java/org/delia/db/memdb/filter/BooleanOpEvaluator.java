package org.delia.db.memdb.filter;

import org.delia.compiler.ast.BooleanExp;
import org.delia.type.DValue;

public class BooleanOpEvaluator extends OpEvaluatorBase {

	public BooleanOpEvaluator(OP op, String fieldName) {
		super(op, fieldName);
	}

	@Override
	protected boolean doMatch(Object left) {
		DValue dval = (DValue) left;
		Boolean b = checkNull(dval, rightVar);
		if (b != null) {
			return b;
		}
		Boolean n1 = getFieldValue(dval).asBoolean();
		Boolean n2 = ((BooleanExp)rightVar).val;

		switch(op) {
		case EQ:
			return n1.compareTo(n2) == 0; 
		case NEQ:
			return n1.compareTo(n2) != 0; 
		default:
			return false; //err!
		}
	}
}