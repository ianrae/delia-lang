package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.zqueryresponse.LetSpan;
import org.delia.zqueryresponse.LetSpanEngine;
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
	
	public interface HLSStragey {
		QueryResponse execute(HLSQueryStatement hls, String sql, QueryContext qtx, DBExecutor dbexecutor);
	}
	
	//normally we just call db directly. one 'let' statement = one call to db
	public static class StandardHLSStragey implements HLSStragey {

		@Override
		public QueryResponse execute(HLSQueryStatement hls, String sql, QueryContext qtx, DBExecutor dbexecutor) {
			QueryResponse qresp = null; //dbexecutor.executeHLSQuery(hls, sql, qtx);
			return qresp;
		}
		
	}
	
	public static class HLSManagerResult {
		public String sql;
		public QueryResponse qresp;
	}
	
	/**
	 * Facade between Delia Runner and the db. Allows us to have different strategies
	 * for executing 'let' statement queries.
	 * Normally each query turns into a single SQL call to DBInterface,
	 * but some require multiple calls and additional handling.
	 * This layer selects a strategy object to execute the query and runs it.
	 * @author Ian Rae
	 *
	 */
	public static class HLSManager extends ServiceBase {

		private DBInterface dbInterface;
		private DTypeRegistry registry;
		private AssocTblManager assocTblMgr;
		private HLSStragey defaultStrategy = new StandardHLSStragey();

		public HLSManager(FactoryService factorySvc, DTypeRegistry registry, DBInterface dbInterface) {
			super(factorySvc);
			this.dbInterface= dbInterface;
			this.registry = registry;
			this.assocTblMgr = new AssocTblManager();
		}
		
		public HLSManagerResult execute(QuerySpec spec, QueryContext qtx, DBExecutor dbexecutor) {
			HLSQueryStatement hls = buildHLS(spec.queryExp);
			
			HLSSQLGenerator sqlGenerator = chooseGenerator();
			sqlGenerator.setRegistry(registry);
			String sql = sqlGenerator.buildSQL(hls);
			
			HLSStragey strategy = chooseStrategy(hls);
			strategy.execute(hls, sql, qtx, dbexecutor);

			QueryResponse qresp = dbexecutor.executeQuery(spec, qtx);
			HLSManagerResult result = new HLSManagerResult();
			result.qresp = qresp;
			result.sql = sql;
			return result;
		}
		
		private HLSSQLGenerator chooseGenerator() {
			//later we will have dbspecific ones
			return new HLSSQLGeneratorImpl(factorySvc, this.assocTblMgr);
		}

		private HLSStragey chooseStrategy(HLSQueryStatement hls) {
			return defaultStrategy;
		}

		public HLSQueryStatement buildHLS(QueryExp queryExp) {
			LetSpanEngine letEngine = new LetSpanEngine(factorySvc, registry, null, null); //TODO what are these nulls?
			List<LetSpan> spanL = letEngine.buildAllSpans(queryExp);
			
			HLSEngine hlsEngine = new HLSEngine(factorySvc, registry);
			HLSQueryStatement hls = hlsEngine.generateStatement(queryExp, spanL);
			
			for(HLSQuerySpan hlspan: hls.hlspanL) {
				String hlstr = hlspan.toString();
				log.log(hlstr);
			}
			return hls;
		}
		
		public String generateSQL(HLSQueryStatement hls) {
//			HLSSQLGenerator gen = new HLSSQLGenerator(factorySvc, assocTblMgr);
//			gen.setRegistry(registry);
//			String sql = gen.buildSQL(hls);
//			log.log("sql: " + sql);
			return null;
		}
		
		
		
	}

	
	@Test
	public void test1() {
		sqlchk("let x = Flight[true]", "SELECT * FROM Flight as a");
	}	
	
	
	//	@Test
	//	public void testOneSpanNoSubSQL() {
	//		sqlchk("let x = Flight[55]", 			"SELECT * FROM Flight as a WHERE a.id=55");
	//		sqlchk("let x = Flight[55].count()", 	"SELECT COUNT(*) FROM Flight as a WHERE a.id=55");
	//		sqlchk("let x = Flight[55].first()", 	"SELECT TOP 1 * FROM Flight as a WHERE a.id=55");
	//		sqlchk("let x = Flight[true]", 			"SELECT * FROM Flight as a");
	//
	//		sqlchk("let x = Flight[55].field1", 						"SELECT a.field1 FROM Flight as a WHERE a.id=55");
	//		sqlchk("let x = Flight[55].field1.min()", 					"SELECT MIN(a.field1) FROM Flight as a WHERE a.id=55");
	//		sqlchk("let x = Flight[55].field1.orderBy('field2')", 		"SELECT a.field1 FROM Flight as a WHERE a.id=55 ORDER BY a.field2");
	//		sqlchk("let x = Flight[55].field1.orderBy('field2').offset(3)", "SELECT a.field1 FROM Flight as a WHERE a.id=55 ORDER BY a.field2 OFFSET 3");
	//		sqlchk("let x = Flight[55].field1.orderBy('field2').offset(3).limit(5)", "SELECT a.field1 FROM Flight as a WHERE a.id=55 ORDER BY a.field2 LIMIT 5 OFFSET 3");
	//		sqlchk("let x = Flight[55].field1.count()", 				"SELECT COUNT(a.field1) FROM Flight as a WHERE a.id=55");
	//		sqlchk("let x = Flight[55].field1.distinct()", 				"SELECT DISTINCT(a.field1) FROM Flight as a WHERE a.id=55");
	//		sqlchk("let x = Flight[55].field1.exists()", 				"SELECT COUNT(a.field1) FROM Flight as a WHERE a.id=55 LIMIT 1");
	//
	//	}
	//
	//	@Test
	//	public void testOneSpanSubSQL() {
	//		useCustomerManyToManySrc = true;
	//		assocTblMgr.flip = false;
	//		sqlchk("let x = Customer[55].fks()", 					"SELECT a.cid,a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.cid=b.leftv WHERE a.cid=55");
	//		sqlchk("let x = Customer[true].fetch('addr')", 			"SELECT a.cid,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.cid=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv");
	//		sqlchk("let x = Customer[true].fetch('addr').first()", 	"SELECT TOP 1 a.cid,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.cid=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv");
	//		sqlchk("let x = Customer[true].fetch('addr').orderBy('cid')", "SELECT a.cid,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.cid=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv ORDER BY a.cid");
	//		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a");
	//		sqlchk("let x = Customer[true].x.fks()", 				"SELECT a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.cid=b.leftv");
	//	}
	//
	//		@Test
	//		public void testOneRelationSQL() {
	//			useCustomerManyToManySrc = true;
	//			
	//			//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv 
	//			sqlchk("let x = Customer[true].addr", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv");
	//
	//			//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv WHERE b.leftv=55 
	//			sqlchk("let x = Customer[55].addr", "SELECT a.id,a.y,b.rightv FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv WHERE b.leftv=55");
	//
	//			//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv LEFT JOIN Customer as c ON b.leftv=c.id AND c.x > 10 
	////			sqlchk("let x = Customer[x > 10].addr", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,()}");
	//			
	////			chk("let x = Customer[true].addr.fks()", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),SUB:true}");
	////			chk("let x = Customer[true].fks().addr", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,()}");
	////			chk("let x = Customer[true].fks().addr.fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,(),SUB:true}");
	////			
	////			chk("let x = Customer[true].addr.orderBy('id')", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),OLO:id,null,null}");
	////			chk("let x = Customer[true].orderBy('id').addr", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,()}");
	////			chk("let x = Customer[true].orderBy('id').addr.orderBy('y')", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,(),OLO:y,null,null}");
	//		}
	//
	//	@Test
	//	public void testAssocTableFlip() {
	//		useCustomerManyToManySrc = true;
	//		assocTblMgr.flip = true;
	//		sqlchk("let x = Customer[true].x.fks()", "SELECT a.x,b.leftv FROM Customer as a LEFT JOIN AddressCustomerAssoc as b ON a.cid=b.rightv");
	//		
	//		assocTblMgr.flip = false;
	//		sqlchk("let x = Customer[true].x.fks()", "SELECT a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.cid=b.leftv");
	//	}
	//
	//	

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


	@Before
	public void init() {
		createDao();
	}



	private void sqlchk(String src, String sqlExpected) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		
		HLSManager mgr = new HLSManager(delia.getFactoryService(), session.getExecutionContext().registry, delia.getDBInterface());
		
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		QueryContext qtx = new QueryContext();
		DBAccessContext dbctx = new DBAccessContext(session.getExecutionContext().registry, null);
		DBExecutor dbexecutor = delia.getDBInterface().createExector(dbctx);
		HLSManagerResult result = mgr.execute(spec, qtx, dbexecutor);
		assertEquals(sqlExpected, result.sql);
	}



}
