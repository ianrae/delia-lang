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
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
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
			QueryResponse qresp = dbexecutor.executeHLSQuery(hls, sql, qtx);
			return qresp;
		}
	}
	
	//normally we just call db directly. one 'let' statement = one call to db
	public static class DoubleHLSStragey implements HLSStragey {

		@Override
		public QueryResponse execute(HLSQueryStatement hls, String sql, QueryContext qtx, DBExecutor dbexecutor) {
			
			HLSQuerySpan hlspan1 = hls.hlspanL.get(1); //Address
			HLSQuerySpan hlspan2 = hls.hlspanL.get(0); //Customer
			QueryResponse qresp = dbexecutor.executeHLSQuery(hls, sql, qtx);
			
			//and again with span1
			HLSQueryStatement clone = new HLSQueryStatement();
			//{Address->Address,MT:Address,[cust in [55,56],()}
			
			//src = "let x = Address[cust in [55,56]]
			
			clone.hlspanL.add(hlspan1);
			clone.queryExp = hls.queryExp;
			clone.querySpec = hls.querySpec;
			QueryResponse qresp2 = dbexecutor.executeHLSQuery(clone, sql, qtx);
			
			
			return qresp;
		}
		
	}
	
	
	public static class HLSManagerResult {
		public String sql;
		public QueryResponse qresp;
	}
	
	/**
	 * MEM doesn't need sql. Only for some unit tests.
	 * @author Ian Rae
	 *
	 */
	public static class DoNothingSQLGenerator implements HLSSQLGenerator {
		private HLSSQLGenerator inner; //may be null

		public DoNothingSQLGenerator(HLSSQLGenerator inner) {
			this.inner = inner;
		}
		@Override
		public String buildSQL(HLSQueryStatement hls) {
			if (inner != null) {
				return inner.buildSQL(hls);
			}
			return null;
		}

		@Override
		public String processOneStatement(HLSQuerySpan hlspan, boolean forceAllFields) {
			if (inner != null) {
				return inner.processOneStatement(hlspan, forceAllFields);
			}
			return null;
		}

		@Override
		public void setRegistry(DTypeRegistry registry) {
			if (inner != null) {
				inner.setRegistry(registry);
			}
		}
		
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
		private boolean generateSQLforMemFlag;

		public HLSManager(FactoryService factorySvc, DTypeRegistry registry, DBInterface dbInterface) {
			super(factorySvc);
			this.dbInterface= dbInterface;
			this.registry = registry;
			this.assocTblMgr = new AssocTblManager();
		}
		
		public HLSManagerResult execute(QuerySpec spec, QueryContext qtx, DBExecutor dbexecutor) {
			HLSQueryStatement hls = buildHLS(spec.queryExp);
			hls.querySpec = spec;
			
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

			HLSSQLGenerator gen = new HLSSQLGeneratorImpl(factorySvc, this.assocTblMgr);
			switch(dbInterface.getDBType()) {
			case MEM:
			{
				if (generateSQLforMemFlag) {
					return new DoNothingSQLGenerator(gen);
				} else {
					return new DoNothingSQLGenerator(null);
				}
			}
			case H2:
			case POSTGRES:
			default:
				return gen;
			}
		}

		private HLSStragey chooseStrategy(HLSQueryStatement hls) {
			if (needDoubleStrategy(hls)) {
				return new DoubleHLSStragey();
			}
			return defaultStrategy;
		}
		
		private boolean needDoubleStrategy(HLSQueryStatement hls) {
			if (hls.hlspanL.size() == 2) {
//				HLSQuerySpan hlspan1 = hls.hlspanL.get(1); //Address
				HLSQuerySpan hlspan2 = hls.hlspanL.get(0); //Customer
				
				QueryType queryType = detectQueryType(hls.queryExp, hlspan2);
				switch(queryType) {
				case OP:
					return true;
				case ALL_ROWS:
				case PRIMARY_KEY:
					default:
						return false;
				}
			} 
			return false;
		}
		private QueryType detectQueryType(QueryExp queryExp, HLSQuerySpan hlspan) {
			QueryTypeDetector queryTypeDetector = new QueryTypeDetector(factorySvc, registry);
			QuerySpec spec = new QuerySpec();
			spec.queryExp = queryExp;
			QueryType queryType = queryTypeDetector.detectQueryType(spec);
			return queryType;
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
		
		public boolean isGenerateSQLforMemFlag() {
			return generateSQLforMemFlag;
		}

		public void setGenerateSQLforMemFlag(boolean generateSQLforMemFlag) {
			this.generateSQLforMemFlag = generateSQLforMemFlag;
		}
		
		
		
	}

	
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
		
		HLSManager mgr = new HLSManager(delia.getFactoryService(), session.getExecutionContext().registry, delia.getDBInterface());
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
