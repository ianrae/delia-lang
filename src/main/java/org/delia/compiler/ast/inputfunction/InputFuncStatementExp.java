package org.delia.compiler.ast.inputfunction;

import org.delia.compiler.ast.ExpBase;
import org.delia.compiler.ast.IdentExp;

public class InputFuncStatementExp extends ExpBase {
	public IdentExp inputField;
	public IdentPairExp outputField;

	public InputFuncStatementExp(int pos, IdentExp inputExp, IdentPairExp outputExp) {
		super(pos);
		this.inputField = inputExp;
		this.outputField = outputExp;
	}
	
	@Override
	public String strValue() {
		return String.format("%s -> %s.%s", inputField.name(), outputField.val1, outputField.val2);
	}

	@Override
	public String toString() {
		return strValue();
	}
}