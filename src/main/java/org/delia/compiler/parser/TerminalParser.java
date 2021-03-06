package org.delia.compiler.parser;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Token;
import org.delia.compiler.ast.Exp;

/**
 * Low-level parser of terminals.
 * 
 * @author Ian Rae
 *
 */
public class TerminalParser {
	public static Terminals operators = Terminals.operators(
	        "-", "(", ")","..","!",
			"[", "]", ",", "{", "}", ":",
			"==", "<", ">", ">=", "<=", "!=",
			".", "$$", "->",
			"=")
		.words(DeliaLexer.IDENTIFIER)
		.keywords("let", "type", "end", "struct", "map", "enum", "list", "or", "and", "false", "true", 
				"in", "like", "configure", "log",
		        "function", "package", "import", "null", "optional", "unique", "primaryKey",
		        "relation", "one", "many", "parent", "serial",
		        "insert", "update", "upsert", "delete", "input",
		        //and for tlang
		        "using", "if", "then", "elseif", "endif", "return")
		.build();
	

	public static Parser<?> identTokenizer = Terminals.Identifier.TOKENIZER;
	public static Parser<String> identSyntacticParser = Terminals.Identifier.PARSER;
	public static Parser<?> integerTokenizer = Terminals.IntegerLiteral.TOKENIZER;
	public static Parser<String> integerSyntacticParser = Terminals.IntegerLiteral.PARSER;
	public static Parser<?> numberTokenizer = Terminals.DecimalLiteral.TOKENIZER;
	public static Parser<String> numberSyntacticParser = Terminals.DecimalLiteral.PARSER;
	public static final Parser<String> LITERAL = Parsers.or(
			Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
			Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER);
	public static final Parser<String> stringSyntacticParser = Terminals.StringLiteral.PARSER;

	public static Parser<?> ignored = Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.WHITESPACES);
	//don't use both numberTokenizer and integerTokenizer
	public static Parser<?> tokenizer = Parsers.or(operators.tokenizer(), identTokenizer, numberTokenizer, LITERAL); // tokenizes the operators and integer

	public static Parser<Token> token(String tok) {
		return operators.token(tok);
	}
	public static Parser<Exp> tokenExp(String tok, Exp exp) {
		return token(tok).<Exp>retn(exp);
	}
	public static <T> Parser<T> tokenExpT(String tok, T exp) {
		return token(tok).<T>retn(exp);
	}
	public static Parser<String> tokenExp2(String tok, String s) {
		return token(tok).retn(s);
	}
}