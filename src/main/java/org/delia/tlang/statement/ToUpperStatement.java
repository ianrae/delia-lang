package org.delia.tlang.statement;

import org.delia.tlang.runner.TLangContext;

public class ToUpperStatement extends StringStatement {
	private boolean b;
	public ToUpperStatement(boolean b) {
		super("toUpperCase");
		this.b = b;
	}
	@Override
	protected String executeStr(String s, TLangContext ctx) {
		return (b) ? s.toUpperCase() : s.toLowerCase();
	}
}