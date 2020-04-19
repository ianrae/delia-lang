package org.delia.db.memdb.filter;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;
import org.delia.type.DValue;

public class NullOpEvaluator implements OpEvaluator {
	private String fieldName;
	private OP op;
	private Object rightVar;

	public NullOpEvaluator(OP op, String fieldName) {
		this.op = op;
		this.fieldName = fieldName;
	}

	@Override
	public boolean match(Object left) {
		DValue dval = (DValue) left;
		//TODO: can fieldName be __SCALAR here??
		String s1 = dval.asStruct().getField(fieldName).asString();

		if (op.equals("=")) {
			return s1 == null;
		} else if (op.equals("!=")) { //TODO and <>
			return s1 != null;
		} else {
			String msg = String.format("Can't use null with %s operand", op);
			DeliaError err = new DeliaError("null-not-allowed-here", msg, null);
			throw new DeliaException(err);
		}
	}

	@Override
	public void setRightVar(Object rightVar) {
		this.rightVar = rightVar;
	}

	@Override
	public void setNegFlag(boolean negFlag) {
		//unused
	}
}