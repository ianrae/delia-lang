package org.delia.tlang.statement;

import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.tlang.runner.TLangStatement;
import org.delia.type.DValue;

public abstract class TLangStatementBase implements TLangStatement {
	protected String name;
	
	public TLangStatementBase(String name) {
		this.name = name;
	}
	@Override
	public abstract void execute(DValue value, TLangResult result, TLangContext ctx);
	@Override
	public boolean evalCondition(DValue dval) {
		return true;
	}
	@Override
	public String getName() {
		return name;
	}
}