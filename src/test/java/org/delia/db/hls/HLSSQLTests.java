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
public class HLSSQLTests extends HLSTestBase {
	
	@Test
	public void testOneSpanNoSubSQL() {
		sqlchkP("let x = Flight[55]", 			"SELECT * FROM Flight as a WHERE a.field1 = ?", "55");
		sqlchkP("let x = Flight[55].count()", 	"SELECT COUNT(*) FROM Flight as a WHERE a.field1 = ?", "55");
		sqlchkP("let x = Flight[55].first()", 	"SELECT TOP 1 * FROM Flight as a WHERE a.field1 = ?", "55");
		sqlchk("let x = Flight[true]", 			"SELECT * FROM Flight as a");

		sqlchkP("let x = Flight[55].field1", 						"SELECT a.field1 FROM Flight as a WHERE a.field1 = ?", "55");
		sqlchkP("let x = Flight[55].field1.min()", 					"SELECT MIN(a.field1) FROM Flight as a WHERE a.field1 = ?", "55");
		sqlchkP("let x = Flight[55].field1.orderBy('field2')", 		"SELECT a.field1 FROM Flight as a WHERE a.field1 = ? ORDER BY a.field2", "55");
		sqlchkP("let x = Flight[55].field1.orderBy('field2').offset(3)", "SELECT a.field1 FROM Flight as a WHERE a.field1 = ? ORDER BY a.field2 OFFSET 3", "55");
		sqlchkP("let x = Flight[55].field1.orderBy('field2').offset(3).limit(5)", "SELECT a.field1 FROM Flight as a WHERE a.field1 = ? ORDER BY a.field2 LIMIT 5 OFFSET 3", "55");
		sqlchkP("let x = Flight[55].field1.count()", 				"SELECT COUNT(a.field1) FROM Flight as a WHERE a.field1 = ?", "55");
		sqlchkP("let x = Flight[55].field1.distinct()", 				"SELECT DISTINCT(a.field1) FROM Flight as a WHERE a.field1 = ?", "55");
		sqlchkP("let x = Flight[55].field1.exists()", 				"SELECT COUNT(a.field1) FROM Flight as a WHERE a.field1 = ? LIMIT 1", "55");

	}

	@Test
	public void testOneSpanSubSQL() {
		useCustomerManyToManySrc = true;
		assocTblMgr.flip = false;
		sqlchkP("let x = Customer[55].fks()", 					"SELECT a.cid,a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.cid=b.leftv WHERE a.cid = ?", "55");
		sqlchk("let x = Customer[true].fetch('addr')", 			"SELECT a.cid,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.cid=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv");
		sqlchk("let x = Customer[true].fetch('addr').first()", 	"SELECT TOP 1 a.cid,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.cid=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv");
		sqlchk("let x = Customer[true].fetch('addr').orderBy('cid')", "SELECT a.cid,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.cid=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv ORDER BY a.cid");
		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a");
		sqlchk("let x = Customer[true].x.fks()", 				"SELECT a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.cid=b.leftv");
	}

		@Test
		public void testOneRelationSQL() {
			useCustomerManyToManySrc = true;
			
			//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv 
			sqlchk("let x = Customer[true].addr", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv");

			//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv WHERE b.leftv=55 
			sqlchkP("let x = Customer[55].addr", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv WHERE b.leftv = ?", "55");

			//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv LEFT JOIN Customer as c ON b.leftv=c.id AND c.x > 10 
//			sqlchk("let x = Customer[x > 10].addr", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,()}");
			
//			chk("let x = Customer[true].addr.fks()", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),SUB:true}");
//			chk("let x = Customer[true].fks().addr", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,()}");
//			chk("let x = Customer[true].fks().addr.fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,(),SUB:true}");
//			
//			chk("let x = Customer[true].addr.orderBy('id')", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),OLO:id,null,null}");
//			chk("let x = Customer[true].orderBy('id').addr", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,()}");
//			chk("let x = Customer[true].orderBy('id').addr.orderBy('y')", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,(),OLO:y,null,null}");
		}

	@Test
	public void testAssocTableFlip() {
		useCustomerManyToManySrc = true;
		assocTblMgr.flip = true;
		sqlchk("let x = Customer[true].x.fks()", "SELECT a.x,b.leftv FROM Customer as a LEFT JOIN AddressCustomerAssoc as b ON a.cid=b.rightv");
		
		assocTblMgr.flip = false;
		sqlchk("let x = Customer[true].x.fks()", "SELECT a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.cid=b.leftv");
	}

	
	@Test
	public void testDebugSQL() {
		useCustomerManyToManySrc = true;
//		assocTblMgr.flip = false;
		
		//TODO: fix WHERE b.leftv = ?"
		//currently we are generating a.id = ?
		sqlchkP("let x = Customer[55].addr", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv WHERE b.leftv = ?", "55");
	}

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
