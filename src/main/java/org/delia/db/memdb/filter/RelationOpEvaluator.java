package org.delia.db.memdb.filter;

import org.delia.compiler.ast.IntegerExp;
import org.delia.type.DRelation;
import org.delia.type.DValue;

public class RelationOpEvaluator extends OpEvaluatorBase {

	public RelationOpEvaluator(OP op, String fieldName) {
		super(op, fieldName);
	}

	@Override
	protected boolean doMatch(Object left) {
		DValue dval = (DValue) left;
	      Boolean b = checkNull(dval, rightVar);
	      if (b != null) {
	        return b;
	      }
		DRelation drel1 = dval.asStruct().getField(fieldName).asRelation();
		if (drel1.isMultipleKey()) {
			for(DValue kk: drel1.getMultipleKeys()) {
				if (doInnerMatch(kk)) {
					return true;
				}
			}
			return false;
		}
		DValue keyVal = drel1.getForeignKey();
		return doInnerMatch(keyVal);
	}

	private boolean doInnerMatch(DValue keyVal) {
		Integer n1 = keyVal.asInt(); //TODO: string later
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