package org.delia.db.hls;


import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class HLSTwoSpanTests extends HLSTestBase {

	//TODO: double spans need to be fixed. Customer[55].addr.cust should collapse to Custoemr[55] (at least for 1:1)
//	@Test
	public void test() {
		useCustomer11Src = true;
//		sqlchkP("let x = Customer[55]", 					"SELECT * FROM Customer as a WHERE a.cid = ?", "55");
//		sqlchkP("let x = Customer[55].addr", 					"SELECT * FROM Address as a WHERE a.cid = ?", "55");
		sqlchkP("let x = Customer[55].addr.cust", 					"SELECT * FROM Address as a WHERE a.cid = ?", "55");
	}


	@Test
	public void testDebugSQL() {
		useCustomer11Src = true;

//		sqlchk("let x = Customer[true].fetch('addr').orderBy('cid')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust ORDER BY a.cid");
//		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a");
//		sqlchkP("let x = Customer[addr < 111].fks()", 			"SELECT a.cid,a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust WHERE b.id < ?", "111");
		sqlchk("let x = Customer[true].x.fks()", 				"SELECT a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust");
	}

	//---
	
	@Before
	public void init() {
		createDao();
	}

	private void sqlchk(String src, String sqlExpected) {
		sqlchkP(src, sqlExpected, null);
	}
	private void sqlchkP(String src, String sqlExpected, String param1) {
		doSqlchkP(src, sqlExpected, param1);
	}
}
