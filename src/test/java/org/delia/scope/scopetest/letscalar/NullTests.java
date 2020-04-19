package org.delia.scope.scopetest.letscalar;

import org.delia.scope.scopetest.ScopeTestBase;
import org.junit.Before;
import org.junit.Test;

public class NullTests extends ScopeTestBase {

	//-- int --
	@Test
	public void testNullInt() {
		runItNull(null); //implicit
		
		initRunner();
		runItNull("int");
	}
	//-- string --
	@Test
	public void testNullString() {
		runItNull("string");
	}

	
	//-- boolean --
	@Test
	public void testNullBoolean() {
		runItNull("boolean");
	}
	
	//-- long --
	@Test
	public void testNullLong() {
		runItNull("long");
	}
	
	//-- number --
	@Test
	public void testNumber() {
		runItNull("number");
	}
	
	//-- date --
	@Test
	public void testDate() {
		runItNull("date");
	}
	
	//---
	@Before
	public void init() {
		initRunner();
	}

}
