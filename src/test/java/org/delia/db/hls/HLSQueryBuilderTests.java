package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

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
import org.delia.zdb.ZDBExecutor;
import org.junit.Before;
import org.junit.Test;

/**
 * HLS = High Level SQL
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

		public HLSManagerResult createCountQuery(String typeName, ZDBExecutor zexec) {
			QueryExp queryExp = innerSvc.createCountQuery(typeName);
			return execQuery(queryExp, zexec);
		}

		public HLSManagerResult createAllRowsQuery(String typeName, ZDBExecutor zexec) {
			QueryExp queryExp = innerSvc.createAllRowsQuery(typeName);
			return execQuery(queryExp, zexec);
		}
		private HLSManagerResult execQuery(QueryExp queryExp, ZDBExecutor zexec) {
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
	public void testAllRows() {
		try(ZDBExecutor dbexecutor = createExecutor()) {
			HLSManagerResult hlsResult = svc.createAllRowsQuery("Customer", dbexecutor);
			this.chkSqlGen(hlsResult, "SELECT * FROM Customer as a");
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}
	
	@Test
	public void testCount() {
		try(ZDBExecutor dbexecutor = createExecutor()) {
			HLSManagerResult hlsResult = svc.createCountQuery("Customer", dbexecutor);
			this.chkSqlGen(hlsResult, "SELECT COUNT(*) FROM Customer as a");
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}
	

	//---
	private HLSManager hlsManager;
	private HLSQueryBuilderService svc;
	
	@Before
	public void init() {
		createDao();
		useCustomer1NSrc = true;
		compileQueryToLetStatement("");

		QueryBuilderService impl = new QueryBuilderServiceImpl(delia.getFactoryService());
		hlsManager = new HLSManager(delia, session.getExecutionContext().registry, session, new DoNothingVarEvaluator());
		svc = new HLSQueryBuilderService(delia.getFactoryService(), impl, hlsManager);
		hlsManager.setGenerateSQLforMemFlag(true);
	}
	
	private void chkSqlGen(String sqlExpected) {
		try(ZDBExecutor dbexecutor = delia.getDBInterface().createExecutor()) {
			dbexecutor.init1(session.getExecutionContext().registry);
			HLSManagerResult hlsResult = svc.createAllRowsQuery("Customer", dbexecutor);
			String sql = hlsResult.sql;
			log.log("sql: " + sql);
			assertEquals(sqlExpected, sql);
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}
	private void chkSqlGen(HLSManagerResult hlsResult, String sqlExpected) {
		String sql = hlsResult.sql;
		log.log("sql: " + sql);
		assertEquals(sqlExpected, sql);
	}
	private ZDBExecutor createExecutor() {
		ZDBExecutor dbexecutor = delia.getDBInterface().createExecutor();
		dbexecutor.init1(session.getExecutionContext().registry);
		return dbexecutor;
	}
}
