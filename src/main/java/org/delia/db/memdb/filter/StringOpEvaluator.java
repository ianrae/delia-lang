package org.delia.db.memdb.filter;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.StringExp;
import org.delia.type.DValue;

public class StringOpEvaluator extends OpEvaluatorBase {

	public StringOpEvaluator(OP op, String fieldName) {
		super(op, fieldName);
	}

	@Override
	protected boolean doMatch(Object left) {
		DValue dval = (DValue) left;
		if (op != OP.LIKE) {
			Boolean b = true;
			b = checkNull(dval, rightVar);
			if (b != null) {
				return b;
			}
		} else if (dval.asStruct().getField(fieldName) == null) {
			return false; //SKIP when s1 is null
		}

		String s1 = getFieldValue(dval).asString();
//		String s2 = ((StringExp)rightVar).strValue();
		String s2 = ((Exp)rightVar).strValue(); //rightVar might not be StringExp.

		switch(op) {
		case LT:
			return s1.compareTo(s2) < 0; 
		case LE:
			return s1.compareTo(s2) <= 0; 
		case GT:
			return s1.compareTo(s2) > 0; 
		case GE:
			return s1.compareTo(s2) >= 0; 
		case EQ:
			return s1.compareTo(s2) == 0; 
		case NEQ:
			return s1.compareTo(s2) != 0; 
		case LIKE:
			return doLike(s1, s2);
		default:
			return false; //err!
		}
	}

	private boolean doLike(String s1, String s2) {
		boolean wildStart = s2.startsWith("%");
		boolean wildEnd = s2.endsWith("%");
		
		if (wildStart) {
			s2 = s2.substring(1, s2.length());
		}
		if (wildEnd) {
			s2 = s2.substring(0, s2.length() - 1);
		}

		if (wildStart && wildEnd) {
			return s1.contains(s2);
		} else if (wildStart) {
			return s1.endsWith(s2);
		} else if (wildEnd) {
			return s1.startsWith(s2);
		} else {
			return s1.contains(s2);
		}
		//TODO fully implement this later!1
	}
}