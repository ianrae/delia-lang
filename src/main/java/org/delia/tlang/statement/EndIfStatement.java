package org.delia.tlang.statement;

import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangStatement;
import org.delia.type.DValue;

public class EndIfStatement implements TLangStatement {
	@Override
	public void execute(DValue value, TLangResult result, TLangContext ctx) {
		result.val = value;
	}
	@Override
	public boolean evalCondition(DValue dval) {
		return true;
	}
	@Override
	public String getName() {
		return "endif";
	}
}