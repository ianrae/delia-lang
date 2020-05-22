package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
import org.delia.db.DBAccessContext;
import org.delia.db.sql.NewLegacyRunner;
import org.delia.type.DValue;
import org.delia.zdb.ZDBExecutor;
import org.junit.Before;
import org.junit.Test;



public class QueryFieldTests extends RunnerTestBase {
	
	@Test
	public void testField() {
		NewLegacyRunner runner = initData();
		
		//now query it
//		LetStatementExp exp2 = chkQueryLet("let a = Actor[44].firstName", "let a = Actor[44].firstName");
		ResultValue res = runner.continueExecution("let a = Actor[44].firstName");
		assertEquals(true, res.ok);
		QueryResponse qresp = helper.chkRawResQuery(res, 1);
		
		DValue dval = qresp.dvalList.get(0);
		assertEquals("bob", dval.asString());
	}
	@Test
	public void testFieldThenFunc() {
		NewLegacyRunner runner = initData();
		
		//now query it
		ResultValue res = runner.continueExecution("let a = Actor[44].id.max()");
		assertEquals(true, res.ok);
		QueryResponse qresp = helper.chkRawResQuery(res, 1);
		
		DValue dval = qresp.dvalList.get(0);
		assertEquals(44, dval.asInt());
	}
	@Test
	public void testFieldThenFuncFail() {
		NewLegacyRunner runner = initData();
		
		//now query it
//		LetStatementExp exp2 = chkQueryLet("let a = Actor[44].id.zzz()", "let a = Actor[44].id.zzz()");
		String src = "let a = Actor[44].id.zzz()";
		ResultValue res = doExecCatchFail(src, false);
		assertEquals(false, res.ok);
	}
	
	// --
	//private Runner runner;
	
	@Before
	public void init() {
		initRunner();
		runner.begin("");
		FetchRunner fetchRunner = createFetchRunner();
//		qffRunner = new QueryFuncOrFieldRunner(factorySvc, runner.getRegistry(), fetchRunner, dbInterface.getCapabilities());
	}
	private FetchRunner createFetchRunner() {
		DBAccessContext dbctx = runner.createDBAccessContext();
		ZDBExecutor dbexecutor = dbInterface.createExecutor();
		Runner run = runner.getDeliaRunner();
		FetchRunner fetchRunner = new FetchRunnerImpl(factorySvc, dbexecutor, runner.getRegistry(), run);
		return fetchRunner;
	}
	
	private NewLegacyRunner initData() {
//		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
//		InsertStatementExp exp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
		
		NewLegacyRunner runner = initRunner();
		runner.begin("type Actor struct {id int unique, firstName string, flag boolean} end");
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema

		String src = "insert Actor {id:44, firstName:'bob', flag:true }";
		ResultValue res = runner.continueExecution(src);
		chkResOK(res);
//		assertEquals(false, runner..exists("a"));
		return runner;
	}
	
}
