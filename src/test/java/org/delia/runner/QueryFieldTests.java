package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.queryresponse.function.QueryFuncOrFieldRunner;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;



public class QueryFieldTests extends RunnerTestBase {
	
	@Test
	public void testField() {
		Runner runner = initData();
		
		//now query it
		LetStatementExp exp2 = chkQueryLet("let a = Actor[44].firstName", "let a = Actor[44].firstName");
		ResultValue res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		QueryResponse qresp = helper.chkRawResQuery(res, 1);
		
		DValue dval = qresp.dvalList.get(0);
		assertEquals("bob", dval.asString());
	}
	@Test
	public void testFieldThenFunc() {
		Runner runner = initData();
		
		//now query it
		LetStatementExp exp2 = chkQueryLet("let a = Actor[44].id.max()", "let a = Actor[44].id.max()");
		ResultValue res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		QueryResponse qresp = helper.chkRawResQuery(res, 1);
		
		DValue dval = qresp.dvalList.get(0);
		assertEquals(44, dval.asInt());
	}
	@Test
	public void testFieldThenFuncFail() {
		Runner runner = initData();
		
		//now query it
		LetStatementExp exp2 = chkQueryLet("let a = Actor[44].id.zzz()", "let a = Actor[44].id.zzz()");
		ResultValue res = runner.executeOneStatement(exp2);
		assertEquals(false, res.ok);
	}
	
	// --
	//private Runner runner;
	private QueryFuncOrFieldRunner qffRunner;
	
	@Before
	public void init() {
		initRunner();
		FactoryService factorySvc = new FactoryServiceImpl(log, et);
		FetchRunner fetchRunner = createFetchRunner();
		qffRunner = new QueryFuncOrFieldRunner(factorySvc, runner.getRegistry(), fetchRunner, dbInterface.getCapabilities());
	}
	private FetchRunner createFetchRunner() {
		DBAccessContext dbctx = new DBAccessContext(runner);
		DBExecutor dbexecutor = dbInterface.createExector(dbctx);
		FetchRunner fetchRunner = new FetchRunnerImpl(factorySvc, dbexecutor, runner.getRegistry(), runner);
		return fetchRunner;
	}
	
	private Runner initData() {
		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		InsertStatementExp exp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
		
		Runner runner = initRunner();
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema

		res = runner.executeOneStatement(exp);
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
		return runner;
	}
	
}
