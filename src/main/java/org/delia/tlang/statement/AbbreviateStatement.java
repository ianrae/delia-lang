package org.delia.tlang.statement;

import org.apache.commons.lang3.StringUtils;
import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.tlang.runner.TLangContext;

public class AbbreviateStatement extends StringStatement {
	private Integer arg1;
	private Boolean arg2;
	
	public AbbreviateStatement(Exp arg1, Exp arg2) {
		super("abbreviate");
		IntegerExp nexp = (IntegerExp) arg1;
		this.arg1 = nexp.val;
		
		if (arg2 != null) {
			BooleanExp bexp = (BooleanExp) arg2;
			this.arg2 = bexp.val;
		}
	}
	@Override
	protected String executeStr(String s, TLangContext ctx) {
		if (arg2 == null || !arg2.booleanValue()) {
			if (s.length() > arg1) {
				return s.substring(0, arg1);
			} else {
				return s;
			}
		} else {
			return StringUtils.abbreviate(s, "...", arg1); //with
		}
	}
}