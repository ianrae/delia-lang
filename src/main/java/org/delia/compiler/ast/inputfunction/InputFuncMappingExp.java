package org.delia.compiler.ast.inputfunction;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.ExpBase;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.compiler.astx.XNAFSingleExp;

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
	public boolean isValidInputField() {
		if (inputField instanceof StringExp) {
			return true;
		} else if (isSyntheticInputField()) {
			return true;
		} else {
			XNAFSingleExp sexp = getSingleExp();
			if (sexp instanceof XNAFNameExp) {
				return true;
			}
		}
		return false;
	}
	public XNAFSingleExp getSingleExp() {
		if (inputField instanceof XNAFMultiExp) {
			XNAFMultiExp multiExp = (XNAFMultiExp) inputField;
			if (multiExp.qfeL.size() == 1) {
				XNAFSingleExp exp1 = multiExp.qfeL.get(0);
				return exp1;
			}
		}
		return null;
	}

	public boolean isSyntheticInputField() {
		XNAFSingleExp sexp = getSingleExp();
		if (sexp != null) {
			if (sexp.isRuleFn) {
				return sexp.funcName.equals("value");
			}
		}
		return false;
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