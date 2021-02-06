package org.delia.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.compiler.astx.XNAFTransientExp;
import org.delia.compiler.parser.LetParser;
import org.delia.compiler.parser.NameAndFuncParser;
import org.delia.compiler.parser.ParserBase;
import org.delia.compiler.parser.TerminalParser;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Token;
import org.junit.Test;



public class JParsec3Tests {

	public static class FooParser extends ParserBase {

		public static Parser<Exp> fnOperand() {
			return Parsers.or(LetParser.explicitValue(), ident());
		}
		public static Parser<List<List<Exp>>> fnOperandx() {
			return fnOperand().many().sepBy(term("."));
		}
		
		public static Parser<XNAFTransientExp> args1() {
			return Parsers.sequence(term("("), fnOperand().many().sepBy(term(",")), term(")"), 
					(Token tok, List<List<Exp>> arg, 
							Token tok2) 
					-> new XNAFTransientExp(tok.index(), arg));
		}
		public static Parser<XNAFSingleExp> ruleFn0a() {
			return Parsers.sequence(Parsers.INDEX, ident(), 
					(Integer pos, IdentExp exp1) 
					-> new XNAFSingleExp(99, exp1, null, false, null));
		}
		public static Parser<XNAFSingleExp> ruleFn0b() {
			return Parsers.sequence(ident(), args1(), 
					(IdentExp exp1, XNAFTransientExp args) 
					-> new XNAFSingleExp(99, exp1, args, true, null));
		}
		public static Parser<XNAFSingleExp> ruleFn1a() {
			return Parsers.longer(ruleFn0a(), ruleFn0b());
		}
		public static Parser<XNAFSingleExp> ruleFn1aa() {
			return Parsers.or(ruleFn0b(), ruleFn0a());
		}
		
		public static Parser<XNAFSingleExp> ruleFn0c() {
			return Parsers.sequence(ident(), args1().asOptional(), 
					(IdentExp exp1, Optional<XNAFTransientExp> args) 
					-> new XNAFSingleExp(99, exp1, args.orElse(null), true, null));
		}
		

		public static Parser<List<List<XNAFSingleExp>>> ruleFn3() {
			return ruleFn1a().many().sepBy(term("."));
		}
		public static Parser<Exp> parseNameAndFuncs() {
			return Parsers.sequence(Parsers.INDEX, term("!").asOptional(), ruleFn3(),
					(Integer pos, Optional<Token> notTok, List<List<XNAFSingleExp>> qfelist) -> new XNAFMultiExp(pos, notTok == null, qfelist));
		}

	}	
	
	@Test
	public void testABunch() {
		chkParse("Customer", 1, "Customer");
		chkParse("a.b", 2, "a.b");
		chkParse("a.b.c", 3, "a.b.c");
		
		chkParse("Customer()", 1, "Customer()");
		chkParse("a.Customer()", 2, "a.Customer()");
		chkParse("a().Customer()", 2, "a().Customer()");
		
		chkParse("Customer(1)", 1, "Customer(1)");
		chkParse("a.Customer(1,2)", 2, "a.Customer(1,2)");
		chkParse("a(1,2).Customer(3,4)", 2, "a(1,2).Customer(3,4)");
	}
	
	private void chkParse(String src, int n, String expected) {
//		Exp exp = FooParser.parseNameAndFuncs().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		Exp exp = NameAndFuncParser.parseNameAndFuncs().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		XNAFMultiExp xexp = (XNAFMultiExp) exp;
		assertEquals(n, xexp.qfeL.size());
		assertEquals(expected, exp.strValue());
	}



	@Test
	public void test0() {
		String src = "Customer";
		Exp exp = FooParser.fnOperand().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		assertEquals("Customer", exp.strValue());
		List<List<Exp>> list = FooParser.fnOperandx().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		assertEquals("Customer", list.get(0).get(0).strValue());

		src = "(abc)";
		XNAFTransientExp xexp = FooParser.args1().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);

		src = "Customer()";
		XNAFSingleExp sexp = FooParser.ruleFn1a().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);

		src = "Customer()";
		exp = FooParser.parseNameAndFuncs().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
	}
	@Test
	public void test0xa() {
		String src = "Customer";
		List<List<Exp>> sexp = FooParser.fnOperandx().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);

		src = "Customer.foo";
		sexp = FooParser.fnOperandx().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);

		src = "Customer.foo.x";
		sexp = FooParser.fnOperandx().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
	}
	@Test
	public void test1a() {
		String src = "Customer";
		XNAFSingleExp sexp = FooParser.ruleFn1a().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);

		src = "Customer()";
		sexp = FooParser.ruleFn1a().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
	}
	@Test
	public void test0b() {
		try {
			String src = "Customer";
			XNAFSingleExp sexp = FooParser.ruleFn0b().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);

			src = "Customer()";
			sexp = FooParser.ruleFn0b().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	@Test
//	public void test0c() {
//		String src = "Customer";
//		XNAFSingleExp sexp = FooParser.ruleFn0c().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
//
//		src = "Customer()";
//		sexp = FooParser.ruleFn0c().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
//	}

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
	public void testSomething() {
		//		Exp exp = parse1("Customer.foo().bar()");
		Exp exp = parse1("a.b.c");
		assertEquals("a().b().c()", exp.strValue());
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
		Exp exp = FooParser.parseNameAndFuncs().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return (XNAFMultiExp) exp;
	}
}
