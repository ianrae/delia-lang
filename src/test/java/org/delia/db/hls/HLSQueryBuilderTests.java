package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.api.Delia;
import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.DBHelper;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryBuilderServiceImpl;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.manager.HLSManager;
import org.delia.db.hls.manager.HLSManagerResult;
import org.delia.queryresponse.LetSpanEngine;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.zdb.ZDBExecutor;
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
		private FactoryService factorySvc;
		
		public HLSQueryBuilderService(FactoryService factorySvc, QueryBuilderService innerSvc, HLSManager hlsManager) {
			this.factorySvc = factorySvc;
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
		public HLSManagerResult createAllRowsQuery(String typeName, ZDBExecutor zexec) {
			QueryExp queryExp = innerSvc.createAllRowsQuery(typeName);
			QuerySpec querySpec = innerSvc.buildSpec(queryExp, new DoNothingVarEvaluator());
			
			QueryContext qtx = new QueryContext();
			qtx.letSpanEngine = new LetSpanEngine(factorySvc, hlsManager.getRegistry()); 
			HLSManagerResult result = hlsManager.execute(querySpec, qtx, zexec);
			
			return result;
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
		HLSQueryBuilderService svc = new HLSQueryBuilderService(delia.getFactoryService(), impl, hlsManager);
		
		hlsManager.setGenerateSQLforMemFlag(true);
		
		try(ZDBExecutor dbexecutor = delia.getDBInterface().createExecutor()) {
			dbexecutor.init1(session.getExecutionContext().registry);
			HLSManagerResult hlsResult = svc.createAllRowsQuery("Customer", dbexecutor);
			String sql = hlsResult.sql;
			log.log("sql: " + sql);
			assertEquals("SELECT * FROM Customer as a", sql);
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
		
		
		
		
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
