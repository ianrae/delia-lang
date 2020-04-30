package org.delia.compiler.ast.inputfunction;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.ExpBase;

public class IfStatementExp extends ExpBase {

	public Exp condition;
	public boolean isIf; //true means if, false means elseif
	public boolean isIfReturn;
	public Exp returnStatement;

	public IfStatementExp(int pos, Exp cond, boolean isIf, boolean isIfReturn, Exp returnStatement) {
		super(pos);
		this.condition = cond;
		this.isIf = isIf; 
		this.isIfReturn = isIfReturn;
		this.returnStatement = returnStatement;
	}
	
	@Override
	public String strValue() {
		return String.format("if %s then", condition.strValue());
	}

	@Override
	public String toString() {
		return strValue();
	}
}