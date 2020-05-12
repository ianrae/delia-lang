package org.delia.scope.scopetest.typerules;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class MaxLenScalarTests extends ScopeTestBase {

	// -- let --
//	@Test
//	public void testLetInvalid() {
//		createScalarType("maxlen(2)");
//		DValue dval = execLetScalarFail("string longer than");
//		chkInvalid(dval);
//	}
	@Test
	public void testLetValid() {
		createScalarType("maxlen(4)");
		DValue dval = execLetScalar("'bob'");
		chkValid(dval);
	}
	@Test
	public void testLetValidNull() {
		createScalarType("maxlen(4)");
		DValue dval = execLetScalar("null");
		assertEquals(null, dval);
	}
	
//	//--insert--
//	@Test
//	public void testInsertInvalid() {
//		createScalarType("maxlen(2)");
//		assertEquals(true, runner.getRegistry().existsType("Name"));
//		chelper = helper.createCompilerHelper();
//		
//		ycreateActorType();
//		int id = 44;
////		DValue dval = insertAndQueryEx(id);
////		chkInvalid(dval);
////		chkDBCounts(1, 0, 0, 1);
//		insertFail(id, "rule-maxlen");
//	}
//	@Test
//	public void testInsertValid() {
//		Runner runner = createActorType("maxlen(20)");
//		int id = 44;
//		DValue dval = insertAndQueryEx(runner, id);
//		chkValid(dval);
//		chkDBCounts(1, 0, 0, 1);
//	}
	
	
//	
//	//--update--
//	@Test
//	public void testUpdateInvalid() {
//		Runner runner = createScalarType("maxlen(4)");
//		DValue dval = insertAndQueryEx(runner);
//		chkValid(dval);
//		
//		updateAndQueryFail("'bobby'", 1, "maxlen");
//		chkDBCounts(1, 0, 0, 1); //update didn't happen
//	}

	// --
	private int nextVarNum = 1;

	@Before
	public void init() {
		runner = initRunner();
	}

	private void createScalarType(String rule) {
		String src = String.format("type Name string %s end", rule);
//		TypeStatementExp exp0 = chkType(src, null);
		runner.begin(src);
	}
	
	private DValue execLetScalar(String val) {
		String src = String.format("let x Name = %s", val);
//		LetStatementExp exp = chelper.chkScalarLet(src, "Name");
		ResultValue res = runner.continueExecution(src);
		chkResOK(res);
		return res.getAsDValue();
	}
	private DValue execLetScalarFail(String errMsgPart) {
		String src = String.format("let x Name = 'bob'");
//		LetStatementExp exp = chelper.chkScalarLet(src, "Name");
		ResultValue res = this.doExecCatchFail(src, false);
		helper.chkResFail2(res, errMsgPart);
		return res.getAsDValue();
	}
	
	private DValue updateAndQuery(String valStr, int expectedSize) {
		String src = String.format("update Actor {firstName:%s}", valStr);
		execUpdateStatement(src);

		//now query it
		String varName = String.format("a%d", nextVarNum++);
		src = String.format("let %s = Actor", varName);
		QueryResponse qresp = execLetStatementMulti(src, expectedSize);
		return qresp.getOne();
	}
	private void updateAndQueryFail(String valStr, int expectedErrorCount, String errId) {
		String src = String.format("update Actor {firstName:%s}", valStr);
		execUpdateFail(src, expectedErrorCount, errId);
	}
	
	private void ycreateActorType() {
		String src = String.format("type Actor struct {id int unique, firstName Name, flag boolean} end");
//		TypeStatementExp exp0 = chkType(src, null);
		runner.begin(src);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema
	}
	
	private DValue insertAndQueryEx(int id) {
		QueryResponse qresp= insertAndQuery(id);
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		return dval;
	}
	private QueryResponse insertAndQuery(int id) {
		String src = String.format("insert Actor {id:%d, firstName:'bob', flag:true }", id);
//		InsertStatementExp exp = chelper.chkInsert(src, null);
		ResultValue res = runner.continueExecution(src);
		chkResOK(res);
		
		//now query it
		src = String.format("let a = Actor[%d]", id);
//		LetStatementExp exp2 = chelper.chkQueryLet(src, null);
		res = runner.continueExecution(src);
		assertEquals(true, res.ok);
		QueryResponse qresp = helper.chkResQuery(res, "Actor");
		return qresp;
	}
	private void insertFail(int id, String errId) {
		String src = String.format("insert Actor {id:%d, firstName:'bob', flag:true }", id);
//		InsertStatementExp exp = chelper.chkInsert(src, null);
		ResultValue res = runner.continueExecution(src);
		chkResFail(res, errId);
	}
	
}
