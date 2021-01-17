package org.delia.scope.scopetest.typerules;

import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.db.sql.NewLegacyRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * todo
 * -test that if we hack db storage and change a value,
 *  that we validate on query and detect it!
 *  
 * @author Ian Rae
 *
 */
public class MaxLenTests extends ScopeTestBase {

	//--insert--
	@Test
	public void testInsertInvalid() {
		NewLegacyRunner runner = xcreateActorType("maxlen(2)");
		int id = 44;
//		DValue dval = insertAndQueryEx(runner, id);
//		chkInvalid(dval);
		insertFail(runner, id, "rule-maxlen");
		chkDBCounts(0, 0, 0, 0);
	}
	@Test
	public void testInsertValid() {
		NewLegacyRunner runner = xcreateActorType("maxlen(20)");
		int id = 44;
		DValue dval = insertAndQueryEx(runner, id);
		chkValid(dval);
		chkDBCounts(1, 0, 0, 1);
	}
	
	@Test
	public void testInsertValidMulti() {
		NewLegacyRunner runner = xcreateActorType("maxlen(20), firstName.contains('b')");
		int id = 44;
		DValue dval = insertAndQueryEx(runner, id);
		chkValid(dval);
		chkDBCounts(1, 0, 0, 1);
	}
//	@Test
//	public void testInsertInvalidMulti() {
//		NewLegacyRunner runner = xcreateActorType("maxlen(2), firstName.contains('x')");
//		int id = 44;
////		DValue dval = insertAndQueryEx(runner, id);
////		chkInvalid(dval);
//		insertFail(runner, id, "rule-maxlen", "rule-contains");
//		chkDBCounts(0, 0, 0, 0);
//	}
	
	//--update--
	@Test
	public void testUpdateInvalid() {
		NewLegacyRunner runner = xcreateActorType("maxlen(4)");
		int id = 44;
		DValue dval = insertAndQueryEx(runner, id);
		chkValid(dval);
		
		updateAndQueryFail("'bobby'", 1, "rule-maxlen");
		chkDBCounts(1, 0, 0, 1); //update didn't happen
	}

	// --
	private int nextVarNum = 1;

	@Before
	public void init() {
		runner = initRunner();
	}

	
	private DValue insertAndQueryEx(NewLegacyRunner runner, int id) {
		QueryResponse qresp= insertAndQuery(runner, id);
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		return dval;
	}
	private void insertFail(NewLegacyRunner runner, int id, String errId) {
		insertFail(runner, id, errId, null);
	}
	private void insertFail(NewLegacyRunner runner, int id, String errId, String errId2) {
		String src = String.format("insert Actor {id:%d, firstName:'bob', flag:true }", id);
		if (errId2 != null) {
			execInsertFail2(src, 2, errId, errId2);
		} else {
			execInsertFail(src, 1, errId);
		}
	}
	
	private QueryResponse insertAndQuery(NewLegacyRunner runner, int id) {
		String src = String.format("insert Actor {id:%d, firstName:'bob', flag:true }", id);
		InsertStatementExp exp = chelper.chkInsert(src, null);
		ResultValue res = runner.continueExecution(src);
		chkResOK(res);
		
		//now query it
		src = String.format("let a = Actor[%d]", id);
		LetStatementExp exp2 = chelper.chkQueryLet(src, null);
		res = runner.continueExecution(src);
		assertEquals(true, res.ok);
		QueryResponse qresp = helper.chkResQuery(res, "Actor");
		return qresp;
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
		String src = String.format("update Actor[true] {firstName:%s}", valStr);
		execUpdateFail(src, expectedErrorCount, errId);
	}
	
}
