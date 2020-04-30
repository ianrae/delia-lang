package org.delia.tlang.statement;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.StringExp;
import org.delia.tlang.runner.TLangContext;

public class CombineStatement extends StringStatement {
	private Exp arg1;
	private Exp arg2;
	private Exp arg3;
	
	public CombineStatement(Exp arg1, Exp arg2, Exp arg3) {
		super("combine");
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
	}
	@Override
	protected String executeStr(String s, TLangContext ctx) {
		String s1 = evalToString(arg1, ctx);
		String s2 = evalToString(arg2, ctx);
		String s3 = evalToString(arg3, ctx);
		return String.format("%s%s%s", s1, s2, s3);
	}
	private String evalToString(Exp exp, TLangContext ctx) {
		if (exp instanceof IdentExp) {
			IdentExp iexp = (IdentExp) exp;
			String inputColumn = iexp.val;
			String value = ctx.inputDataMap.get(inputColumn);
			return value;
		} else if (exp instanceof StringExp) {
			StringExp sexp = (StringExp) exp;
			return sexp.strValue();
		} else {
			return "";
		}
	}
}