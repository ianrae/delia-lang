package org.delia.rule;

import org.junit.Before;
import org.junit.Test;

public class CompareRuleIntTests extends CompareRuleTestBase {
	
	@Test
	public void testLT() {
		ruleText = "wid < 10";
		chkInt(-1);
		chkInt(0);
		chkInt(9);
		chkIntFail(10);
		chkIntFail(20);
	}
	@Test
	public void testLE() {
		ruleText = "wid <= 10";
		chkInt(-1);
		chkInt(0);
		chkInt(9);
		chkInt(10);
		chkIntFail(20);
	}
	@Test
	public void testGT() {
		ruleText = "wid > 10";
		chkIntFail(-1);
		chkIntFail(10);
		chkInt(11);
		chkInt(20);
	}
	@Test
	public void testGE() {
		ruleText = "wid >= 10";
		chkIntFail(-1);
		chkInt(10);
		chkInt(11);
		chkInt(20);
	}
	@Test
	public void testEQ() {
		ruleText = "wid == 10";
		chkIntFail(9);
		chkInt(10);
		chkIntFail(20);
	}
	@Test
	public void testNE() {
		ruleText = "wid != 10";
		chkInt(9);
		chkIntFail(10);
		chkInt(20);

		//Not supported
//		ruleText = "wid <> 10"; //alternate syntax
//		chkInt(9);
//		chkIntFail(10);
//		chkInt(20);
	}
	
	
	// --

	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private void chkInt(int val) {
		String s = String.format("wid: %d", val);
		chkPass("int", s);
	}
	private void chkIntFail(int val) {
		String s = String.format("wid: %d", val);
		chkFail("int", s);
	}
}
