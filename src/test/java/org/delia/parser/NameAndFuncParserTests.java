package org.delia.parser;

import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.parser.NameAndFuncParser;
import org.delia.compiler.parser.TerminalParser;
import org.junit.Test;



public class NameAndFuncParserTests {
	
	@Test
	public void test() {
		Exp exp = parse1("Customer");
		exp = parse1("Customer.foo()");
		exp = parse1("Customer.foo(5)");
		exp = parse1("Customer.foo(5,6)");
		exp = parse1("Customer.foo('abc',6)");
//		exp = parse1("Customer.foo(other.bar())");
	}
	
	@Test
	public void test1a() {
		Exp exp = parse1("Customer.foo().bar()");
		assertEquals("Customer().foo().bar()", exp.strValue());
	}
	
	@Test
	public void testDebug() {
//		Exp exp = parse1("Customer.foo(5)");
//		Exp exp = parse1("Customer");
//TODO: fix		Exp exp = parse1("Customer.foo(other.bar(4))");
//		exp = parse1("Customer.foo(5,6)");
		log("sdfsdf");
	}
	

	// --
	private void log(String s) {
		System.out.println(s);
	}
	private XNAFMultiExp parse1(String filter) {
		XNAFMultiExp exp = parseFilter(filter);
		log(exp.strValue());
		return exp;
	}

	private XNAFMultiExp parseFilter(String src) {
		return parse(src);
	}

	
	private XNAFMultiExp parse(String src) {
		log(src);
		NameAndFuncParser.initLazy();
		Exp exp = NameAndFuncParser.parseNameAndFuncs().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return (XNAFMultiExp) exp;
	}
}
