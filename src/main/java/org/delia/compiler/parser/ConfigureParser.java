package org.delia.compiler.parser;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.compiler.ast.ConfigureStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;

/**
 * Parser for configure statement
 * 
 * @author Ian Rae
 *
 */
public class ConfigureParser extends ParserBase {

	public static Parser<IdentExp> configPrefix() {
		return Parsers.sequence(Parsers.INDEX, ident(), term("."), 
				(Integer pos, IdentExp prefixExp, Token tokRelation) 
				-> prefixExp);
	}

	public static Parser<Exp> configAssignment() {
		return Parsers.sequence(term("configure"), configPrefix().optional(), ident(), term("="), LetParser.righthandside(),
				(Token tok, IdentExp prefix, IdentExp varName, Token eq, Exp strexp) -> new ConfigureStatementExp(tok.index(), prefix, varName, strexp));
	}
}