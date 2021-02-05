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
				-> new XNAFSingleExp(99, exp1, null, true, null));
		}
		public static Parser<XNAFSingleExp> ruleFn0b() {
		return Parsers.sequence(ident(), args1(), 
				(IdentExp exp1, XNAFTransientExp args) 
				-> new XNAFSingleExp(99, exp1, args, true, null));
		}
		public static Parser<XNAFSingleExp> ruleFn1a() {
			return Parsers.or(ruleFn0b(), ruleFn0a());
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
//		Exp exp = parse1("Customer.foo().bar()");
		Exp exp = parse1("a.b.c");
		assertEquals("sdf", exp.strValue());
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
