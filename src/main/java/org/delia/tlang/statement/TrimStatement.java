package org.delia.tlang.statement;

import org.delia.tlang.runner.TLangContext;

public class TrimStatement extends StringStatement {
	public TrimStatement() {
		super("trim");
	}
	@Override
	protected String executeStr(String s, TLangContext ctx) {
		return s.trim();
	}
}