package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * HLS = High Level SQL
 * 
 * 
 * @author Ian Rae
 *
 */
public class HLSSQL1NTests extends HLSTestBase {

	@Test
	public void testOneSpanSubSQL() {
		useCustomer1NSrc = true;
		sqlchkP("let x = Customer[55].fks()", 					"SELECT a.cid,a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.id=b.cust WHERE a.cid = ?", "55");
		sqlchk("let x = Customer[true].fetch('addr')", 			"SELECT a.cid,a.x,b.id,b.y,b.cust FROM Customer as a LEFT JOIN Address as b ON a.id=b.cust");
		sqlchk("let x = Customer[true].fetch('addr').first()", 	"SELECT TOP 1 a.cid,a.x,b.id,b.y,b.cust FROM Customer as a LEFT JOIN Address as b ON a.id=b.cust");
		sqlchk("let x = Customer[true].fetch('addr').orderBy('id')", "SELECT a.cid,a.x,b.id,b.y,b.cust FROM Customer as a LEFT JOIN Address as b ON a.id=b.cust ORDER BY a.id");
		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a");
		sqlchk("let x = Customer[true].x.fks()", 				"SELECT a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.id=b.cust");
	}

	//	@Test
	//	public void testOneRelationSQL() {
	//		useCustomerSrc = true;
	//		chk("let x = Customer[true].addr", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,()}");
	//		
	//		chk("let x = Customer[true].fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true}");
	//		chk("let x = Customer[true].fetch('addr')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr}");
	//		
	//		chk("let x = Customer[true].fetch('addr').first()", "{Customer->Customer,MT:Customer,[true],(first),SUB:false,addr}");
	//		chk("let x = Customer[true].fetch('addr').orderBy('id')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr,OLO:id,null,null}");
	//
	//		//this one doesn't need to do fetch since just getting x
	//		chk("let x = Customer[true].x.fetch('addr')", "{Customer->int,MT:Customer,[true],F:x,()}");
	//		
	//		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
	//		
	//		chk("let x = Customer[true].addr.fks()", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),SUB:true}");
	//		chk("let x = Customer[true].fks().addr", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,()}");
	//		chk("let x = Customer[true].fks().addr.fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,(),SUB:true}");
	//		
	//		chk("let x = Customer[true].addr.orderBy('id')", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),OLO:id,null,null}");
	//		chk("let x = Customer[true].orderBy('id').addr", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,()}");
	//		chk("let x = Customer[true].orderBy('id').addr.orderBy('y')", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,(),OLO:y,null,null}");
	//	}


	@Test
	public void testDebugSQL() {
		useCustomer1NSrc = true;

		//		
//		sqlchk("let x = Customer[true].x.fks()", "SELECT a.x,b.id FROM Customer as a LEFT JOIN Address as b ON a.id=b.cust");
		sqlchkP("let x = Customer[55].fks()", 					"SELECT a.cid,a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.id=b.cust WHERE a.cid = ?", "55");
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
