package org.delia.scope.scopetest.typerules;

import static org.junit.Assert.assertEquals;

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
public class DateYearScalarTests extends ScopeTestBase {

	// -- let --
	@Test
	public void testLetNotAllowed() {
		createIntScalarType("year() == 2019");
		DValue dval = execLetScalarFail("X value is not an int");
		assertEquals(null, dval);
	}
//	@Test
//	public void testLetInvalid() {
//		createScalarType("year() == 2019");
//		DValue dval = execLetScalarFail("'2011' == '2019'");
//		chkInvalid(dval);
//	}
	@Test
	public void testLetValid() {
		createScalarType("year() == 2011");
		DValue dval = execLetScalar("'2011-01-30'");
		chkValid(dval);
	}
	@Test
	public void testLetValidNull() {
		createScalarType("year() == 2011");
		DValue dval = execLetScalar("null");
		assertEquals(null, dval);
	}
	
	//--insert--
//	@Test
//	public void testInsertInvalid() {
//		createScalarType("year() == 2019");
//		assertEquals(true, runner.getRegistry().existsType("X"));
//		chelper = helper.createCompilerHelper();
//		
//		createActorType();
//		int id = 44;
//		insertFail(id, "rule-compare");
//	}

	// --

	@Before
	public void init() {
		runner = initRunner();
	}

	private void createScalarType(String rule) {
		String src = String.format("type X date %s end", rule);
//		TypeStatementExp exp0 = chkType(src, null);
		runner.begin(src);
	}
	private void createIntScalarType(String rule) {
		String src = String.format("type X int %s end", rule);
//		TypeStatementExp exp0 = chkType(src, null);
		runner.begin(src);
	}
	
	private DValue execLetScalar(String val) {
		String src = String.format("let x X = %s", val);
//		LetStatementExp exp = chelper.chkScalarLet(src, "X");
		ResultValue res = runner.continueExecution(src);
		chkResOK(res);
		return res.getAsDValue();
	}
	private DValue execLetScalarFail(String errMsgPart) {
		String src = String.format("let x X = '2011-03-16'");
//		LetStatementExp exp = chelper.chkScalarLet(src, "X");
		ResultValue res = doExecCatchFail(src, false);
		helper.chkResFail2(res, errMsgPart);
		return res.getAsDValue();
	}
	
	private QueryResponse insertAndQuery(int id) {
		String src = String.format("insert Actor {id:%d, dt:'2011-03-16', flag:true }", id);
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
		String src = String.format("insert Actor {id:%d, dt:'2011-03-16', flag:true }", id);
//		InsertStatementExp exp = chelper.chkInsert(src, null);
		ResultValue res = runner.continueExecution(src);
		chkResFail(res, errId);
	}
	
}
