package org.delia.compiler.parser;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.inputfunction.EndIfStatementExp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.IfStatementExp;
import org.delia.compiler.ast.inputfunction.InputFuncHeaderExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionBodyExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.compiler.ast.inputfunction.TLangBodyExp;

public class InputFunctionParser extends ParserBase {


	public static Parser<Exp> tlangSingleStatement() {
		return Parsers.or(
				LetParser.explicitValue(),
				NameAndFuncParser.parseNameAndFuncs()
				);
	}
	
	private static Parser<Exp> condition() {
		return Parsers.or(
				LetParser.explicitValue(),
				ident()
				);
	}
	
	private static Parser<IfStatementExp> ifStatement() {
		return Parsers.sequence(term("if"), condition(), term("then"), tlangSingleStatement(),
				(Token tok, Exp cond, Token tok3, Exp statement) -> new IfStatementExp(99, cond, statement));
	}
	private static Parser<IfStatementExp> elseIfStatement() {
		return Parsers.sequence(term("elseif"), condition(), term("then"), tlangSingleStatement(),
				(Token tok, Exp cond, Token tok3, Exp statement) -> new IfStatementExp(99, cond, statement));
	}
	
	
	private static Parser<Exp> endIfStatement() {
		return Parsers.or(term("endif")).
				map(new org.codehaus.jparsec.functors.Map<Token, EndIfStatementExp>() {
					@Override
					public EndIfStatementExp map(Token arg0) {
						return new EndIfStatementExp(99);
					}
				});
	}
	
	
	public static Parser<Exp> tlangBodyStatement() {
		return Parsers.or(
				ifStatement(),
				elseIfStatement(),
				endIfStatement(),
				tlangSingleStatement()
				);
	}
	

	private static Parser<TLangBodyExp> tlangBody() {
		return Parsers.or(tlangBodyStatement().many().sepBy(term(","))).
				map(new org.codehaus.jparsec.functors.Map<List<List<Exp>>, TLangBodyExp>() {
					@Override
					public TLangBodyExp map(List<List<Exp>> list) {
						return new TLangBodyExp(99, list);
					}
				});
	}

	private static Parser<TLangBodyExp> tlangBody1() {
		return Parsers.sequence(term("using"), term("{"), tlangBody(), term("}"),
				(Token tok, Token tok2, TLangBodyExp body, Token tok3) -> body);
	}

	private static Parser<Exp> fnBodyStatements() {
		return Parsers.sequence(ident(), term("->"), identPair(), tlangBody1().optional(),
				(IdentExp field, Token tok, IdentPairExp output, TLangBodyExp body) -> new InputFuncMappingExp(99, field, output, body));
	}

	private static Parser<InputFunctionBodyExp> fnBody() {
		return Parsers.or(fnBodyStatements().many().sepBy(term(","))).
				map(new org.codehaus.jparsec.functors.Map<List<List<Exp>>, InputFunctionBodyExp>() {
					@Override
					public InputFunctionBodyExp map(List<List<Exp>> list) {
						return new InputFunctionBodyExp(99, list);
					}
				});
	}

	private static Parser<StringExp> inputFunc() {
		return Parsers.sequence(term("input"), term("function"), 
				(Token fn, Token tok) -> new StringExp(99, ""));
	}

	private static Parser<IdentPairExp> identPair() {
		return Parsers.sequence(ident(), term("."), ident(), 
				(IdentExp exp1, Token tok, IdentExp exp2) -> new IdentPairExp(99, exp1.name(), exp2.name()));
	}
	private static Parser<IdentPairExp> identPairArg() {
		return Parsers.sequence(ident(), ident(), 
				(IdentExp exp1, IdentExp exp2) -> new IdentPairExp(99, exp1.name(), exp2.name()));
	}

	private static Parser<InputFuncHeaderExp> inputFn1() {
		return Parsers.sequence(inputFunc(), ident(), term("("), identPairArg().many().sepBy(term(",")), term(")"), 
				(StringExp fn, IdentExp exp1, Token tok, List<List<IdentPairExp>> args, Token tok2) -> new InputFuncHeaderExp(exp1, args));
	}

	public static Parser<InputFunctionDefStatementExp> inputFunction() {
		return Parsers.sequence(Parsers.INDEX, inputFn1(), term("{"), fnBody(), term("}"), 
				(Integer pos, InputFuncHeaderExp exp1, Token tok, InputFunctionBodyExp body, Token tok2) 
				-> new InputFunctionDefStatementExp(pos, exp1, body));
	}
}