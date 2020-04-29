package org.delia.tlang.runner;

import org.delia.type.DValue;

public interface TLangStatement {
	String getName();
	boolean evalCondition(DValue dval);
	void execute(DValue value, TLangResult result, TLangContext ctx);
}