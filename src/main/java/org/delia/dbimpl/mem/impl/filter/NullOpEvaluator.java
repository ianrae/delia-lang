package org.delia.dbimpl.mem.impl.filter;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;
import org.delia.tok.Tok;
import org.delia.type.DValue;

import java.util.List;

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
			DeliaError err = new DeliaError("null-not-allowed-here", msg);
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

	@Override
	public void setFuncs(List<Tok.FunctionTok> funcs) {
		//unused
	}
}