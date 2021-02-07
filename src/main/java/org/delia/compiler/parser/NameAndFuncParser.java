package org.delia.compiler.parser;

import java.util.List;
import java.util.Optional;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.compiler.astx.XNAFTransientExp;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Token;

/**
 * parses expressions like
 *    firstName
 *    firstName.len()
 *    firstName.contains('x')
 *    firstName.contains(other.foo()) //ruleArgRef - recursive
 *    
 * Used in rules, and in query filter expressions
 * 
 * @author Ian Rae
 *
 */
public class NameAndFuncParser extends ParserBase {
		
		public static void initLazy() {
//			ruleArgRef.set(NameAndFuncParser.parseNameAndFuncs());		
		}
		
		//fns
		private static Parser<Exp> fnOperand() {
			return Parsers.or(LetParser.explicitValue(), ident());
		}
		
		private static Parser<XNAFTransientExp> args1() {
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
			return Parsers.or(ruleFn0b(), ruleFn0a());
		}
		
//		private static Parser<XNAFSingleExp> ruleFn2() {
//			return Parsers.sequence(Parsers.INDEX, ruleFn1ax(), 
//					(Integer pos, XNAFSingleExp qfe) -> qfe);
//		}
		public static Parser<List<List<XNAFSingleExp>>> ruleFn3() {
			return ruleFn1a().many().sepBy(term("."));
		}
		public static Parser<Exp> parseNameAndFuncs() {
			return Parsers.sequence(Parsers.INDEX, term("!").asOptional(), ruleFn3(),
					(Integer pos, Optional<Token> notTok, List<List<XNAFSingleExp>> qfelist) -> new XNAFMultiExp(pos, !notTok.isPresent(), qfelist));
		}
		
	}