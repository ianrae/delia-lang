package org.delia.tlang.statement;

import org.delia.tlang.runner.TLangContext;
import org.delia.tlang.runner.TLangResult;
import org.delia.type.DValue;

public class ValueStatement extends TLangStatementBase {
	private DValue dval;
	public ValueStatement(DValue dval) {
		super("value");
		this.dval = dval;
	}
	@Override
	public void execute(DValue value, TLangResult result, TLangContext ctx) {
		result.val = dval;
	}
	public DValue getDval() {
		return dval;
	}

}