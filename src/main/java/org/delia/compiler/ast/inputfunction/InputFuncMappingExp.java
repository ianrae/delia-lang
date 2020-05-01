package org.delia.compiler.ast.inputfunction;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.ExpBase;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;

public class InputFuncMappingExp extends ExpBase {
	public Exp inputField;
	public IdentPairExp outputField;
	public TLangBodyExp tlangBody;

	public InputFuncMappingExp(int pos, Exp inputExp, IdentPairExp outputExp, TLangBodyExp tlangBody) {
		super(pos);
		this.inputField = inputExp;
		this.outputField = outputExp;
		this.tlangBody = tlangBody;
	}
	
	public String getInputField() {
		if (inputField instanceof StringExp) {
			StringExp sexp = (StringExp) inputField;
			return sexp.strValue();
		}
		XNAFMultiExp multiExp = (XNAFMultiExp) inputField;
		return multiExp.qfeL.get(0).funcName;
	}
	
	@Override
	public String strValue() {
		return String.format("%s -> %s.%s", inputField.strValue(), outputField.val1, outputField.val2);
	}

	@Override
	public String toString() {
		return strValue();
	}
}