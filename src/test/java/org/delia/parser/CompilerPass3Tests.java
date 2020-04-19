package org.delia.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.DeliaCompiler;
import org.delia.compiler.ast.Exp;
import org.delia.runner.DeliaException;
import org.junit.Test;

public class CompilerPass3Tests extends ParserTestBase {
	
	@Test(expected=DeliaException.class)
	public void testSyntaxError() {
		DeliaCompiler compiler = initCompiler();
		compiler.parse("letxxx a = 10"); //throws
	}
	@Test
	public void testOK() {
		DeliaCompiler compiler = initCompiler();
		List<Exp> list = compiler.parse("let a = 10");
		assertEquals(1, list.size());
	}
	@Test(expected=DeliaException.class)
	public void testPass3Error() {
		DeliaCompiler compiler = initCompiler();
		List<Exp> list = compiler.parse("let a = 10\nlet a = 5");
		assertEquals(1, list.size());
	}
	@Test(expected=DeliaException.class)
	public void testPass3ErrorDupType() {
		DeliaCompiler compiler = initCompiler();
		List<Exp> list = compiler.parse("type Actor struct {} end\ntype Actor struct {} end");
		assertEquals(1, list.size());
	}
	@Test(expected=DeliaException.class)
	public void testPass3ErrorDupFunc() {
		DeliaCompiler compiler = initCompiler();
		List<Exp> list = compiler.parse("function foo() {}\nfunction foo() {}");
		assertEquals(1, list.size());
	}
	@Test(expected=DeliaException.class)
	public void testPass3FuncNotFound() {
		DeliaCompiler compiler = initCompiler();
		List<Exp> list = compiler.parse("function foo() {}\nlet x = bar()");
		assertEquals(1, list.size());
	}
	@Test(expected=DeliaException.class)
	public void testPass3FuncWrongNumArgs() {
		DeliaCompiler compiler = initCompiler();
		List<Exp> list = compiler.parse("function foo(id) {}\nlet x = foo()");
		assertEquals(1, list.size());
	}
}
