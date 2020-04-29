package org.delia.tlang.statement;

import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.type.DValue;

public abstract class StringStatement extends TLangStatementBase {
	public StringStatement(String name) {
		super(name);
	}
	@Override
	public void execute(DValue value, TLangResult result, TLangContext ctx) {
		String s = value.asString();
		s = executeStr(s, ctx);
		result.val = ctx.builder.buildString(s);
	}
	protected abstract String executeStr(String s, TLangContext ctx);
}