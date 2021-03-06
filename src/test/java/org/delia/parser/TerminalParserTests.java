package org.delia.parser;

import static org.junit.Assert.*;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Token;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.parser.TerminalParser;
import org.junit.Test;

public class TerminalParserTests {

	@Test
	public void test() {
		Token s = TerminalParser.token("let").from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse("let");
		assertEquals("let", s.toString());
	}

	@Test
	public void test2() {
		chkParser("let", "let");
		chkParser(">", ">");
		chkParser(">=", ">=");
		chkParser("$$", "$$");
	}
	
	private Parser<String> ss() {
		return Parsers.or(TerminalParser.token("let"), TerminalParser.token("end")).retn("abc");
	}
	@Test
	public void test3() {
		String s = ss().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse("let");
		assertEquals("abc", s);
	}

    public static Parser<NumberExp> xnumbervalueassign() {
        return Parsers.or(TerminalParser.numberSyntacticParser).
                map(new org.jparsec.functors.Map<String, NumberExp>() {
                    @Override
                    public NumberExp map(String arg0) {
                        return new NumberExp(Double.parseDouble(arg0));
                    }
                });
    }
    @Test
    public void test4a() {
        Exp exp = xnumbervalueassign().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse("5.2");
        assertEquals("5.2", exp.strValue());
        exp = xnumbervalueassign().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse("52");
        assertEquals("52.0", exp.strValue());
    }
	
	
	private void chkParser(String input, String output) {
		Token s = TerminalParser.token(input).from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(input);
		assertEquals(output, s.toString());
	}
}
