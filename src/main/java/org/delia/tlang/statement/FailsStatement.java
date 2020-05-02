package org.delia.tlang.statement;

import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.type.DValue;

public class FailsStatement extends TLangStatementBase {
	
	public FailsStatement() {
		super("fail");
	}

	@Override
	public void execute(DValue value, TLangResult result, TLangContext ctx) {
		ctx.failFlag = true;
	}
}