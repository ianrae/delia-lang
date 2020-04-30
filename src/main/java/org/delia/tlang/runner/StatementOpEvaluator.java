package org.delia.tlang.runner;

import org.delia.db.memdb.filter.OP;
import org.delia.db.memdb.filter.OpEvaluator;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class StatementOpEvaluator implements OpEvaluator {
	protected OP op;
	protected Object rightVar;
	protected boolean negFlag;
	private TLangRunner tlangRunner;
	private DValueOpEvaluator dvalEvaluator;
	private TLangStatement leftStatement;

	public StatementOpEvaluator(OP op) {
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
		TLangStatement leftval = leftStatement;
		TLangStatement rightval = (TLangStatement) rightVar;
		
		TLangResult res1 = tlangRunner.executeOne(leftval, null);
		TLangResult res2 = tlangRunner.executeOne(rightval, null);
		if (!res1.ok || !res2.ok) {
			DeliaExceptionHelper.throwError("bad-if-condition", "bad if condition");
		}
		
		DValue dval1 = (DValue) res1.val;
		DValue dval2 = (DValue) res2.val;
		
		dvalEvaluator.setRightVar(dval2);
		return dvalEvaluator.match(dval1);
	}
	
	@Override
	public void setRightVar(Object rightVar) {
		this.rightVar = rightVar;
	}
	@Override
	public void setNegFlag(boolean negFlag) {
		this.negFlag = negFlag;
	}

	public void setTlangRunner(TLangRunner tlangRunner) {
		this.tlangRunner = tlangRunner;
	}

	public void setDvalEvaluator(DValueOpEvaluator dvalEvaluator) {
		this.dvalEvaluator = dvalEvaluator;
	}

	public void setLeftStatement(TLangStatement stat1) {
		this.leftStatement = stat1;
	}
}