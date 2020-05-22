package org.delia.compiler.parser;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.compiler.ast.QueryInExp;
import org.delia.compiler.ast.StringExp;

/**
 * Parser for queries (that appear on right-side of let statements).
 * 
 * @author Ian Rae
 *
 */
public class QueryParser extends ParserBase {
	
	private static Parser<Exp> filterArg() {
		return Parsers.or(
				LetParser.explicitValue(),
				NameAndFuncParser.parseNameAndFuncs());
	}
	public static Parser<StringExp> filterop() {
		//"==", "<", ">", ">=", "<=", "!="
		return Parsers.or(term("!="), term("="), term("=="),
				term(">"), term("<"), term(">="), term("<="), term("like"))
				.map(new org.codehaus.jparsec.functors.Map<Token, StringExp>() {
					@Override
					public StringExp map(Token tok) {
						return new StringExp(tok.index(), (String)tok.toString());
					}
				});
	}
	
	private static Parser<Exp> singleFieldValue() {
		return Parsers.or(LetParser.explicitValue(), varName());
	}
	private static Parser<FilterOpFullExp> queryIn() {
		return Parsers.sequence(ident(), term("in"), term("["), singleFieldValue().many().sepBy(term(",")), term("]"),
				(IdentExp fieldExp, Token t0, Token t1, List<List<Exp>> exps, Token t2) -> new FilterOpFullExp(99, new QueryInExp(99, fieldExp, exps)));
	}
	
	private static final Parser.Reference<FilterOpFullExp> filterOpRef = Parser.newReference();
	public static void initLazy() {
		NameAndFuncParser.initLazy();
		QueryParser.filterOpRef.set(QueryParser.opexpr2());
	}
	
	
//	public static Parser<FilterOpFullExp> like() {
//		return Parsers.sequence(Parsers.INDEX, filterArg(), term("like"), filterArg(),
//				(Integer pos, Exp op1, Token tok, Exp op2) -> new FilterOpFullExp(pos, new FilterOpExp(pos, op1, new StringExp("like"), op2)));
//	}
	private static Parser<FilterOpFullExp> opexpr0() {
		return Parsers.sequence(Parsers.INDEX, filterArg(), filterop(), filterArg(),
				(Integer pos, Exp op1, StringExp op, Exp op2) -> new FilterOpFullExp(pos, new FilterOpExp(pos, op1, op, op2)));
	}
	private static Parser<FilterOpFullExp> opexpr0AndLazy() {
		return Parsers.or(opexpr0(), filterOpRef.lazy());
	}
	private static Parser<FilterOpFullExp> opexprParen() {
		return Parsers.sequence(Parsers.INDEX, term("("), opexpr0AndLazy(), term(")"),
				(Integer pos, Token t0, FilterOpFullExp opexp, Token t1) -> opexp);
	}
	private static Parser<FilterOpFullExp> opexprNegParen() {
		return Parsers.sequence(Parsers.INDEX, term("!"), term("("), opexpr0AndLazy(), term(")"),
				(Integer pos, Token t0, Token t1, FilterOpFullExp opexp, Token t2) -> opexp.negate());
	}
	
	private static Parser<FilterOpFullExp> opexpr1() {
		return Parsers.or(queryIn(), opexpr0(), opexprParen(), opexprNegParen());
	}
//	public static Parser<FilterOpFullExp> opexprNeg() {
//		return Parsers.sequence(Parsers.INDEX, term("!"), opexpr1(),
//				(Integer pos, Token t0, FilterOpFullExp opexp) -> new FilterOpFullExp(pos, true, opexp, false, null));
//	}
	private static Parser<FilterOpFullExp> opexprOr() {
		return Parsers.sequence(Parsers.INDEX, opexpr1(), term("or"), opexpr1(),
				(Integer pos, FilterOpFullExp opexp1, Token t0, FilterOpFullExp opexp2) -> new FilterOpFullExp(pos, false, opexp1, false, opexp2));
	}
	private static Parser<FilterOpFullExp> opexprAnd() {
		return Parsers.sequence(Parsers.INDEX, opexpr1(), term("and"), opexpr1(),
				(Integer pos, FilterOpFullExp opexp1, Token t0, FilterOpFullExp opexp2) -> new FilterOpFullExp(pos, false, opexp1, true, opexp2));
	}
	
	private static Parser<FilterOpFullExp> opexpr2() {
		return Parsers.or(opexprOr(), opexprAnd(), opexpr1());
	}
	private static Parser<Exp> filter0() {
		return Parsers.or(
				opexpr2(),
				LetParser.explicitValue(),
				varName());
	}
	
	private static Parser<FilterExp> filterExpr() {
		return Parsers.sequence(term("["), Parsers.INDEX, filter0(), term("]"), 
				(Token tok1, Integer pos, Exp cond, Token tok2) -> new FilterExp(pos, cond));
	}
	
	//fns
	//TODO: used by LetParser - fix to use NameAndFuncParser
	public static Parser<Exp> fnOperand() {
		return Parsers.or(LetParser.explicitValue(), varName());
	}
	private static Parser<QueryFuncExp> queryFn1() {
		return Parsers.sequence(ident(), term("("), fnOperand().many().sepBy(term(",")), term(")"), 
				(IdentExp exp1, Token tok, List<List<Exp>> arg, Token tok2) -> new QueryFuncExp(tok.index(), exp1, arg, false));
	}
	private static Parser<QueryFuncExp> queryFn1NoArg() {
		return Parsers.or(ident()).
		map(new org.codehaus.jparsec.functors.Map<IdentExp, QueryFuncExp>() {
			@Override
			public QueryFuncExp map(IdentExp exp) {
				return new QueryFieldExp(99, exp);
			}
		});
	}
	private static Parser<QueryFuncExp> fieldOrFn() {
		return Parsers.or(queryFn1(), queryFn1NoArg()); 
	}
	private static Parser<QueryFuncExp> queryFn2() {
		return Parsers.sequence(term("."), fieldOrFn(), 
				(Token tok, QueryFuncExp qfe) -> qfe);
	}
	private static Parser<List<List<QueryFuncExp>>> queryFn3() {
		return queryFn2().many().sepBy(term("."));
	}
	
	
	private static Parser<FilterExp> optionalFilter() {
		return filterExpr().optional();
	}
	public static Parser<Exp> fullQuery() {
		return Parsers.sequence(Parsers.INDEX, varName(), optionalFilter(), queryFn3().optional(),
				(Integer pos, IdentExp varName, FilterExp exp, List<List<QueryFuncExp>> qfelist) -> new QueryExp(pos, varName, exp, qfelist));
	}
	//used by delete and update statements
	public static Parser<QueryExp> partialQuery() {
		return Parsers.sequence(Parsers.INDEX, varName(), optionalFilter(),
				(Integer pos, IdentExp varName, FilterExp exp) -> new QueryExp(pos, varName, exp, null));
	}
}