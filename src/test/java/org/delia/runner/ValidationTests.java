package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.error.DeliaError;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.type.DValue;
import org.delia.type.ValidationState;
import org.junit.Before;
import org.junit.Test;

public class ValidationTests extends RunnerTestBase {
	
	public void testValid() {
		createActorType("id < 100");
		int id = 44;
		DValue dval = insertAndQueryEx(id);
		chkValid(dval);
	}
	@Test
	public void testInvalid() {
		createActorType("id < 100");
		int id = 120;
//		DValue dval = insertAndQueryEx(runner, id);
//		chkInvalid(dval);
		insertFail(id, "rule-compare");
	}
	@Test
	public void testMaxLenInvalid() {
		createActorType("firstName.maxlen(2)");
		int id = 44;
//		DValue dval = insertAndQueryEx(runner, id);
//		chkInvalid(dval);
		insertFail(id, "rule-maxlen");
	}
	@Test
	public void testMaxLenValid() {
		createActorType("firstName.maxlen(20)");
		int id = 44;
		DValue dval = insertAndQueryEx(id);
		chkValid(dval);
	}
	@Test  
	public void testLenValid() {
		createActorType("firstName.len() < 10");
		int id = 44;
		DValue dval = insertAndQueryEx(id);
		chkValid(dval);
	}
	@Test  
	public void testLenInvalid() {
		createActorType("firstName.len() < 2");
		int id = 44;
//		DValue dval = insertAndQueryEx(runner, id);
//		chkInvalid(dval);
		insertFail(id, "rule-compare");
	}
	
	@Test  
	public void testContainsInvalid() {
		createActorType("firstName.contains('x')");
		int id = 44;
		insertFail(id, "rule-contains");
	}
	@Test  
	public void testContainsValid() {
		createActorType("firstName.contains('b')");
		int id = 44;
		DValue dval = insertAndQueryEx(id);
		chkValid(dval);
	}
	@Test  
	public void testContainsInValidNot() {
		createActorType("!firstName.contains('b')");
		int id = 44;
		insertFail(id, "rule-contains");
	}
	
	
	private DValue insertAndQueryEx(int id) {
		QueryResponse qresp= insertAndQuery(id);
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		return dval;
	}
	private QueryResponse insertAndQuery(int id) {
		String src = String.format("insert Actor {id:%d, firstName:'bob', flag:true }", id);
		InsertStatementExp exp = chkInsert(src, null);
		ResultValue res = runner.executeOneStatement(exp);
		chkResOK(res);
		
		//now query it
		src = String.format("let a = Actor[%d]", id);
		LetStatementExp exp2 = chkQueryLet(src, null);
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Actor");
		return qresp;
	}
	
	private void insertFail(int id, String errId) {
		String src = String.format("insert Actor {id:%d, firstName:'bob', flag:true }", id);
		ResultValue res = execInsertFail(src, 1, errId);
	}

	@Test
	public void testQueryAll() {
		InsertStatementExp exp = chkInsert("insert Customer {id:44, firstName:'bob' }", "insert Customer {id: 44,firstName: 'bob' }");
		
		Runner runner = initRunner();
		ResultValue res = runner.executeOneStatement(exp);
		chkResOK(res);
		
		//now query it
		LetStatementExp exp2 = chkQueryLet("let a = Customer", "let a = Customer");
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Customer");
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
	}
	
	// --
	
	@Before
	public void init() {
		initRunner();
	}
	
	private void chkValid(DValue dval) {
		assertEquals(ValidationState.VALID, dval.getValidationState());
	}
	private void chkInvalid(DValue dval) {
		assertEquals(ValidationState.INVALID, dval.getValidationState());
	}
	private Runner createActorType(String rule) {
		String src = String.format("type Actor struct {id int unique, firstName string, flag boolean} %s end", rule);
		TypeStatementExp exp0 = chkType(src, null);
		Runner runner = initRunner();
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema
		return runner;
	}

	protected ResultValue execInsertFail(String src, int expectedErrorCount, String errId) {
		InsertStatementExp exp = chkInsert(src, null);
		ResultValue res = runner.executeOneStatement(exp);
		assertEquals(false, res.ok);
		assertEquals(expectedErrorCount, res.errors.size());
		//get last error
		DeliaError err = getLastError(res); 
		assertEquals(errId, err.getId());
		return res;
	}
	protected DeliaError getLastError(ResultValue res) {
		DeliaError err = res.errors.get(res.errors.size() - 1);
		return err;
	}


}
