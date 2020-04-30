package org.delia.tlang.runner;

import org.delia.db.memdb.filter.OpEvaluator;
import org.delia.type.DValue;

public class OpCondition implements Condition {
	private OpEvaluator evaluator;

	public OpCondition(OpEvaluator evaluator) {
		this.evaluator = evaluator;
	}
	@Override
	public boolean eval(DValue dval) {
		return evaluator.match(dval);
	}
	@Override
	public void setTLangRunner(TLangRunner tlangRunner) {
	}
}