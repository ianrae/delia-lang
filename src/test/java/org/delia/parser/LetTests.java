package org.delia.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.base.UnitTestLog;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.runner.CompilerHelper;
import org.delia.runner.RunnerHelper;
import org.junit.Test;



public class LetTests {

	@Test
	public void testStr() {
		chkString("let a = 'bob'", "let a = 'bob'");
	}
	@Test
	public void testInt() {
		chkInt("let a = 45", "let a = 45");
		chkInt("let a = -2147483648", "let a = -2147483648"); //Integer.MIN
		chkInt("let a = 2147483647", "let a = 2147483647"); //Integer.MAX
	}
	@Test
	public void testBoolean() {
		chkBoolean("let a = true", "let a = true");
		chkBoolean("let a = false", "let a = false");
	}

	@Test
	public void testLong() {
		chkLong("let a = 2147483650", "let a = 2147483650");
	}
	@Test
	public void testNull() {
		chkNull("let a = null", "let a = null");
	}
	
	//TODO: this is now a parse error. fix
//	@Test
//	public void testNullBad() {
//		chkQueryLet("let a = Actor[null = y]", "let a = Actor[null = y]");
//	}

	@Test
	public void testVar() {
//		chkVar("let a = x", "let a = x");
		chkQueryLet("let a = x", "let a = x");
		//Note let = x parsed as let x = Actor
		//will have to figure this out during link step
	}
	@Test
	public void testLetQuery() {
		chkQueryLet("let a = Actor[x = y]", "let a = Actor[x = y]");
	}
	
	//TODO: fix
//	@Test
//	public void testLetUserFn() {
//		chkUserFunc("let a = foo()", "let a = foo()");
//	}
	
	@Test
	public void testQuery1() {
		chkDollarDollarQuery("Actor", "Actor");
		chkDollarDollarQuery("Actor[x]", "Actor[x]");
		chkDollarDollarQuery("Actor[45]", "Actor[45]");
		chkDollarDollarQuery("Actor['s']", "Actor['s']");
		chkDollarDollarQuery("Actor[true]", "Actor[true]");
	}
	@Test
	public void testQueryOpExpr() {
		chkDollarDollarQuery("Actor[x = y]", "Actor[x = y]");
		chkDollarDollarQuery("Actor[x != 45]", "Actor[x != 45]");
		chkDollarDollarQuery("Actor[x = 's']", "Actor[x = 's']");
		chkDollarDollarQuery("Actor[x = true]", "Actor[x = true]");
	}
	@Test
	public void testQueryOp() {
		chkDollarDollarQuery("Actor[x = y]", "Actor[x = y]");
		chkDollarDollarQuery("Actor[x == y]", "Actor[x == y]");
		chkDollarDollarQuery("Actor[x != y]", "Actor[x != y]");
		chkDollarDollarQuery("Actor[x > y]", "Actor[x > y]");
		chkDollarDollarQuery("Actor[x < y]", "Actor[x < y]");
		chkDollarDollarQuery("Actor[x >= y]", "Actor[x >= y]");
		chkDollarDollarQuery("Actor[x <= y]", "Actor[x <= y]");
	}
	@Test
	public void testQueryFunc() {
		chkDollarDollarQuery("Actor[x = y].abc()", "Actor[x = y].abc()");
		chkDollarDollarQuery("Actor[x = y].abc(45)", "Actor[x = y].abc(45)");
		chkDollarDollarQuery("Actor[x = y].abc(45,'abc')", "Actor[x = y].abc(45,'abc')");
		chkDollarDollarQuery("Actor[x = y].abc(45,true)", "Actor[x = y].abc(45,true)");
	}
	@Test
	public void testQueryFunc2() {
		chkDollarDollarQuery("Actor.abc(45).def()", "Actor.abc(45).def()");
		chkDollarDollarQuery("Actor[x = y].abc(45).def()", "Actor[x = y].abc(45).def()");
	}
	@Test
	public void testDollar() {
		chkDollarDollarQuery("Actor[$$]", "Actor[$$]");
		chkDollarDollarQuery("$$[44]", "$$[44]");
	}
	@Test
	public void testQueryField() {
		chkDollarDollarQuery("Actor.abc", "Actor.abc");
		chkDollarDollarQuery("Actor.abc.def", "Actor.abc.def");
		chkDollarDollarQuery("Actor.abc.def()", "Actor.abc.def()");
		chkDollarDollarQuery("Actor.abc().def", "Actor.abc().def");
	}
	
	@Test
	public void testMultiLine() {
		chkTwoLine("Actor[$$]\nlet x = y", "let $$ = Actor[$$]", "let x = y");
	}
	
	// --
	private CompilerHelper chelper = new CompilerHelper(null, new UnitTestLog());
	
	private void chkString(String input, String output) {
		chelper.chkString(input, output);
	}
	private void chkInt(String input, String output) {
		LetStatementExp exp = (LetStatementExp) chelper.parseOne(input);
		assertEquals(output, exp.toString());
		assertEquals("int", exp.typeName);
	}
	private void chkBoolean(String input, String output) {
		LetStatementExp exp = (LetStatementExp) chelper.parseOne(input);
		assertEquals(output, exp.toString());
		assertEquals("boolean", exp.typeName);
	}
	private void chkNull(String input, String output) {
		LetStatementExp exp = (LetStatementExp) chelper.parseOne(input);
		assertEquals(output, exp.toString());
		assertEquals(null, exp.typeName);
		assertEquals("null", exp.value.strValue());
	}
	private void chkLong(String input, String output) {
		LetStatementExp exp = (LetStatementExp) chelper.parseOne(input);
		assertEquals(output, exp.toString());
		assertEquals("long", exp.typeName);
	}
	private void chkQueryLet(String input, String output) {
		chelper.chkQueryLet(input, output);
	}
	private void chkUserFunc(String input, String output) {
		chelper.chkUserFuncInvoke(input, output);
	}
	
	private void chkDollarDollarQuery(String input, String output) {
		chelper.chkQueryLet(input, "let $$ = " + output);
//		QueryExp exp = (QueryExp) chelper.parseOne(input);
//		assertEquals(output, exp.toString());
	}
	private void chkTwoLine(String input, String output1, String output2) {
		List<Exp> list = chelper.parse(input);
		assertEquals(2, list.size());
		Exp exp = list.get(0);
		assertEquals(output1, exp.toString());
		exp = list.get(1);
		assertEquals(output2, exp.toString());
	}
	
}
