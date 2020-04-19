package org.delia.compiler.ast;

import org.codehaus.jparsec.Token;

public class FieldQualifierExp extends StringExp {

	public FieldQualifierExp(int pos, Token tok) {
		super((String)tok.toString());
	}
	
	@Override
	public String toString() {
		return strValue();
	}
}