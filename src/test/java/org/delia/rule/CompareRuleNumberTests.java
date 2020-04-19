package org.delia.rule;

import org.delia.db.memdb.MemDBInterface;
import org.junit.Before;
import org.junit.Test;

public class CompareRuleNumberTests extends CompareRuleTestBase {
	
	@Test
	public void testLT() {
		ruleText = "wid < 10.5";
		chkNumber(-1);
		chkNumber(0);
		chkNumber(10.48);
		chkNumberFail(10.5);
		chkNumberFail(20.0);
	}
	@Test
	public void testLE() {
		ruleText = "wid <= 10.5";
		chkNumber(-1.2);
		chkNumber(0);
		chkNumber(9.4);
		chkNumber(10.48);
		chkNumber(10.5);
		chkNumberFail(10.5001);
		//TODO does delia need an operator to compare number within some delta??
		chkNumberFail(20.0);
	}
	@Test
	public void testGT() {
		ruleText = "wid > 10.5";
		chkNumberFail(-1.2);
		chkNumberFail(0);
		chkNumberFail(9.4);
		chkNumberFail(10.48);
		chkNumberFail(10.5);
		chkNumber(10.5001);
		chkNumber(20.0);
	}
	@Test
	public void testGE() {
		ruleText = "wid >= 10.5";
		chkNumberFail(-1.2);
		chkNumberFail(0);
		chkNumberFail(9.4);
		chkNumberFail(10.48);
		chkNumber(10.5);
		chkNumber(10.5001);
		chkNumber(20.0);
	}
	@Test
	public void testEQ() {
		ruleText = "wid == 10.5";
		chkNumberFail(9);
		chkNumberFail(10.48);
		chkNumber(10.5);
		chkNumberFail(10.5001);
		chkNumberFail(20.0);
	}
	@Test
	public void testNE() {
		ruleText = "wid != 10.5";
		chkNumber(9);
		chkNumber(10.48);
		chkNumberFail(10.5);
		chkNumber(10.5001);
		chkNumber(20.0);

		//<> Not supported
	}
	
	
	// --

	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private void chkNumber(double val) {
		String s = String.format("wid: %g", val);
		chkPass("number", s);
	}
	private void chkNumberFail(double val) {
		String s = String.format("wid: %g", val);
		chkFail("number", s);
	}
}
