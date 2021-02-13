package org.delia.compiler.parser;

import java.util.List;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.RuleExp;
import org.delia.compiler.ast.RuleSetExp;
import org.delia.compiler.ast.StringExp;

/**
 * parser for validation rules in a type statement.
 * 
 * @author Ian Rae
 *
 */
public class RuleParser extends ParserBase {
		
		private static final Parser.Reference<Exp> ruleArgRef = Parser.newReference();
		public static void initLazy() {
			NameAndFuncParser.initLazy();
//			ruleArgRef.set(RuleParser.ruleFn4());		
			ruleArgRef.set(RuleParser.ruleFn4x());		
		}
		private static Parser<Exp> ruleArg() {
			return Parsers.or(
					LetParser.explicitValue(),
					ruleArgRef.lazy(),
					ident());
		}
		private static Parser<StringExp> ruleOp() {
			//"==", "<", ">", ">=", "<=", "!="
			//allow = same as ==
			//allow <> same as !=
			return QueryParser.filterop();
		}
		
		public static Parser<FilterOpExp> opexpr() {
			return Parsers.sequence(Parsers.INDEX, ruleArg(), ruleOp(), ruleArg(),
					(Integer pos, Exp op1, StringExp op, Exp op2) -> new FilterOpExp(pos, op1, op, op2));
		}
		
		private static Parser<Exp> ruleFn4x() {
			return NameAndFuncParser.parseNameAndFuncs();
		}
		
		private static Parser<Exp> allRules() {
			return Parsers.or(opexpr(), ruleFn4x());
		}
		
		//public for unit tests
		public static Parser<RuleExp> oneRule() {
			return Parsers.sequence(Parsers.INDEX, allRules(),
					(Integer pos, Exp exp) -> new RuleExp(pos, exp));
		}
		
		public static Parser<RuleSetExp> rules() {
			return Parsers.or(oneRule().many().sepBy(term(","))).
					map(new org.jparsec.functors.Map<List<List<RuleExp>>, RuleSetExp>() {
						@Override
						public RuleSetExp map(List<List<RuleExp>> ruleL) {
							return new RuleSetExp(99, ruleL);
						}
					});
		}
	}