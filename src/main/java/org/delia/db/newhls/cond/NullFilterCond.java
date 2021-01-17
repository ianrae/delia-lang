package org.delia.db.newhls.cond;

import org.delia.compiler.ast.NullExp;
import org.delia.db.newhls.ValType;

public class NullFilterCond extends SingleFilterCond {
	public NullFilterCond(NullExp exp) {
		this.val1 = new FilterVal(ValType.NULL, exp);
	}
	
	@Override
	public String renderSql() {
		return String.format("%s", val1.asString());
	}
}