package org.delia.parser;

import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.RuleExp;
import org.delia.compiler.ast.RuleSetExp;
import org.delia.compiler.parser.RuleParser;
import org.delia.compiler.parser.TerminalParser;
import org.junit.Test;



public class RuleTests {
	
	@Test
	public void test1() {
		RuleExp exp = parseRule("x < 5");
		chkOpRule(exp, "x", "<", "5");
	}
	@Test
	public void test2() {
		RuleSetExp exp = parseRules("x < 5");
		assertEquals(1, exp.ruleL.size());
		chkOpRule(exp.ruleL.get(0), "x", "<", "5");
	}
	@Test
	public void test2a() {
		RuleSetExp exp = parseRules("x < 5,y < 10");
		assertEquals(2, exp.ruleL.size());
		chkOpRule(exp.ruleL.get(0), "x", "<", "5");
		chkOpRule(exp.ruleL.get(1), "y", "<", "10");
	}
	
	@Test
	public void testFn1() {
		RuleSetExp exp = parseRules("len()");
		assertEquals(1, exp.ruleL.size());
		RuleHelper.chkFuncRule(exp.ruleL.get(0), 1, "len", 0);
		RuleHelper.chkFuncRulePolarity(exp.ruleL.get(0), true);
	}
	@Test
	public void testNotFn1() {
		RuleSetExp exp = parseRules("!len()");
		assertEquals(1, exp.ruleL.size());
		RuleHelper.chkFuncRule(exp.ruleL.get(0), 1, "len", 0);
		RuleHelper.chkFuncRulePolarity(exp.ruleL.get(0), false);
	}
	@Test
	public void testFn2() {
		RuleSetExp exp = parseRules("x < 5,len(15)");
		assertEquals(2, exp.ruleL.size());
		chkOpRule(exp.ruleL.get(0), "x", "<", "5");
		RuleHelper.chkFuncRule(exp.ruleL.get(1), 1, "len", 1);
	}
	@Test
	public void testFn1a() {
		RuleSetExp exp = parseRules("x.len()");
		assertEquals(1, exp.ruleL.size());
		RuleHelper.chkFuncRule(exp.ruleL.get(0), 2, "x", 0);
	}
	@Test
	public void testFn2a() {
		RuleSetExp exp = parseRules("xyz(),len(15)");
		assertEquals(2, exp.ruleL.size());
		RuleHelper.chkFuncRule(exp.ruleL.get(1), 1, "len", 1);
	}
	@Test
	public void testFn2aFail() {
		RuleSetExp exp = parseRules("xyz(14) len(15)");
		assertEquals(1, exp.ruleL.size()); //TODO fix this!!
		RuleHelper.chkFuncRule(exp.ruleL.get(0), 2, "xyz", 1);
	}
	@Test
	public void testFn2aOK() {
		RuleSetExp exp = parseRules("xyz(3), len(15)");
		assertEquals(2, exp.ruleL.size());
		RuleHelper.chkFuncRule(exp.ruleL.get(0), 1, "xyz", 1);
	}
	
	// --
	
	private RuleExp parseRule(String src) {
		RuleParser.initLazy();		
		RuleExp exp = RuleParser.oneRule().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return exp;
	}
	private RuleSetExp parseRules(String src) {
		RuleParser.initLazy();		
		RuleSetExp exp = RuleParser.rules().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return exp;
	}
	private void chkOpRule(RuleExp exp, String op1, String op, String op2) {
		RuleHelper.chkOpRule(exp, op1, op, op2);
	}
}
