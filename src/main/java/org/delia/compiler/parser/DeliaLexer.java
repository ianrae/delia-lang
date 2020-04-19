package org.delia.compiler.parser;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.pattern.Patterns;

/**
 * Lexer specific for the Delia language rules.
 * 
 */
public final class DeliaLexer {
  
  static final Parser<String> IDENTIFIER = Patterns.isChar(Character::isJavaIdentifierStart)
      .next(Patterns.isChar(Character::isJavaIdentifierPart).many())
      .toScanner("identifier")
      .source();
  
}
