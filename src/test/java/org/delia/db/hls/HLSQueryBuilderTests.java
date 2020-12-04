package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryBuilderServiceImpl;
import org.delia.db.QuerySpec;
import org.delia.db.hls.manager.HLSManager;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DType;
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
public class HLSQueryBuilderTests extends HLSTestBase {
	
	public static class HLSQueryBuilderService { // implements QueryBuilderService {
		
		private QueryBuilderService innerSvc;
		private HLSManager hlsManager;
		
		public HLSQueryBuilderService(QueryBuilderService innerSvc, HLSManager hlsManager) {
			this.innerSvc = innerSvc;
			this.hlsManager = hlsManager;
		}

//		@Override
//		public QueryExp createEqQuery(String typeName, String fieldName, DValue targetValue) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public QueryExp createPrimaryKeyQuery(String typeName, DValue keyValue) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public QueryExp createInQuery(String typeName, List<DValue> list, DType relType) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public QueryExp createCountQuery(String typeName) {
//			// TODO Auto-generated method stub
//			return null;
//		}

//		@Override
		public HLSQueryStatement createAllRowsQuery(String typeName) {
			QueryExp queryExp = innerSvc.createAllRowsQuery(typeName);
			QuerySpec querySpec = innerSvc.buildSpec(queryExp, new DoNothingVarEvaluator());
			HLSQueryStatement hls = hlsManager.buildStatementOnly(querySpec);
			return hls;
		}

//		@Override
//		public QuerySpec buildSpec(QueryExp exp, VarEvaluator varEvaluator) {
//			// TODO Auto-generated method stub
//			return null;
//		}
		
	}

	@Test
	public void testDebugSQL() {
		useCustomer1NSrc = true;
		compileQueryToLetStatement("");

		QueryBuilderService impl = new QueryBuilderServiceImpl(delia.getFactoryService());
		HLSManager hlsManager = new HLSManager(delia, session.getExecutionContext().registry, session, new DoNothingVarEvaluator());
		HLSQueryBuilderService svc = new HLSQueryBuilderService(impl, hlsManager);
		HLSQueryStatement hls = svc.createAllRowsQuery("Customer");
		HLSSQLGenerator gen = createGen();
		String sql = gen.buildSQL(hls);
		log.log("sql: " + sql);
		assertEquals("sdfsdf", sql);
		
		
//		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a LEFT JOIN Address as b ON a.cid=b.cust");
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
