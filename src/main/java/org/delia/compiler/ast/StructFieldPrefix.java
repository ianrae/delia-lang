package org.delia.compiler.ast;

import org.jparsec.Token;

/**
 * Only used during parsing to build a StructFieldExp. Not kept during runtime.
 */
public class StructFieldPrefix extends ExpBase {
	public IdentExp nameExp;
	public IdentExp exp;
	public boolean isRelation;
	public String relationName;

	public StructFieldPrefix(int pos, Token tokRelation, IdentExp name, IdentExp exp, StringExp relationNameExp) {
		super(pos);
		this.nameExp = name;
		this.exp = exp;
		this.isRelation = (tokRelation != null);
		this.relationName = relationNameExp == null ? null : relationNameExp.strValue();
	}
	
	@Override
	public String strValue() {
		return null;
	}

	@Override
	public String toString() {
		return strValue();
	}
}