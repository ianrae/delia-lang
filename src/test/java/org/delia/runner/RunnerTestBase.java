package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
import org.delia.base.UnitTestLog;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBInterface;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.NewLegacyRunner;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.zdb.ZDBInterfaceFactory;


public class RunnerTestBase {


	// --
	//private Runner runner;
	protected ZDBInterfaceFactory dbInterface;
	protected RunnerHelper helper = new RunnerHelper();
	protected CompilerHelper chelper;
	protected NewLegacyRunner runner;
	protected Log log;
	protected ErrorTracker et;
	protected FactoryService factorySvc = new FactoryServiceImpl(log, et);

	protected NewLegacyRunner initRunner()  {
		log = new UnitTestLog();
		et = new SimpleErrorTracker(log);

		runner = new NewLegacyRunner(log);
		dbInterface = runner.getDelia().getDBInterface();
		return runner;
	}

	protected LetStatementExp chkString(String input, String output) {
		return chelper.chkString(input, output);
	}
	protected void chkResOK(ResultValue res) {
		helper.chkResOK(res);
	}
	protected void chkResFail2(ResultValue res, String errorMsgPart) {
		helper.chkResFail2(res, errorMsgPart);
	}
	protected void chkResStr(ResultValue res, String expected) {
		helper.chkResStr(res, expected);
	}
	protected QueryResponse chkResQuery(ResultValue res, String typeName) {
		return helper.chkResQuery(res, typeName);
	}
	protected TypeStatementExp chkType(String input, String output) {
		return chelper.chkType(input, output);
	}
	protected InsertStatementExp chkInsert(String input, String output) {
		return chelper.chkInsert(input, output);
	}
	protected LetStatementExp chkQueryLet(String input, String output) {
		return chelper.chkQueryLet(input, output);
	}
	protected DeleteStatementExp chkDelete(String input, String output) {
		return chelper.chkDelete(input, output);
	}
	protected UpdateStatementExp chkUpdate(String input, String output) {
		return chelper.chkUpdate(input, output);
	}

	protected NewLegacyRunner createBasicActorType() {
		String src = String.format("type Actor struct {id int unique, firstName string} end");
		//		TypeStatementExp exp0 = chelper.chkType(src, null);
		runner.begin(src);

		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema
		return runner;
	}
	protected ResultValue doExecCatchFail(String src, boolean shouldPass) {
		ResultValue res;
		try {
			res = runner.beginOrContinue(src, false);
		} catch (DeliaException e) {
			res = new ResultValue();
			res.ok = false;
			res.errors.add(e.getLastError());
		}
		assertEquals(shouldPass, res.ok);
		return res;
	}

}
