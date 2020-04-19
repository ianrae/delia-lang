package org.delia.parser;

import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.UserFunctionDefStatementExp;
import org.delia.compiler.parser.TerminalParser;
import org.delia.compiler.parser.UserFnParser;
import org.junit.Test;



public class UserFnTests {
	@Test
	public void test0() {
		UserFunctionDefStatementExp exp = parseUserFn("function foo() {}");
		assertEquals("foo", exp.funcName);
	}
	@Test
	public void test1() {
		UserFunctionDefStatementExp exp = parseUserFn("function foo() { let x=5 }");
		assertEquals("foo", exp.funcName);
	}
	@Test
	public void test2() {
		UserFunctionDefStatementExp exp = parseUserFn("function foo() { let x=5, x }");
		assertEquals("function foo(){let x = 5,let $$ = x}", exp.toString());
		assertEquals("foo", exp.funcName);
		assertEquals(2, exp.bodyExp.statementL.size());
		Exp line1 = exp.bodyExp.statementL.get(0);
		Exp line2 = exp.bodyExp.statementL.get(1);
	}
	
	// --
	private UserFunctionDefStatementExp parseUserFn(String src) {
		UserFunctionDefStatementExp exp = UserFnParser.userFunction().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return exp;
	}
	
}
