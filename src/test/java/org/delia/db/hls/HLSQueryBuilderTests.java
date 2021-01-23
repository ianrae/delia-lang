//package org.delia.db.hls;
//
//
//import static org.junit.Assert.assertEquals;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.delia.compiler.ast.QueryExp;
//import org.delia.db.DBHelper;
//import org.delia.db.hls.manager.HLSManagerResult;
//import org.delia.runner.DoNothingVarEvaluator;
//import org.delia.type.DType;
//import org.delia.type.DTypeRegistry;
//import org.delia.type.DValue;
//import org.delia.valuebuilder.ScalarValueBuilder;
//import org.delia.zdb.ZDBExecutor;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * HLS = High Level SQL
// * 
// * @author Ian Rae
// *
// */
//public class HLSQueryBuilderTests extends HLSTestBase {
//	
//	@Test
//	public void testAllRows() {
//		try(ZDBExecutor dbexecutor = createExecutor()) {
//			QueryExp queryExp = svc.getQueryBuilderSvc().createAllRowsQuery("Customer");
//			HLSManagerResult hlsResult = svc.execQueryEx(queryExp, dbexecutor,  new DoNothingVarEvaluator());
//			this.chkSqlGen(hlsResult, "SELECT * FROM Customer as a");
//		} catch (Exception e) {
//			DBHelper.handleCloseFailure(e);
//		}
//	}
//	
//	@Test
//	public void testCount() {
//		try(ZDBExecutor dbexecutor = createExecutor()) {
//			QueryExp queryExp = svc.getQueryBuilderSvc().createCountQuery("Customer");
//			HLSManagerResult hlsResult = svc.execQueryEx(queryExp, dbexecutor,  new DoNothingVarEvaluator());
//			this.chkSqlGen(hlsResult, "SELECT COUNT(*) FROM Customer as a");
//		} catch (Exception e) {
//			DBHelper.handleCloseFailure(e);
//		}
//	}
//	
//	@Test
//	public void testIn() throws Exception {
//		ScalarValueBuilder builder = new ScalarValueBuilder(delia.getFactoryService(), registry);
//		List<DValue> list = new ArrayList<>();
//		list.add(builder.buildInt(55));
//		list.add(builder.buildInt(57));
//		
//		DType relType = registry.getType("Customer");
//		
//		try(ZDBExecutor dbexecutor = createExecutor()) {
//			QueryExp queryExp = svc.getQueryBuilderSvc().createInQuery("Customer", list, relType);
//			HLSManagerResult hlsResult = svc.execQueryEx(queryExp, dbexecutor,  new DoNothingVarEvaluator());
//			this.chkSqlGen(hlsResult, "SELECT * FROM Customer as a WHERE a.cid IN (?,?)");
//		} catch (Exception e) {
//			DBHelper.handleCloseFailure(e);
//			throw e;
//		}
//	}
//
//	@Test
//	public void testPrimaryKey() {
//		ScalarValueBuilder builder = new ScalarValueBuilder(delia.getFactoryService(), registry);
//		DValue dval = builder.buildInt(55);
//		
//		try(ZDBExecutor dbexecutor = createExecutor()) {
//			QueryExp queryExp = svc.getQueryBuilderSvc().createPrimaryKeyQuery("Customer", dval);
//			HLSManagerResult hlsResult = svc.execQueryEx(queryExp, dbexecutor, new DoNothingVarEvaluator());
//			this.chkSqlGen(hlsResult, "SELECT * FROM Customer as a WHERE a.cid = ?");
//		} catch (Exception e) {
//			DBHelper.handleCloseFailure(e);
//		}
//	}
//	
//	@Test
//	public void testEq() {
//		ScalarValueBuilder builder = new ScalarValueBuilder(delia.getFactoryService(), registry);
//		DValue dval = builder.buildInt(55);
//		
//		try(ZDBExecutor dbexecutor = createExecutor()) {
//			QueryExp queryExp = svc.getQueryBuilderSvc().createEqQuery("Customer", "x", dval);
//			HLSManagerResult hlsResult = svc.execQueryEx(queryExp, dbexecutor, new DoNothingVarEvaluator());
//			this.chkSqlGen(hlsResult, "SELECT * FROM Customer as a WHERE a.x = ?");
//		} catch (Exception e) {
//			DBHelper.handleCloseFailure(e);
//		}
//	}
//	
//	
//	//---
//	//private HLSManagerBase hlsManager;
//	private HLSSimpleQueryService svc;
//	private DTypeRegistry registry;
//	
//	@Before
//	public void init() {
//		createDao();
//		useCustomer1NSrc = true;
//		compileQueryToLetStatement("");
//
//		registry = session.getExecutionContext().registry;
////		svc = new HLSSimpleQueryService(delia.getFactoryService(), delia.getDBInterface(), registry);
//		svc = delia.getFactoryService().createSimpleQueryService(delia.getDBInterface(), registry);
//	}
//	
//	private void chkSqlGen(HLSManagerResult hlsResult, String sqlExpected) {
//		String sql = hlsResult.sql;
//		log.log("sql: " + sql);
//		assertEquals(sqlExpected, sql);
//	}
//	private ZDBExecutor createExecutor() {
//		ZDBExecutor dbexecutor = delia.getDBInterface().createExecutor();
//		dbexecutor.init1(session.getExecutionContext().registry);
//		return dbexecutor;
//	}
//}
