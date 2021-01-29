package org.delia.compiler.parser;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.compiler.ast.LogStatementExp;
import org.delia.compiler.ast.Exp;

/**
 * Parser for log statement
 * 
 * @author Ian Rae
 *
 */
public class LogParser extends ParserBase {

	public static Parser<LogStatementExp> logStatement() {
		return Parsers.sequence(term("log"), LetParser.righthandside(),
				(Token tok, Exp exp) -> new LogStatementExp(tok.index(), exp));
	}
}