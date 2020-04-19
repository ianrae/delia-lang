package org.delia.compiler.ast;

import java.util.List;

public class QueryInExp extends ExpBase {
	public ListExp listExp;
	public String fieldName;

	public QueryInExp(int pos, IdentExp fieldName, List<List<Exp>> args) {
		super(pos);
		this.fieldName = fieldName.name();
		this.listExp = new ListExp(pos, args);
	}
	
	
	@Override
	public String strValue() {
		String s = String.format("%s in %s", fieldName, listExp.strValue());
		return s;
	}

	@Override
	public String toString() {
		return strValue();
	}
}