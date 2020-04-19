package org.delia.parser;

import org.delia.runner.DeliaException;
import org.delia.scope.scopetest.relation.DeliaClientTestBase;
import org.junit.Before;
import org.junit.Test;

public class CompilerPass3RuleTests extends DeliaClientTestBase {
	
	
	@Test(expected=DeliaException.class)
	public void testRuleError() {
		String src = "type Point struct {x int, y int} z < 10 end";
		this.execTypeStatement(src);
	}
	
	// --
	
	@Before
	public void init() {
		super.init();
	}
	
}
