package org.delia.compiler.ast;

import java.util.StringJoiner;

import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;

public class RuleExp extends ExpBase {
	public Exp opExpr;
	//public List<QueryFuncExp> qfelist = new ArrayList<>();

	public RuleExp(int pos, Exp opExp) {
		super(pos);
		this.opExpr = opExp;
	}
	
	@Override
	public String strValue() {
		if (opExpr instanceof FilterOpExp) {
			FilterOpExp foe = (FilterOpExp) opExpr;
			String s1 = formatValue(foe.op1);
			String s2 = formatValue(foe.op2);
			String s = String.format("%s %s %s", s1, foe.op, s2);
			return s;
		} else if (opExpr instanceof XNAFMultiExp) {
			XNAFMultiExp rfe = (XNAFMultiExp) opExpr;
			StringJoiner joiner = new StringJoiner(",");
			
			if (rfe.qfeL.size() == 2) {
				XNAFSingleExp qfe0 = rfe.qfeL.get(0);
				XNAFSingleExp qfe1 = rfe.qfeL.get(1);
				if (!qfe0.isRuleFn && qfe1.isRuleFn) {
					String ss = String.format("%s.%s(", qfe0.funcName, qfe1.funcName);
					StringJoiner j2 = new StringJoiner(",");
					for(Exp x: qfe1.argL) {
						j2.add(x.strValue());
					}
					ss += j2.toString();
					ss += ")";
					joiner.add(ss);
				}
			} else {
				for(XNAFSingleExp qfe: rfe.qfeL) {
					joiner.add(qfe.funcName);
					//!!add args later
					joiner.add("()");
				}
			}
			
			return joiner.toString();
		} else {
			return "";
		}
	}
	
	@Override
	public String toString() {
		return strValue();
	}
}