package org.delia.compiler.ast;

public class QueryFieldExp extends QueryFuncExp {

	public QueryFieldExp(int pos, IdentExp nameExp) {
		super(pos, nameExp, null, false);
	}
	
	@Override
	public String strValue() {
		return funcName;
	}

	@Override
	public String toString() {
		return String.format(".%s", funcName);
	}
}