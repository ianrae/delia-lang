package org.delia.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.delia.base.DBHelper;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UserFunctionDefStatementExp;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class FilterEvaluatorTests extends RunnerTestBase {
	
	@Test
	public void test() {
		createBasicActorType();
		createUserFn1("foo", false);
		chelper = helper.createCompilerHelper();
		
		int id = 120;
		DValue dval = insertAndQueryEx(id);
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		
		QueryResponse qresp = invokeUserFn(null);
		DValue dval2 = qresp.getOne();
		assertSame(dval, dval2);
	}
	
	// --
	@Before
	public void init() {
		initRunner();
	}
	
	private void createUserFn1(String fnName, boolean withArg) {
		String src;
		if (withArg) {
			String arg = withArg ? "id" : "";
			src = String.format("function foo(%s) { let x = Actor[%s] } ",arg, arg);
		} else {
			src = String.format("function foo() { let x = Actor[120] } ");
		}
		
		UserFunctionDefStatementExp exp0 = chelper.chkUserFn(src, fnName);
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
	}
	private DValue insertAndQueryEx(int id) {
		QueryResponse qresp= insertAndQuery(id);
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		return dval;
	}
	private QueryResponse insertAndQuery(int id) {
		String src = String.format("insert Actor {id:%d, firstName:'bob'}", id);
		InsertStatementExp exp = chelper.chkInsert(src, null);
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
	private QueryResponse invokeUserFn(Integer id) {
		String src = String.format("let b = foo(%s)", id == null ? "" : id.toString());
		LetStatementExp exp2 = chelper.chkUserFuncInvoke(src, null);
		ResultValue res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Actor");
		return qresp;
	}




}
