package org.delia.scope.scopetest.configure;

import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.ConfigureStatementExp;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.ScopeTestBase;
import org.junit.Before;
import org.junit.Test;

public class ConfigureTests extends ScopeTestBase {

	@Test(expected=DeliaException.class)
	public void testBadVar() {
		runConfigureStatement("sss", "555");
	}
	
	@Test
	public void testTimezone() {
		runConfigureStatement("timezone", "'US/Pacific'");
		//TODO: also test -0500 format too
	}
	
	//TODO should throw.
	@Test //(expected=DangException.class)
	public void testTimezoneBad() {
		runConfigureStatement("timezone", "zzzzz");
	}
	
	@Test 
	public void testFKs() {
		runConfigureStatement("loadFKs", "true");
	}
	
	//---
	@Before
	public void init() {
		initRunner();
	}
	
	private Object runConfigureStatement(String varName, String valStr) {
		String src = String.format("configure %s = %s", varName, valStr);
//		ConfigureStatementExp exp2 = chelper.chkConfingure(src);
		ResultValue res = runner.continueExecution(src);
		assertEquals(true, res.ok);
		Object obj = res.val;
		return obj;
	}
}
