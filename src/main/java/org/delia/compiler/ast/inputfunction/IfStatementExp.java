package org.delia.compiler.ast.inputfunction;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.ExpBase;

public class IfStatementExp extends ExpBase {

	public Exp condition;
	public Exp statement;

	public IfStatementExp(int pos, Exp cond, Exp statement) {
		super(pos);
		this.condition = cond;
		this.statement = statement;
	}
	
	@Override
	public String strValue() {
		return String.format("if %s then %s", condition.strValue(), statement.strValue());
	}

	@Override
	public String toString() {
		return strValue();
	}
}