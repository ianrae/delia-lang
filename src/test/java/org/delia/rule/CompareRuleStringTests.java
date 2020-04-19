package org.delia.rule;

import org.delia.db.memdb.MemDBInterface;
import org.junit.Before;
import org.junit.Test;

public class CompareRuleStringTests extends CompareRuleTestBase {
	
	@Test
	public void testLT() {
		ruleText = "wid < 'bet'";
		chkString("");
		chkString("a");
		chkString("bat");
		chkString("BET");
		chkStringFail("bet");
		chkStringFail("betty");
		chkStringFail("cat");
	}
	@Test
	public void testLE() {
		ruleText = "wid <= 'bet'";
		chkString("");
		chkString("a");
		chkString("bat");
		chkString("bet");
		chkStringFail("betty");
		chkStringFail("cat");
	}
	@Test
	public void testGT() {
		ruleText = "wid > 'bet'";
		chkStringFail("");
		chkStringFail("a");
		chkStringFail("bat");
		chkStringFail("bet");
		chkString("betty");
		chkString("cat");
	}
	@Test
	public void testGE() {
		ruleText = "wid >= 'bet'";
		chkStringFail("");
		chkStringFail("a");
		chkStringFail("bat");
		chkString("bet");
		chkString("betty");
		chkString("cat");
	}
	@Test
	public void testEQ() {
		ruleText = "wid == 'bet'";
		chkStringFail("");
		chkStringFail("a");
		chkStringFail("bat");
		chkString("bet");
		chkStringFail("betty");
		chkStringFail("cat");
	}
	@Test
	public void testNE() {
		ruleText = "wid != 'bet'";
		chkString("");
		chkString("a");
		chkString("bat");
		chkStringFail("bet");
		chkString("betty");
		chkString("cat");
	}
	
	
	// --

	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private void chkString(String val) {
		String s = String.format("wid: '%s'", val);
		chkPass("string", s);
	}
	private void chkStringFail(String val) {
		String s = String.format("wid: '%s'", val);
		chkFail("string", s);
	}
}
