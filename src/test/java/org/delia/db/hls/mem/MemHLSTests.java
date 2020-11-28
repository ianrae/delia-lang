package org.delia.db.hls.mem;


import org.delia.assoc.CreateNewDatIdVisitor;
import org.delia.builder.DeliaBuilder;
import org.delia.core.FactoryService;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.QueryContext;
import org.delia.db.hls.AliasManager;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.HLSTestBase;
import org.delia.runner.QueryResponse;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.mem.MemZDBExecutor;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Rewrite MEM query code to use HLS.
 *  Replace the existing zdb query and separate spanEngine for .addr,fetch,etc 
 * GOAL: New code will use HLS to do entire query in zdb layer
 * GOAL: DeliaOptions.useHLS default to true. in fact get rid of useHLS completely
 * 
 * -QueryExp contains type + [filter] + fields..or..fns
 * -LetSpan holds the field/fns part
 * -so Customer[55] has no spans
 * -HLS turns these into HLSQuerySpans 
 *   -if spanL empty then we create a single HLSQuerySpan
 *   -else one HLSQuerySpan per span
 *   
 * Note. in MEM we need to prune rel DVals so is same as H2/PG.  
 *   -remove all parent rel values (unless fks or fetch)
 *   -leave ManyToMany because that's what we do with SQL
 *     
 * @author Ian Rae
 *
 */
public class MemHLSTests extends HLSTestBase {
	
	public class HLSMemZDBExecutor extends MemZDBExecutor {

		public HLSMemZDBExecutor(FactoryService factorySvc, MemZDBInterfaceFactory dbInterface) {
			super(factorySvc, dbInterface);
		}

//		@Override
//		public QueryResponse rawQuery(QuerySpec spec, QueryContext qtx) {
//			QueryResponse qresp = new QueryResponse();
//
//			try {
//				qresp = doExecuteQuery(spec, qtx);
//			} catch (InternalException e) {
//				qresp.ok = false;
//				qresp.err = e.getLastError();
//			}
//
//			return qresp;
//		}


		@Override
		public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx) {
			log.log("ziggy!");
			qtx.pruneParentRelationFlag = false;
			qtx.loadFKs = false;
			return this.doExecuteQuery(hls.querySpec, qtx);
		}
	}	
	
	public class HLSMemZDBInterfaceFactory extends MemZDBInterfaceFactory {
		
		public HLSMemZDBInterfaceFactory(FactoryService factorySvc) {
			super(factorySvc);
		}
		
		@Override
		public ZDBExecutor createExecutor() {
			return new HLSMemZDBExecutor(factorySvc, this);
		}
	}	

	@Test
	public void testOneSpanSubSQL() {
		useCustomer11Src = true;
		sqlchkP("let x = Customer[55].fks()", 					"SELECT a.cid,a.x,b.id as addr FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust WHERE a.cid = ?", "55");
	}

	@Test
	public void testDebugSQL() {
		useCustomer11Src = true;

	}

	//---
	
	@Before
	public void init() {
//		createDao();
	}
	
	@Override
	protected DeliaGenericDao createDao() {
		DeliaBuilder builder = new DeliaBuilder();
		FactoryService factorySvc = builder.createFactorySvcEx();
		HLSMemZDBInterfaceFactory dbInterface = new HLSMemZDBInterfaceFactory(factorySvc);
		this.delia = builder.buildEx(dbInterface, factorySvc);
		MemZDBInterfaceFactory memDBinterface = (MemZDBInterfaceFactory) delia.getDBInterface();
		memDBinterface.createSingleMemDB();
		CreateNewDatIdVisitor.hackFlag = true;
		
		this.delia.getOptions().useHLS = true;
		
		if (flipAssocTbl) {
			createTable(memDBinterface, "AddressCustomerDat1");
		} else {
			createTable(memDBinterface, "CustomerAddressDat1");
		}
		aliasManager = new AliasManager(delia.getFactoryService());
		
		return new DeliaGenericDao(delia);
	}
	

	private void sqlchkP(String src, String sqlExpected, String param1) {
		doSqlchkP(src, sqlExpected, param1);
	}
}
