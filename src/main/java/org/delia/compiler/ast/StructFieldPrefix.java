package org.delia.compiler.ast;

import org.codehaus.jparsec.Token;

/**
 * Only used during parsing to build a StructFieldExp. Not kept during runtime.
 */
public class StructFieldPrefix extends ExpBase {
	public IdentExp nameExp;
	public IdentExp exp;
	public boolean isRelation;

	public StructFieldPrefix(int pos, Token tokRelation, IdentExp name, IdentExp exp) {
		super(pos);
		this.nameExp = name;
		this.exp = exp;
		this.isRelation = (tokRelation != null);
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