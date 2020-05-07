package org.delia.scope.scopetest;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.delia.db.InstrumentedDBInterface;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.NewLegacyRunner;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.CompilerHelper;
import org.delia.runner.LegacyRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.runner.RunnerHelper;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.ValidationState;

public class ScopeTestBase {
	protected static final double DELTA = 0.000001;

	// --
	protected InstrumentedDBInterface dbInterface;
	protected RunnerHelper helper = new RunnerHelper();
	protected NewLegacyRunner runner;
	protected Log log = new UnitTestLog();
	protected CompilerHelper chelper = new CompilerHelper(null, log);
	protected ErrorTracker et = new SimpleErrorTracker(log);
	protected FactoryService factorySvc = new FactoryServiceImpl(log, et);

	protected NewLegacyRunner initRunner()  {
		DBInterface mockInterface = new MemDBInterface();
		dbInterface = new InstrumentedDBInterface(mockInterface);
		DBHelper.createTable(dbInterface, "Flight"); //!! fake schema
		
		runner = new NewLegacyRunner(log); 
		runner.forceDBInterface(dbInterface);
		return runner;
	}
	
	protected void chkValid(DValue dval) {
		assertEquals(ValidationState.VALID, dval.getValidationState());
	}
	protected void chkInvalid(DValue dval) {
		assertEquals(ValidationState.INVALID, dval.getValidationState());
	}

	protected DValue runIt(String type, String valStr) {
//		Runner runner = initRunner();
		DValue dval = (DValue) runScalarLet(runner, valStr, type);
		chkValid(dval);
		return dval;
	}
	protected Object runScalarLet(NewLegacyRunner runner, String valStr, String type) {
		//use explicit type since otherwise 55 will be seen as int, not long
		String src = String.format("let a %s = %s", type == null ? "" : type, valStr);
//		LetStatementExp exp2 = chelper.chkScalarLet(src, type);
		ResultValue res = runner.beginOrContinue(src, true);
		assertEquals(true, res.ok);
		Object obj = res.val;
		return obj;
	}

	protected DValue runItNull(String type) {
//		Runner runner = initRunner();
		DValue dval = (DValue) runScalarLet(runner, "null", type);
		assertEquals(null, dval);
		return dval;
	}
	
	protected void chkResOK(ResultValue res) {
		helper.chkResOK(res);
	}
	protected void chkResFail(ResultValue res, String errorMsgPart) {
		helper.chkResFail(res, errorMsgPart);
	}
	
	protected TypeStatementExp chkType(String input, String output) {
		return chelper.chkType(input, output);
	}
	protected Date makeDate(String s) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy") ; 
		Date dt = null;
		try {
			dt = sdf.parse(s);
			log(dt.toString());
		} catch (ParseException e) {
		}
		return dt;
	}
	protected void log(String s) {
		System.out.println(s);
	}
	
	protected ResultValue execTypeStatement(String src) {
//		TypeStatementExp exp0 = chkType(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
		return res;
	}
	protected DeliaError getLastError(ResultValue res) {
		DeliaError err = res.errors.get(res.errors.size() - 1);
		return err;
	}
	protected ResultValue execInsertStatement(String src) {
		return execInsertStatement(src, true);
	}
	protected ResultValue execInsertStatement(String src, boolean shouldPass) {
//		InsertStatementExp exp = chelper.chkInsert(src, null);
		ResultValue res = runner.beginOrContinue(src, shouldPass);
		if (shouldPass) {
			chkResOK(res);
		}
		return res;
	}
	protected ResultValue execInsertFail(String src, int expectedErrorCount, String errId) {
//		InsertStatementExp exp = chelper.chkInsert(src, null);
		ResultValue res = runner.beginOrContinue(src, false);
		assertEquals(false, res.ok);
		assertEquals(expectedErrorCount, res.errors.size());
		//get last error
		DeliaError err = getLastError(res); 
		assertEquals(errId, err.getId());
		return res;
	}
	protected ResultValue execInsertFail2(String src, int expectedErrorCount, String errId, String errId2) {
//		InsertStatementExp exp = chelper.chkInsert(src, null);
		ResultValue res = runner.beginOrContinue(src, false);
		assertEquals(false, res.ok);
		assertEquals(expectedErrorCount, res.errors.size());
		//get first error
		DeliaError err = res.errors.get(0); 
		assertEquals(errId, err.getId());
		//get second error
		err = res.errors.get(1); 
		assertEquals(errId2, err.getId());
		
		return res;
	}
	protected ResultValue execUpdateStatement(String src) {
//		UpdateStatementExp exp = chelper.chkUpdate(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
		return res;
	}
	protected ResultValue execUpdateFail(String src, int expectedErrorCount, String errId) {
//		UpdateStatementExp exp = chelper.chkUpdate(src, null);
		ResultValue res = runner.beginOrContinue(src, false);
		assertEquals(false, res.ok);
		assertEquals(expectedErrorCount, res.errors.size());
		//get last error
		DeliaError err = getLastError(res); 
		assertEquals(errId, err.getId());
		return res;
	}
	protected ResultValue execDeleteStatement(String src) {
//		DeleteStatementExp exp = chelper.chkDelete(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
		return res;
	}
	protected ResultValue execDeleteFail(String src, int expectedErrorCount, String errId) {
//		DeleteStatementExp exp = chelper.chkDelete(src, null);
		ResultValue res = runner.beginOrContinue(src, false);
		assertEquals(false, res.ok);
		assertEquals(expectedErrorCount, res.errors.size());
		//get last error
		DeliaError err = getLastError(res); 
		assertEquals(errId, err.getId());
		return res;
	}

	protected DValue getOne(String fieldName, QueryResponse qresp, boolean expectNull) {
		DValue dval = qresp.getOne();
		chkValid(dval);
		DValue inner = dval.asStruct().getField(fieldName);
		if (expectNull) {
			assertEquals(null, inner);
		} else {
			chkValid(inner);
		}
		return inner;
	}
	protected DValue getLastOne(String fieldName, QueryResponse qresp, boolean expectNull, int expectedSize) {
		//get last one
		int n = qresp.dvalList.size();
		DValue dval = qresp.dvalList.get(n - 1);
		chkValid(dval);
		DValue inner = dval.asStruct().getField(fieldName);
		if (expectNull) {
			assertEquals(null, inner);
		} else {
			chkValid(inner);
		}
		return inner;
	}
	
	protected QueryResponse execLetStatementOne(String src, String typeName) {
//		LetStatementExp exp2 = chelper.chkQueryLet(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		assertEquals(true, res.ok);
		QueryResponse qresp = helper.chkResQuery(res, typeName);
		return qresp;
	}
	protected QueryResponse execLetStatementMulti(String src, int expectedSize) {
		return execLetStatementMulti(src, expectedSize, null);
	}
	protected QueryResponse execLetStatementMulti(String src, int expectedSize, Shape expectedShape) {
//		LetStatementExp exp2 = chelper.chkQueryLet(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		assertEquals(true, res.ok);
		QueryResponse qresp = helper.chkRawResQuery(res, expectedSize, expectedShape);
		return qresp;
	}
	protected QueryResponse execLetStatementNull(String src) {
//		LetStatementExp exp2 = chelper.chkQueryLet(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		assertEquals(true, res.ok);
		QueryResponse qresp = helper.chkRawResQueryNull(res);
		return qresp;
	}
	protected DValue execLetStatementScalar(String src, String type) {
//		LetStatementExp exp2 = chelper.chkScalarLet(src, type);
		ResultValue res = runner.beginOrContinue(src, true);
		assertEquals(true, res.ok);
		return res.getAsDValue();
	}
	protected ResultValue execLetStatementScalarFail(String src, String type) {
//		LetStatementExp exp2 = chelper.chkScalarLet(src, type);
		ResultValue res = runner.beginOrContinue(src, false);
		assertEquals(false, res.ok);
		return res;
	}
	protected void chkDBCounts(int n1, int n2, int n3, int n4) {
		assertEquals(n1, dbInterface.insertCount);
		assertEquals(n2, dbInterface.updateCount);
		assertEquals(n3, dbInterface.deleteCount);
		assertEquals(n4, dbInterface.queryCount);
		
	}

	protected NewLegacyRunner createActorType(String rule) {
		String src = String.format("type Actor struct {id int unique, firstName string, dt date optional} %s end", rule);
//		TypeStatementExp exp0 = chkType(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema
		return runner;
	}
	protected NewLegacyRunner createActorTypeDate(String rule) {
		String src = String.format("type Actor struct {id int unique, firstName string, dt date} %s end", rule);
//		TypeStatementExp exp0 = chkType(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema
		return runner;
	}
	protected NewLegacyRunner xcreateActorType(String rule) {
		String src = String.format("type Actor struct {id int unique, firstName string, flag boolean} firstName.%s end", rule);
//		TypeStatementExp exp0 = chkType(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema
		return runner;
	}
	
	protected void createActorType() {
		String src = String.format("type Actor struct {id int unique, dt X, flag boolean} end");
//		TypeStatementExp exp0 = chkType(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema
	}
	
	

}
