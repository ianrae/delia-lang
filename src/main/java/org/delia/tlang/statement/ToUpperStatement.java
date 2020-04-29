package org.delia.tlang.statement;

import org.delia.tlang.runner.TLangContext;

public class ToUpperStatement extends StringStatement {
	public ToUpperStatement() {
		super("toUpperCase");
	}
	@Override
	protected String executeStr(String s, TLangContext ctx) {
		return s.toUpperCase();
	}
}