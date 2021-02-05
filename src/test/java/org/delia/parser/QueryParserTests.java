package org.delia.parser;

import org.jparsec.error.ParserException;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.parser.QueryParser;
import org.delia.compiler.parser.TerminalParser;
import org.junit.Test;



public class QueryParserTests {
	
	@Test
	public void test() {
		Exp exp = parse("Customer");
		exp = parse("Customer[true]");
		exp = parse("Customer[55]");
	}
	
	@Test(expected=ParserException.class)
	public void test33() {
		Exp exp = parse("Customer[]");
	}
	
	@Test
	public void test2() {
		parse1("x < 5");
		parse1("(x < 5)");
		parse1("!(x < 5)");
		parse1("x < 5 and y < 10");
		parse1("(x < 5) and (y < 10)");
	}
	@Test
	public void test2a() {
		parse1("5 < x");
		parse1("z < x");
//		parse1("(x < 5)");
//		parse1("!(x < 5)");
//		parse1("x < 5 and y < 10");
//		parse1("(x < 5) and (y < 10)");
	}
	
	@Test
	public void testDebug() {
//		QueryExp exp = parseFilter("(x < 5) and (y < 10) or z < 10");
		QueryExp exp = parseFilter("((x < 5) and (y < 10)) or z < 10");
		log(exp.strValue());
		chkFilter(exp);
	}
	
	private void chkFilter(QueryExp expQry) {
		FilterExp filterExp = expQry.filter;
		FilterOpFullExp exp = (FilterOpFullExp) filterExp.cond;
		log(exp.strValue());
	}

	// --
	private void log(String s) {
		System.out.println(s);
	}
	private QueryExp parse1(String filter) {
		QueryExp exp = parseFilter(filter);
		log(exp.strValue());
		chkFilter(exp);
		return exp;
	}

	private QueryExp parseFilter(String filterStr) {
		String src = String.format("Customer[%s]", filterStr);
		return parse(src);
	}

	
	private QueryExp parse(String src) {
		QueryParser.initLazy();
		Exp exp = QueryParser.fullQuery().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return (QueryExp) exp;
	}
}
