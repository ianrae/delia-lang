package org.delia.db.newhls.cond;

import org.delia.compiler.ast.BooleanExp;
import org.delia.db.hld.ValType;

public class BooleanFilterCond extends SingleFilterCond {
	public BooleanFilterCond(BooleanExp exp) {
		this.val1 = new FilterVal(ValType.BOOLEAN, exp);
	}
	
	public boolean asBoolean() {
		return val1.asBoolean();
	}
	@Override
	public String renderSql() {
		return String.format("%b", val1.asBoolean());
	}
}