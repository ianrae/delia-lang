package org.delia.compiler.parser;

import java.util.List;
import java.util.Optional;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Token;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.compiler.astx.XNAFTransientExp;

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
		
//		private static final Parser.Reference<Exp> ruleArgRef = Parser.newReference();
		public static void initLazy() {
//			ruleArgRef.set(NameAndFuncParser.parseNameAndFuncs());		
		}
//		private static Parser<Exp> ruleArg() {
//			return Parsers.or(
//					LetParser.explicitValue(),
//					ruleArgRef.lazy(),
//					ident());
//		}
		
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
		private static Parser<XNAFSingleExp> ruleFn1a() {
			return Parsers.sequence(ident(), args1().asOptional(), 
					(IdentExp exp1, Optional<XNAFTransientExp> args) 
					-> new XNAFSingleExp(99, exp1, args.orElse(null), true, null));
		}
		private static Parser<XNAFSingleExp> ruleFn1ax() {
			return Parsers.sequence(Parsers.INDEX, ident(), 
					(Integer pos, IdentExp exp1) 
					-> new XNAFSingleExp(99, exp1, null, true, null));
		}
		
//		private static Parser<XNAFSingleExp> ruleFn1() {
//			return Parsers.sequence(ident(), term("("), fnOperand().many().sepBy(term(",")), term(")"), 
//					(IdentExp exp1, Token tok, List<List<Exp>> arg, 
//							Token tok2) 
//					-> new XNAFSingleExp(tok.index(), exp1, arg, true));
//		}
//		private static Parser<XNAFSingleExp> ruleFn1NoArg() {
//			return Parsers.or(ident()).
//			map(new org.jparsec.functors.Map<IdentExp, XNAFSingleExp>() {
//				@Override
//				public XNAFSingleExp map(IdentExp exp) {
//					return new XNAFNameExp(99, exp);
//				}
//			});
//		}
//		private static Parser<XNAFSingleExp> fieldOrFn() {
//			return Parsers.or(ruleFn1(), ruleFn1NoArg()); 
//		}
		private static Parser<XNAFSingleExp> ruleFn2() {
			return Parsers.sequence(Parsers.INDEX, ruleFn1ax(), 
					(Integer pos, XNAFSingleExp qfe) -> qfe);
		}
		private static Parser<List<List<XNAFSingleExp>>> ruleFn3() {
			return ruleFn1a().many().sepBy(term("."));
		}
		public static Parser<Exp> parseNameAndFuncs() {
			return Parsers.sequence(Parsers.INDEX, term("!").asOptional(), ruleFn3(),
					(Integer pos, Optional<Token> notTok, List<List<XNAFSingleExp>> qfelist) -> new XNAFMultiExp(pos, notTok == null, qfelist));
		}
		
	}