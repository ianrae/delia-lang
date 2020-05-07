package org.delia.scope.scopetest.typerules;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.RunnerImpl;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * todo
 *  
 * @author Ian Rae
 *
 */
public class DateYearTests extends ScopeTestBase {

	//--insert--
	@Test
	public void testInsertValid() {
		createActorType("dt.year() == 2011");
		int id = 44;
		DValue dval = insertAndQueryEx(id);
		chkValid(dval);
		chkDBCounts(1, 0, 0, 1);
	}
	@Test
	public void testInsertInvalid() {
		createActorType("dt.year() == 2019");
		int id = 44;
		insertFail(id, "rule-compare");
		chkDBCounts(0, 0, 0, 0);
	}
	@Test
	public void testNotAllowed() {
		createActorType("firstName.year() == 2019");
		int id = 44;
		insertFail(id, "rule-year");
		chkDBCounts(0, 0, 0, 0);
	}

	// --

	@Before
	public void init() {
		runner = initRunner();
	}

	private DValue insertAndQueryEx(int id) {
		QueryResponse qresp= insertAndQuery(id);
		DValue dval = qresp.getOne();
		return dval;
	}
	private void insertFail(int id, String errId) {
		insertFail(id, errId, null);
	}
	private void insertFail(int id, String errId, String errId2) {
		String src = String.format("insert Actor {id:%d, firstName:'bob', dt:'2011-01-30' }", id);
		if (errId2 != null) {
			execInsertFail2(src, 2, errId, errId2);
		} else {
			execInsertFail(src, 1, errId);
		}
	}
	
	private QueryResponse insertAndQuery(int id) {
		String src = String.format("insert Actor {id:%d, firstName:'bob', dt:'2011-01-30' }", id);
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
	
}
