package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
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
	
	public static class HLSQueryService { // implements QueryBuilderService {
		private QueryBuilderService innerSvc;
		private HLSManager hlsManager;
		private FactoryService factorySvc;
		
		public HLSQueryService(FactoryService factorySvc, QueryBuilderService innerSvc, HLSManager hlsManager) {
			this.factorySvc = factorySvc;
			this.innerSvc = innerSvc;
			this.hlsManager = hlsManager;
		}

		public HLSManagerResult execEqQuery(String typeName, String fieldName, DValue targetValue, ZDBExecutor zexec) {
			QueryExp queryExp = innerSvc.createEqQuery(typeName, fieldName, targetValue);
			return execQuery(queryExp, zexec);
		}

		public HLSManagerResult execPrimaryKeyQuery(String typeName, DValue keyValue, ZDBExecutor zexec) {
			QueryExp queryExp = innerSvc.createPrimaryKeyQuery(typeName, keyValue);
			return execQuery(queryExp, zexec);
		}

		public HLSManagerResult execInQuery(String typeName, List<DValue> list, DType relType, ZDBExecutor zexec) {
			QueryExp queryExp = innerSvc.createInQuery(typeName, list, relType);
			return execQuery(queryExp, zexec);
		}

		public HLSManagerResult execCountQuery(String typeName, ZDBExecutor zexec) {
			QueryExp queryExp = innerSvc.createCountQuery(typeName);
			return execQuery(queryExp, zexec);
		}

		public HLSManagerResult execAllRowsQuery(String typeName, ZDBExecutor zexec) {
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
	}

	@Test
	public void testAllRows() {
		try(ZDBExecutor dbexecutor = createExecutor()) {
			HLSManagerResult hlsResult = svc.execAllRowsQuery("Customer", dbexecutor);
			this.chkSqlGen(hlsResult, "SELECT * FROM Customer as a");
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}
	
	@Test
	public void testCount() {
		try(ZDBExecutor dbexecutor = createExecutor()) {
			HLSManagerResult hlsResult = svc.execCountQuery("Customer", dbexecutor);
			this.chkSqlGen(hlsResult, "SELECT COUNT(*) FROM Customer as a");
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}
	
	@Test
	public void testIn() throws Exception {
		ScalarValueBuilder builder = new ScalarValueBuilder(delia.getFactoryService(), registry);
		List<DValue> list = new ArrayList<>();
		list.add(builder.buildInt(55));
		list.add(builder.buildInt(57));
		
		DType relType = registry.getType("Address");
		
		try(ZDBExecutor dbexecutor = createExecutor()) {
			HLSManagerResult hlsResult = svc.execInQuery("Customer", list, relType, dbexecutor);
			this.chkSqlGen(hlsResult, "SxxELECT COUNT(*) FROM Customer as a");
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
			throw e;
		}
	}

	@Test
	public void testPrimaryKey() {
		ScalarValueBuilder builder = new ScalarValueBuilder(delia.getFactoryService(), registry);
		DValue dval = builder.buildInt(55);
		
		try(ZDBExecutor dbexecutor = createExecutor()) {
			HLSManagerResult hlsResult = svc.execPrimaryKeyQuery("Customer", dval, dbexecutor);
			this.chkSqlGen(hlsResult, "SELECT * FROM Customer as a WHERE a.cid = ?");
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}
	
	@Test
	public void testEq() {
		ScalarValueBuilder builder = new ScalarValueBuilder(delia.getFactoryService(), registry);
		DValue dval = builder.buildInt(55);
		
		try(ZDBExecutor dbexecutor = createExecutor()) {
			HLSManagerResult hlsResult = svc.execEqQuery("Customer", "x", dval, dbexecutor);
			this.chkSqlGen(hlsResult, "SELECT * FROM Customer as a WHERE a.x = ?");
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}
	
	
	//---
	private HLSManager hlsManager;
	private HLSQueryService svc;
	private DTypeRegistry registry;
	
	@Before
	public void init() {
		createDao();
		useCustomer1NSrc = true;
		compileQueryToLetStatement("");

		registry = session.getExecutionContext().registry;
		QueryBuilderService impl = new QueryBuilderServiceImpl(delia.getFactoryService());
		hlsManager = new HLSManager(delia, registry, session, new DoNothingVarEvaluator());
		svc = new HLSQueryService(delia.getFactoryService(), impl, hlsManager);
		hlsManager.setGenerateSQLforMemFlag(true);
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
