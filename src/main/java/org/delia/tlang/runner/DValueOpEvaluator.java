package org.delia.tlang.runner;

import org.delia.db.memdb.filter.OP;
import org.delia.db.memdb.filter.OpEvaluator;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;

public class DValueOpEvaluator implements OpEvaluator {
	protected OP op;
	protected Object rightVar;
	protected boolean negFlag;
	private EvalSpec innerEval;

	public DValueOpEvaluator(OP op) {
		this.op = op;
	}
	
	@Override
	public boolean match(Object left) {
		boolean b = doMatch(left);
		if (negFlag) {
			return !b;
		} else {
			return b;
		}
	}
	protected boolean doMatch(Object left) {
		DValue leftval = (DValue) left;
		DValue rightval = (DValue) rightVar;
		
		if (innerEval == null) {
			innerEval = createInnerEval(leftval, rightval);
		} else {
			innerEval.left = left;
		}
		
		return innerEval.execute();
	}
	
	private EvalSpec createInnerEval(DValue leftval, DValue rightval) {
		Shape shape = leftval.getType().getShape();
		switch(shape) {
		case INTEGER:
		{
			EvalSpec espec = new EvalSpec();
			espec.op = op;
			espec.left = leftval.asInt();
			espec.right = rightval.asInt();
			return espec;
		}
		case LONG:
		{
			EvalSpec espec = new EvalSpec();
			espec.op = op;
			espec.left = leftval.asLong();
			espec.right = rightval.asLong();
			return espec;
		}
		case NUMBER:
		{
			EvalSpec espec = new EvalSpec();
			espec.op = op;
			espec.left = leftval.asNumber();
			espec.right = rightval.asNumber();
			return espec;
		}
		case BOOLEAN:
		{
			EvalSpec espec = new EvalSpec();
			espec.op = op;
			espec.left = leftval.asBoolean();
			espec.right = rightval.asBoolean();
			return espec;
		}
		case DATE:
		{
			EvalSpec espec = new EvalSpec();
			espec.op = op;
			espec.left = leftval.asDate();
			espec.right = rightval.asDate();
			return espec;
		}
		case STRING:
		{
			EvalSpec espec = new EvalSpec();
			espec.op = op;
			espec.left = leftval.asString();
			espec.right = rightval.asString();
			return espec;
		}
		default:
			DeliaExceptionHelper.throwError("tlang-unsupported-shape", "TLANG unsupported shape: %s", shape.name());
			return null;
		}
	}

	@Override
	public void setRightVar(Object rightVar) {
		this.rightVar = rightVar;
	}
	@Override
	public void setNegFlag(boolean negFlag) {
		this.negFlag = negFlag;
	}
}