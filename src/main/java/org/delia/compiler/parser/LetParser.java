package org.delia.compiler.parser;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.UserFnCallExp;

/**
 * Parser for a let statement.
 * 
 * @author Ian Rae
 *
 */
public class LetParser extends ParserBase {
	public static Parser<BooleanExp> booleanValue() {
		return Parsers.or(
				TerminalParser.tokenExpT("true" ,new BooleanExp(true)),
				TerminalParser.tokenExpT("false", new BooleanExp(false)));
	}

	public static Parser<StringExp> stringValue() {
		return Parsers.or(TerminalParser.stringSyntacticParser).
				map(new org.codehaus.jparsec.functors.Map<String, StringExp>() {
					@Override
					public StringExp map(String arg0) {
						return new StringExp(arg0);
					}
				});
	}

	//int,float parser
	public static Exp numberBuilder(int pos, Token negSign, String input) {
		if (input != null && input.contains(".")) {
			NumberExp exp = new NumberExp(pos, Double.parseDouble(input));
			if (negSign != null) {
				exp.val = -1.0 * exp.val;
			}
			return exp;
		} else {
			String tmpStr = (negSign != null) ? "-" + input : input;
			Long lvalue = Long.parseLong(tmpStr);
			Integer ivalue = lvalue.intValue();
			if (ivalue.longValue() == lvalue.longValue()) {
				IntegerExp exp = new IntegerExp(pos, lvalue.intValue());
				return exp;
			} else {
				LongExp exp = new LongExp(pos, lvalue);
				return exp;
			}
		}
	}
	public static Parser<Token> optionalNegSign() {
		return term("-").optional();
	}
	public static Parser<Exp> someNumberValue() {
		return Parsers.sequence(Parsers.INDEX, optionalNegSign(), TerminalParser.numberSyntacticParser, (Integer pos, Token tok, String s) -> numberBuilder(pos, tok, s));
	}

	public static Parser<Exp> righthandside() {
		return Parsers.or(
				explicitValue(),
				userFnCall(),
				QueryParser.fullQuery(),
				ident());
	}
	
	private static Parser<Exp> nullValue() {
		return Parsers.or(term("null")).
				map(new org.codehaus.jparsec.functors.Map<Token, NullExp>() {
					@Override
					public NullExp map(Token arg0) {
						return new NullExp();
					}
				});
	}
	
	//userfn call
	public static Parser<Exp> userFnCall() {
		return Parsers.sequence(Parsers.INDEX, ident(), term("("), QueryParser.fnOperand().many().sepBy(term(",")), term(")"),
				(Integer pos, IdentExp fnName, Token tok1, List<List<Exp>> list, Token tok2) -> new UserFnCallExp(pos, fnName, list));
	}
	
	
	public static Parser<Exp> explicitValue() {
		return Parsers.or(
				LetParser.booleanValue(),
				LetParser.someNumberValue(),
				LetParser.stringValue(),
				nullValue());
	}

	public static Parser<Exp> letAssignment() {
		return Parsers.sequence(term("let"), varName(), ident().optional(), term("="), righthandside(),
				(Token tok, IdentExp varName, IdentExp typeName, Token eq, Exp strexp) -> new LetStatementExp(tok.index(), varName, typeName, strexp));
	}
	public static Parser<Exp> dollarDollarAssignment() {
		return Parsers.or(righthandside()).
				map(new org.codehaus.jparsec.functors.Map<Exp, LetStatementExp>() {
					@Override
					public LetStatementExp map(Exp exp) {
						return new LetStatementExp(exp.getPos(), new IdentExp("$$"), null, exp);
					}
				});

	}
}