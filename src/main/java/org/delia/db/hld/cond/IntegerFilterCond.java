package org.delia.db.hld.cond;

import org.delia.compiler.ast.IntegerExp;
import org.delia.db.hld.ValType;

public class IntegerFilterCond extends SingleFilterCond  {
	public IntegerFilterCond(IntegerExp exp) {
		this.val1 = new FilterVal(ValType.INT, exp);
	}
	public int asInt() {
		return val1.asInt();
	}
	@Override
	public String renderSql() {
		return String.format("%d", val1.asInt());
	}
}