package org.delia.tlang.statement;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.tlang.runner.TLangContext;

public class SubstringStatement extends StringStatement {
	private Integer arg1;
	private Integer arg2;
	
	public SubstringStatement(Exp arg1, Exp arg2) {
		super("substring");
		IntegerExp nexp = (IntegerExp) arg1;
		this.arg1 = nexp.val;
		
		if (arg2 != null) {
			nexp = (IntegerExp) arg2;
			this.arg2 = nexp.val;
		}
	}
	@Override
	protected String executeStr(String s, TLangContext ctx) {
		if (arg2 == null) {
			return s.substring(arg1);
		} else {
			return s.substring(arg1, arg2);
		}
	}
}