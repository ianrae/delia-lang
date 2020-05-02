package org.delia.compiler.ast.inputfunction;

import org.delia.compiler.ast.ExpBase;

public class EndIfStatementExp extends ExpBase {

	public EndIfStatementExp(int pos) {
		super(pos);
	}
	
	@Override
	public String strValue() {
		return String.format("endif");
	}

	@Override
	public String toString() {
		return strValue();
	}
}