package org.delia.compiler.parser;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.UserFuncHeaderExp;
import org.delia.compiler.ast.UserFunctionBodyExp;
import org.delia.compiler.ast.UserFunctionDefStatementExp;

/**
 * parser for the function statement.
 * 
 * @author Ian Rae
 *
 */
public class UserFnParser extends ParserBase {
	public static Parser<Exp> fnBodyStatements() {
		return Parsers.or(LetParser.letAssignment(),
				LetParser.dollarDollarAssignment(),
				QueryParser.fullQuery(),
				CrudParser.allCrudStatements());
	}
	
	public static Parser<UserFunctionBodyExp> fnBody() {
		return Parsers.or(fnBodyStatements().many().sepBy(term(","))).
				map(new org.codehaus.jparsec.functors.Map<List<List<Exp>>, UserFunctionBodyExp>() {
					@Override
					public UserFunctionBodyExp map(List<List<Exp>> list) {
						return new UserFunctionBodyExp(99, list);
					}
				});
	}

	public static Parser<UserFuncHeaderExp> userFn1() {
		return Parsers.sequence(term("function"), ident(), term("("), ident().many().sepBy(term(",")), term(")"), 
				(Token fn, IdentExp exp1, Token tok, List<List<IdentExp>> args, Token tok2) -> new UserFuncHeaderExp(exp1, args));
	}
	
	public static Parser<UserFunctionDefStatementExp> userFunction() {
		return Parsers.sequence(Parsers.INDEX, userFn1(), term("{"), fnBody(), term("}"), 
				(Integer pos, UserFuncHeaderExp exp1, Token tok, UserFunctionBodyExp body, Token tok2) 
				-> new UserFunctionDefStatementExp(pos, exp1, body));
	}
}