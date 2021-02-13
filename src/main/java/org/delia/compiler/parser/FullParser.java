package org.delia.compiler.parser;

import java.util.List;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.delia.compiler.ast.Exp;

/**
 * Top-level parser for the Delia language.
 * 
 * @author Ian Rae
 *
 */
public class FullParser {

	public static Parser<Exp> allStatements() {
		return Parsers.or(LetParser.letAssignment(),
				LetParser.dollarDollarAssignment(),
				QueryParser.fullQuery(),
				CrudParser.allCrudStatements(),
				TypeParser.typeStatement(),
				UserFnParser.userFunction(),
				ConfigureParser.configAssignment(),
				LogParser.logStatement(),
				InputFunctionParser.inputFunction());
	}
	public static Exp parseOne(String input){
		RuleParser.initLazy();		
		QueryParser.initLazy();
		return FullParser.allStatements().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(input);
	}
	public static List<Exp> parse(String input){
		RuleParser.initLazy();		
		QueryParser.initLazy();
		return FullParser.allStatements().many().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(input);
	}
}