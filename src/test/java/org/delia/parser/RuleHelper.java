package org.delia.parser;

import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.RuleExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;

public class RuleHelper {

	public static void chkOpRule(RuleExp exp, String op1, String op, String op2) {
		FilterOpExp foe = (FilterOpExp) exp.opExpr;
		assertEquals(op1, foe.op1.strValue());
		assertEquals(op, foe.op);
		assertEquals(op2, foe.op2.strValue());
	}
	public static void chkFuncRule(RuleExp exp, int n, String fnName, int numArgs) {
		XNAFMultiExp rfe = (XNAFMultiExp) exp.opExpr;
		assertEquals(n, rfe.qfeL.size());
		if (n > 0) {
			XNAFSingleExp qfe = rfe.qfeL.get(0);
			assertEquals(fnName, qfe.funcName);
			assertEquals(numArgs, qfe.argL.size());
		}
	}
	public static void chkFuncRulePolarity(RuleExp exp, boolean polarity) {
		XNAFMultiExp rfe = (XNAFMultiExp) exp.opExpr;
		assertEquals(polarity, rfe.polarity);
	}

	public static void chkRules(TypeStatementExp typeExp, int expectedSize) {
		assertEquals(expectedSize, typeExp.ruleSetExp.ruleL.size());
	}

}
