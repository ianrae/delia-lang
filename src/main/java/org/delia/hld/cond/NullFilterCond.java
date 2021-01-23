package org.delia.hld.cond;

import org.delia.compiler.ast.NullExp;
import org.delia.hld.ValType;

public class NullFilterCond extends SingleFilterCond {
	public NullFilterCond(NullExp exp) {
		this.val1 = new FilterVal(ValType.NULL, exp);
	}
	
	@Override
	public String renderSql() {
		return String.format("%s", val1.asString());
	}
}