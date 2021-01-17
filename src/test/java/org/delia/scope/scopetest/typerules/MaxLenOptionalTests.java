package org.delia.scope.scopetest.typerules;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
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
public class MaxLenOptionalTests extends ScopeTestBase {

	//--insert--
	@Test
	public void testInsertInvalid() {
		zcreateActorType("maxlen(2)");
		int id = 44;
		insertFail(id, false, "rule-maxlen");
		chkDBCounts(0, 0, 0, 0);
	}
	@Test
	public void testInsertNullInvalidNotRun() {
		zcreateActorType("maxlen(2)");
		int id = 44;
		DValue dval = insertAndQueryEx(id, true);
		chkValid(dval);
		chkDBCounts(1, 0, 0, 1);
	}
	@Test
	public void testInsertValid() {
		zcreateActorType("maxlen(20)");
		int id = 44;
		DValue dval = insertAndQueryEx(id, false);
		chkValid(dval);
		chkDBCounts(1, 0, 0, 1);
	}
	
	@Test
	public void testInsertValidMulti() {
		zcreateActorType("maxlen(20), firstName.contains('b')");
		int id = 44;
		DValue dval = insertAndQueryEx(id, false);
		chkValid(dval);
		chkDBCounts(1, 0, 0, 1);
	}
	@Test
	public void testInsertInvalidMultiNotRun() {
		zcreateActorType("maxlen(2), firstName.contains('x')");
		int id = 44;
		DValue dval = insertAndQueryEx(id, true);
		chkValid(dval);
		chkDBCounts(1, 0, 0, 1);
	}
	
	//--update--
	@Test
	public void testUpdateInvalid() {
		zcreateActorType("maxlen(4)");
		int id = 44;
		DValue dval = insertAndQueryEx(id, false);
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

	
	private DValue insertAndQueryEx(int id, boolean isNull) {
		QueryResponse qresp= insertAndQuery(id, isNull);
		DValue dval = qresp.getOne();
		if (isNull) {
			assertEquals(null, dval.asStruct().getField("firstName"));
		} else {
			assertEquals("bob", dval.asStruct().getField("firstName").asString());
		}
		return dval;
	}
	private void insertFail(int id, boolean isNull, String errId) {
		insertFail(id, isNull, errId, null);
	}
	private void insertFail(int id, boolean isNull, String errId, String errId2) {
		String val = isNull ? "null" : "'bob'";
		String src = String.format("insert Actor {id:%d, firstName:%s, flag:true }", id, val);
		if (errId2 != null) {
			execInsertFail2(src, 2, errId, errId2);
		} else {
			execInsertFail(src, 1, errId);
		}
	}
	
	private QueryResponse insertAndQuery(int id, boolean isNull) {
		String val = isNull ? "null" : "'bob'";
		String src = String.format("insert Actor {id:%d, firstName:%s, flag:true }", id, val);
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
	
	private NewLegacyRunner zcreateActorType(String rule) {
		String src = String.format("type Actor struct {id int unique, firstName string optional, flag boolean} firstName.%s end", rule);
//		TypeStatementExp exp0 = chkType(src, null);
		runner.begin(src);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema
		return runner;
	}	
}
