package org.delia.tlang.statement;

import org.apache.commons.lang3.StringUtils;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.StringExp;
import org.delia.tlang.runner.TLangContext;

public class SubstringAfterStatement extends StringStatement {
	private String arg1;
	
	public SubstringAfterStatement(Exp arg1) {
		super("substringBefore");
		StringExp nexp = (StringExp) arg1;
		this.arg1 = nexp.val;
	}
	@Override
	protected String executeStr(String s, TLangContext ctx) {
		return StringUtils.substringAfter(s, arg1);
	}
}