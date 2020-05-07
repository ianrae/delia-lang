package org.delia.scope.scopetest.letscalar;

import static org.junit.Assert.assertEquals;

import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class LetParserTests extends ScopeTestBase {
	
	//TODO: fix this. should be a way to force a field named 'unique', for example
	//the solution may be a mapper in dbinterface
	//so delia still enforces reserved words

	//-- can reserved words be used as varnames? No.
	//let unique int = 55
	@Test
	public void testInt() {
		try {
			DValue dval = (DValue) runLetStatement("unique", "int", "55");
		} catch (DeliaException e) {
			log(e.getMessage());
		}
	}

	//-- can reserved words be used as varnames? No.
	//let unique int = 55
	@Test
	public void testField() {
		try {
			createFlightTypeEx("unique", "int", "optional");
		} catch (DeliaException e) {
			log(e.getMessage());
		}
	}
	
//	@Test
//	public void test3() {
//		try {
//			DValue dval = (DValue) runLetStatement("set", "int", "55");
//		} catch (DangException e) {
//			log(e.getMessage());
//		}
//	}
	

	//---
	@Before
	public void init() {
		initRunner();
	}

	private Object runLetStatement(String varName, String type, String valStr) {
		//use explicit type since otherwise 55 will be seen as int, not long
		String src = String.format("let %s %s = %s", varName, type == null ? "" : type, valStr);
//		LetStatementExp exp2 = chelper.chkScalarLet(src, type);
		ResultValue res = runner.continueExecution(src);
		assertEquals(true, res.ok);
		Object obj = res.val;
		return obj;
	}

	private void createFlightTypeEx(String fieldName, String type, String modifier) {
		String src = String.format("type Flight struct {%s %s %s} end", fieldName, type, modifier);
		this.execTypeStatement(src);
	}
}
