package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaImpl;
import org.delia.base.FakeTypeCreator;
import org.delia.base.UnitTestLog;
import org.delia.builder.DeliaBuilder;
import org.delia.core.FactoryService;
import org.delia.log.Log;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.zdb.DBInterfaceFactory;



public class RunnerHelper {
	private Runner currentRunner;
	
	public LegacyRunner create(FactoryService factorySvc, DBInterfaceFactory dbInterface) {
//		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		DeliaBuilder builder = new DeliaBuilder();
		Delia delia = builder.buildEx(dbInterface, factorySvc);
		
		DeliaImpl impl = (DeliaImpl) delia;
		currentRunner = impl.createRunner(null);
		LegacyRunner runner = new LegacyRunner(currentRunner, factorySvc);
		
//		runner.legacyTypeMode = true; //TODO: remove
//		boolean b = runner.init(null);
//		assertEquals(true, b);
//		dbInterface.init(factorySvc);
		this.addFakeTypes(currentRunner.getRegistry());
//		dbInterface.setRegistry(runner.getRegistry());
//		dbInterface.setVarEvaluator(runner);
		
		return runner;
	}
	
	public void chkResOK(ResultValue res) {
		assertEquals(true, res.ok);
		assertEquals(true, res.errors.isEmpty());
	}
	public void chkResFail(ResultValue res, String errId) {
		assertEquals(false, res.ok);
		boolean b = res.errors.get(0).getId().equals(errId);
		assertEquals(true, b);
	}
	public void chkResFail2(ResultValue res, String errorMsgPart) {
		assertEquals(false, res.ok);
		if (errorMsgPart != null) {
			boolean b = res.errors.get(0).getMsg().indexOf(errorMsgPart) >= 0;
			assertEquals(true, b);
		}
	}
	public void chkResStr(ResultValue res, String expected) {
		assertEquals(true, res.ok);
		assertEquals(Shape.STRING, res.shape);
		DValue dval = (DValue) res.val;
		assertEquals(expected, dval.asString());
	}
	public void chkResBoolean(ResultValue res, boolean expected) {
		assertEquals(true, res.ok);
		assertEquals(Shape.BOOLEAN, res.shape);
		DValue dval = (DValue) res.val;
		Boolean b = (Boolean) dval.asBoolean();
		assertEquals(expected, b.booleanValue());
	}
	public QueryResponse chkResQuery(ResultValue res, String typeName) {
		assertEquals(true, res.ok);
		//TODO: fix assertEquals(typeName, res.shape);
		QueryResponse qresp = (QueryResponse) res.val;
		DValue dval = qresp.getOne();
		assertEquals(typeName, dval.getType().getName());
		return qresp;
	}
	public QueryResponse chkRawResQuery(ResultValue res, int size) {
		return chkRawResQuery(res, size, null);
	}
	public QueryResponse chkRawResQuery(ResultValue res, int size, Shape expectedShape) {
		assertEquals(true, res.ok);
		assertEquals(expectedShape, res.shape);
		QueryResponse qresp = (QueryResponse) res.val;
		assertEquals(size, qresp.dvalList.size());
		return qresp;
	}
	public QueryResponse chkRawResQueryNull(ResultValue res) {
		assertEquals(true, res.ok);
		assertEquals(null, res.shape);
		QueryResponse qresp = (QueryResponse) res.val;
		assertEquals(null, qresp.dvalList);
		return qresp;
	}
	public QueryResponse chkResQueryMany(ResultValue res, int size) {
		assertEquals(true, res.ok);
		assertEquals(null, res.shape);
		QueryResponse qresp = (QueryResponse) res.val;
		assertEquals(size, qresp.dvalList.size());
		return qresp;
	}
	public void chkResQueryFail(ResultValue res) {
		assertEquals(true, res.ok);
		assertEquals(null, res.shape);
		assertEquals(true, res.val != null);
		QueryResponse qresp = (QueryResponse) res.val;
		assertEquals(null, qresp.dvalList);
	}
	public void chkResQueryEmpty(ResultValue res) {
		assertEquals(true, res.ok);
		assertEquals(null, res.shape);
		assertEquals(true, res.val != null);
		QueryResponse qresp = (QueryResponse) res.val;
		assertEquals(true, qresp.dvalList.isEmpty());
	}

	public QueryResponse chkResQuery(QueryResponse qresp, String typeName) {
		DValue dval = qresp.getOne();
		assertEquals(typeName, dval.getType().getName());
		return qresp;
	}
	public void addFakeTypes(DTypeRegistry registry) {
		//TODO: remove this later
		FakeTypeCreator creator = new FakeTypeCreator();
		creator.createFakeTypes(registry);
	}
	public void addDeptAndEmployee(DTypeRegistry registry) {
		//TODO: remove this later
		FakeTypeCreator creator = new FakeTypeCreator();
		creator.createDeptAndEmployee(registry);
	}

	public Runner getCurrentRunner() {
		return currentRunner;
	}

	//---
	public CompilerHelper createCompilerHelper() {
		InternalCompileState execCtx = currentRunner == null ? null: currentRunner.getCompileState();
		Log log = currentRunner == null ? new UnitTestLog() : currentRunner.getLog();
		CompilerHelper chelp = new CompilerHelper(execCtx, log);
		return chelp;
	}
}
