package org.delia.compiler.parser;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.InputFuncHeaderExp;
import org.delia.compiler.ast.inputfunction.InputFuncStatementExp;
import org.delia.compiler.ast.inputfunction.InputFunctionBodyExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;

public class InputFunctionParser extends ParserBase {
		
		
//		public static Parser<Exp> fnBodyStatements() {
//			return Parsers.or(ident(),
//					LetParser.explicitValue(),
//					NameAndFuncParser.parseNameAndFuncs()
//					);
//		}
		public static Parser<Exp> fnBodyStatements() {
			return Parsers.sequence(ident(), term("->"), identPair(),
					(IdentExp field, Token tok, IdentPairExp output) -> new InputFuncStatementExp(99, field, output));
		}
		
		public static Parser<InputFunctionBodyExp> fnBody() {
			return Parsers.or(fnBodyStatements().many().sepBy(term(","))).
					map(new org.codehaus.jparsec.functors.Map<List<List<Exp>>, InputFunctionBodyExp>() {
						@Override
						public InputFunctionBodyExp map(List<List<Exp>> list) {
							return new InputFunctionBodyExp(99, list);
						}
					});
		}
		
		public static Parser<StringExp> inputFunc() {
			return Parsers.sequence(term("input"), term("function"), 
					(Token fn, Token tok) -> new StringExp(99, ""));
		}
		
		public static Parser<IdentPairExp> identPair() {
			return Parsers.sequence(ident(), term("."), ident(), 
					(IdentExp exp1, Token tok, IdentExp exp2) -> new IdentPairExp(99, exp1.name(), exp2.name()));
		}
		public static Parser<IdentPairExp> identPairArg() {
			return Parsers.sequence(ident(), ident(), 
					(IdentExp exp1, IdentExp exp2) -> new IdentPairExp(99, exp1.name(), exp2.name()));
		}

		public static Parser<InputFuncHeaderExp> inputFn1() {
			return Parsers.sequence(inputFunc(), ident(), term("("), identPairArg().many().sepBy(term(",")), term(")"), 
					(StringExp fn, IdentExp exp1, Token tok, List<List<IdentPairExp>> args, Token tok2) -> new InputFuncHeaderExp(exp1, args));
		}
		
		public static Parser<InputFunctionDefStatementExp> inputFunction() {
			return Parsers.sequence(Parsers.INDEX, inputFn1(), term("{"), fnBody(), term("}"), 
					(Integer pos, InputFuncHeaderExp exp1, Token tok, InputFunctionBodyExp body, Token tok2) 
					-> new InputFunctionDefStatementExp(pos, exp1, body));
		}
	}