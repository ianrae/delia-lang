package org.delia.hld.cond;

import org.delia.compiler.ast.LongExp;
import org.delia.hld.ValType;

public class LongFilterCond extends SingleFilterCond {
	public LongFilterCond(LongExp exp) {
		this.val1 = new FilterVal(ValType.LONG, exp);
	}
	public long asLong() {
		return val1.asLong();
	}
	@Override
	public String renderSql() {
		return String.format("%d", val1.asLong());
	}
}