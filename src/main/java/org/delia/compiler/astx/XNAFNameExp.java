package org.delia.compiler.astx;

import org.delia.compiler.ast.IdentExp;

public class XNAFNameExp extends XNAFSingleExp {

	public XNAFNameExp(int pos, IdentExp nameExp) {
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