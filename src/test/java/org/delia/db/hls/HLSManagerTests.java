package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.manager.HLSManager;
import org.delia.db.hls.manager.HLSManagerResult;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * HLS = High Level SQL
 * 
 * 
 * @author Ian Rae
 *
 */
public class HLSManagerTests extends HLSTestBase {
	
	@Test
	public void test1() {
		QueryResponse qresp = sqlchk("let x = Flight[true]", "SELECT * FROM Flight as a");
		List<DValue> list = qresp.dvalList;
		assertEquals(2, list.size());
	}	
	
	@Test
	public void testDoubleStratey() {
		insertSomeRecords = true;
		useCustomerManyToManySrc = true;
		generateSQLforMemFlag = false;
//		//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv 
//		sqlchk("let x = Customer[true].addr", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv");
//		//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv WHERE b.leftv=55 
//		sqlchk("let x = Customer[55].addr", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv WHERE b.leftv=55");

		//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv LEFT JOIN Customer as c ON b.leftv=c.id AND c.x > 10 
		QueryResponse qresp = sqlchk("let x = Customer[x >= 10].addr", null);
		
		List<DValue> list = qresp.dvalList;
		assertEquals(2, list.size());
		DValue dval = list.get(0);
		assertEquals(100, dval.asStruct().getField("id").asInt());
		DValue inner = dval.asStruct().getField("cust");
		DRelation drel = inner.asRelation();
		assertEquals(55, drel.getForeignKey().asInt());
		assertEquals(true, drel.haveFetched());
		
		dval = list.get(1);
		assertEquals(101, dval.asStruct().getField("id").asInt());
		inner = dval.asStruct().getField("cust");
		drel = inner.asRelation();
		assertEquals(55, drel.getForeignKey().asInt());
		assertEquals(false, drel.haveFetched());
	}	
	

	@Test
	public void testDebugSQL() {
		useCustomerManyToManySrc = true;
		assocTblMgr.flip = false;

		//		sqlchk("let x = Customer[true].fks()", "SELECT a.cid,a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.cid=b.leftv");
		//		sqlchk("let x = Customer[true].fetch('addr')", "SELECT a.cid,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.cid=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv");
		//		
		//		//this one doesn't need to do fetch since just getting x
		//		
		//		sqlchk("let x = Customer[true].addr.fks()", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv");
		//		sqlchk("let x = Customer[true].fks().addr", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv");
		//		sqlchk("let x = Customer[true].fks().addr.fks()", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv");

		//		sqlchk("let x = Customer[true].addr.orderBy('id')", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv ORDER BY a.id");
		//		sqlchk("let x = Customer[true].orderBy('cid').addr", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv ORDER BY b.leftv");

		//select * from address where cust in (select * from Customer order by x desc limit 1)		
		//		sqlchk("let x = Customer[true].orderBy('x').addr", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,()}");
		//		chk("let x = Customer[true].orderBy('id').addr.orderBy('y')", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,(),OLO:y,null,null}");

	}

	//-------------------------
	private boolean generateSQLforMemFlag = true;
	
	@Before
	public void init() {
		createDao();
	}

	private QueryResponse sqlchk(String src, String sqlExpected) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		
		HLSManager mgr = new HLSManager(session);
		mgr.setGenerateSQLforMemFlag(generateSQLforMemFlag);
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		QueryContext qtx = new QueryContext();
		DBAccessContext dbctx = new DBAccessContext(session.getExecutionContext().registry, null);
		DBExecutor dbexecutor = delia.getDBInterface().createExector(dbctx);
		HLSManagerResult result = mgr.execute(spec, qtx, dbexecutor);
		assertEquals(sqlExpected, result.sql);
		return result.qresp;
	}
}
