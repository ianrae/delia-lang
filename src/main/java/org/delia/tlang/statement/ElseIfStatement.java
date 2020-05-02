package org.delia.tlang.statement;

import org.delia.tlang.runner.Condition;
import org.delia.tlang.runner.NeedsTLangRunner;
import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangRunner;
import org.delia.tlang.runner.TLangStatement;
import org.delia.type.DValue;

public class ElseIfStatement implements TLangStatement, NeedsTLangRunner {
	public Condition cond;
	public ElseIfStatement(Condition cond) {
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
		return "elseif";
	}
	@Override
	public void setTLangRunner(TLangRunner tlangRunner) {
		this.cond.setTLangRunner(tlangRunner);
	}
}