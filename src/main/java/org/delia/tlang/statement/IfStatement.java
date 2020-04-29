package org.delia.tlang.statement;

import org.delia.tlang.runner.Condition;
import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangStatement;
import org.delia.type.DValue;

public class IfStatement implements TLangStatement {
	public Condition cond;

	public IfStatement(Condition cond) {
		this.cond = cond;
	}
	@Override
	public void execute(DValue value, TLangResult result, TLangContext ctx) {
		result.val = value;
	}
	@Override
	public boolean evalCondition(DValue dval) {
		return cond.eval(dval);
	}
	@Override
	public String getName() {
		return "if";
	}
}