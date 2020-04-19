package org.delia.rule;

import org.delia.db.memdb.MemDBInterface;
import org.junit.Before;
import org.junit.Test;

public class CompareRuleDateTests extends CompareRuleTestBase {
	
	//TODO: need to also compare tz in WrappedDate!!
	//TODO: these tests only test to nearest sec. add more tests for msecs
	
	@Test
	public void testLT() {
		ruleText = "wid < '2020-01-31T09:59:51'";
		chkDate("2020");
		chkDate("2020-01-31T09:59:50");
		chkDateFail("2020-01-31T09:59:51");
		chkDateFail("2020-01-31T09:59:52");
		chkDateFail("2021");
	}
	@Test
	public void testLE() {
		ruleText = "wid <= '2020-01-31T09:59:51'";
		chkDate("2020");
		chkDate("2020-01-31T09:59:50");
		chkDate("2020-01-31T09:59:51");
		chkDateFail("2020-01-31T09:59:52");
		chkDateFail("2021");
	}
	@Test
	public void testGT() {
		ruleText = "wid > '2020-01-31T09:59:51'";
		chkDateFail("2020");
		chkDateFail("2020-01-31T09:59:50");
		chkDateFail("2020-01-31T09:59:51");
		chkDate("2020-01-31T09:59:52");
		chkDate("2021");
	}
	@Test
	public void testGE() {
		ruleText = "wid >= '2020-01-31T09:59:51'";
		chkDateFail("2020");
		chkDateFail("2020-01-31T09:59:50");
		chkDate("2020-01-31T09:59:51");
		chkDate("2020-01-31T09:59:52");
		chkDate("2021");
	}
	@Test
	public void testEQ() {
		ruleText = "wid == '2020-01-31T09:59:51'";
		chkDateFail("2020");
		chkDateFail("2020-01-31T09:59:50");
		chkDate("2020-01-31T09:59:51");
		chkDateFail("2020-01-31T09:59:52");
		chkDateFail("2021");
	}
	@Test
	public void testNE() {
		ruleText = "wid != '2020-01-31T09:59:51'";
		chkDate("2020");
		chkDate("2020-01-31T09:59:50");
		chkDateFail("2020-01-31T09:59:51");
		chkDate("2020-01-31T09:59:52");
		chkDate("2021");
	}
	
	
	// --

	@Before
	public void init() {
		super.init();
		enableAutoCreateTables();
	}
	
	private void chkDate(String val) {
		String s = String.format("wid: '%s'", val);
		chkPass("date", s);
	}
	private void chkDateFail(String val) {
		String s = String.format("wid: '%s'", val);
		chkFail("date", s);
	}
}
