package org.delia.compiler.ast.inputfunction;

import org.delia.compiler.ast.ExpBase;

public class IdentPairExp extends ExpBase {
	public String val1;
	public String val2;

	public IdentPairExp(int pos, String s1, String s2) {
		super(pos);
		this.val1 = s1;
		this.val2 = s2;
	}
	
	public String typeName() {
		return val1;
	}
	public String argName() {
		return val2;
	}

	@Override
	public String strValue() {
		return String.format("%s %s", val1, val2);
	}
}