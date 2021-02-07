package org.delia.compiler.ast;

import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;

public class FilterOpExp extends ExpBase {
	public Exp op1;
	public String op;
	public Exp op2;

	public FilterOpExp(int pos, Exp op1, StringExp op, Exp op2) {
		super(pos);
		this.op1 = op1;
		this.op = op == null ? null : op.strValue();
		this.op2 = op2;
	}
	@Override
	public String strValue() {
		return op;
	}
	
	@Override
	public String toString() {
		String s1 = formatValue(op1);
		String s2 = formatValue(op2);
		String s = String.format("%s %s %s", s1, op, s2);
		return s;
	}
	public Exp getFirstArg() {
		if (op1 instanceof XNAFMultiExp) {
			XNAFMultiExp multiexp = (XNAFMultiExp) op1;
			if (multiexp.qfeL.size() == 1) {
				XNAFSingleExp inner = multiexp.qfeL.get(0);
				if (inner.isSimpleField()) {
					return new IdentExp(inner.funcName);
				}
			}
		}
		return op1;
	}
	public Exp getSecondArg() {
		if (op2 instanceof XNAFMultiExp) {
			XNAFMultiExp multiexp = (XNAFMultiExp) op2;
			if (multiexp.qfeL.size() == 1) {
				XNAFSingleExp inner = multiexp.qfeL.get(0);
				if (inner.isSimpleField()) {
					return new IdentExp(inner.funcName);
				}
			}
		}
		return op2;
	}
	
}