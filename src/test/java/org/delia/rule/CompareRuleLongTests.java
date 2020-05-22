package org.delia.rule;

import org.junit.Before;
import org.junit.Test;

public class CompareRuleLongTests extends CompareRuleTestBase {
	
	@Test
	public void testLT() {
		ruleText = "wid < 10";
		chkLong(-1);
		chkLong(0);
		chkLong(9);
		chkLongFail(10);
		chkLongFail(20);
	}
	@Test
	public void testLE() {
		ruleText = "wid <= 10";
		chkLong(-1);
		chkLong(0);
		chkLong(9);
		chkLong(10);
		chkLongFail(20);
	}
	@Test
	public void testGT() {
		ruleText = "wid > 10";
		chkLongFail(-1);
		chkLongFail(10);
		chkLong(11);
		chkLong(20);
	}
	@Test
	public void testGE() {
		ruleText = "wid >= 10";
		chkLongFail(-1);
		chkLong(10);
		chkLong(11);
		chkLong(20);
	}
	@Test
	public void testEQ() {
		ruleText = "wid == 10";
		chkLongFail(9);
		chkLong(10);
		chkLongFail(20);
	}
	@Test
	public void testNE() {
		ruleText = "wid != 10";
		chkLong(9);
		chkLongFail(10);
		chkLong(20);

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
	
	private void chkLong(int val) {
		String s = String.format("wid: %d", val);
		chkPass("long", s);
	}
	private void chkLongFail(int val) {
		String s = String.format("wid: %d", val);
		chkFail("long", s);
	}
}
