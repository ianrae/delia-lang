package org.delia.compiler.parser;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Token;
import org.delia.compiler.ast.IdentExp;

public class ParserBase {
	public static Parser<IdentExp> ident() {
		return Parsers.or(TerminalParser.identSyntacticParser)
				.map(new org.jparsec.functors.Map<String, IdentExp>() {
					@Override
					public IdentExp map(String arg0) {
						return new IdentExp(arg0);
					}
				});
	}

	public static Parser<IdentExp> dollarVar() {
		return Parsers.or(term("$$"))
				.map(new org.jparsec.functors.Map<Token, IdentExp>() {
					@Override
					public IdentExp map(Token tok) {
						return new IdentExp(tok.index(), (String)tok.toString());
					}
				});
	}
	
	public static Parser<IdentExp> varName() {
		return Parsers.or(dollarVar(), ident());
	}

	public static Parser<Token> term(String name) {
		return TerminalParser.token(name);
	}	
	
}