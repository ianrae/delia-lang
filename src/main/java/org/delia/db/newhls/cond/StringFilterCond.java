package org.delia.db.newhls.cond;

import org.delia.compiler.ast.StringExp;
import org.delia.db.hld.ValType;

public class StringFilterCond extends SingleFilterCond {
	public StringFilterCond(StringExp exp) {
		this.val1 = new FilterVal(ValType.STRING, exp);
	}
	public String asString() {
		return val1.asString();
	}
	@Override
	public String renderSql() {
		return String.format("'%s'", val1.asString()); //TODO: escape ' chars1
	}
}