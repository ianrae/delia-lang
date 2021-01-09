package org.delia.db.newhls.cond;

import org.delia.compiler.ast.IntegerExp;
import org.delia.db.newhls.ValType;

public class IntFilterCond extends SingleFilterCond  {
	public IntFilterCond(IntegerExp exp) {
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