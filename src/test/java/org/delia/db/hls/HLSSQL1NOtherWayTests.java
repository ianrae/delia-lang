package org.delia.db.hls;



import org.junit.Before;
import org.junit.Test;

/**
 * HLS = High Level SQL
 * 
 * 
 * @author Ian Rae
 *
 */
public class HLSSQL1NOtherWayTests extends HLSTestBase {

	@Test
	public void testOneSpanSubSQL() {
		useCustomer1NOtherWaySrc = true;
		//TODO: don't actually need both a.addr and b.id. they are the same value
		sqlchkP("let x = Customer[55].fks()", 					"SELECT * FROM Customer as a WHERE a.cid = ?", "55");
		sqlchk("let x = Customer[true].fetch('addr')", 			"SELECT a.cid,a.x,a.addr,b.id as addr,b.y FROM Customer as a LEFT JOIN Address as b ON a.addr=b.id");
		sqlchk("let x = Customer[true].fetch('addr').first()", 	"SELECT TOP 1 a.cid,a.x,a.addr,b.id as addr,b.y FROM Customer as a LEFT JOIN Address as b ON a.addr=b.id");
		sqlchk("let x = Customer[true].fetch('addr').orderBy('cid')", "SELECT a.cid,a.x,a.addr,b.id as addr,b.y FROM Customer as a LEFT JOIN Address as b ON a.addr=b.id ORDER BY a.cid");
		
		//the following sql works but we could not do the join since its not needed.
		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a LEFT JOIN Address as b ON a.addr=b.id");
		sqlchk("let x = Customer[true].x.fks()", 				"SELECT a.x FROM Customer as a");
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
		useCustomer1NOtherWaySrc = true;
		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a LEFT JOIN Address as b ON a.addr=b.id");

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
//	private void sqlchk(String src, String sqlExpected) {
//		HLSQueryStatement hls = buildHLS(src);
//		HLSSQLGenerator gen = createGen();
//		String sql = gen.buildSQL(hls);
//		log.log("sql: " + sql);
//		assertEquals(sqlExpected, sql);
//	}
}
