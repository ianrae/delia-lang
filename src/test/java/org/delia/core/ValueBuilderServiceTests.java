package org.delia.core;

import static org.junit.Assert.assertEquals;

import org.delia.runner.DeliaException;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

public class ValueBuilderServiceTests extends ScopeTestBase {
	
	@Test
	public void testInt() {
		DValue dval = builder.buildInt("35");
		assertEquals(35, dval.asInt());
		dval = builder.buildInt(35);
		assertEquals(35, dval.asInt());
	}
	@Test(expected=DeliaException.class)
	public void testIntFail() {
		ScalarValueBuilder builder = createSvc();
		builder.buildInt("garbage");
	}

	@Test
	public void testLong() {
		DValue dval = builder.buildLong("35");
		assertEquals(35, dval.asLong());
		dval = builder.buildLong(35L);
		assertEquals(35, dval.asLong());
	}
	@Test(expected=DeliaException.class)
	public void testIntLong() {
		builder.buildLong("garbage");
	}

	@Test
	public void testNumber() {
		DValue dval = builder.buildNumber("35");
		assertEquals(35, dval.asNumber(), DELTA);
		dval = builder.buildNumber(35.26);
		assertEquals(35.26, dval.asNumber(), DELTA);
	}
	@Test(expected=DeliaException.class)
	public void testIntNumber() {
		builder.buildNumber("garbage");
	}
	
	@Test
	public void testBoolean() {
		DValue dval = builder.buildBoolean("true");
		assertEquals(true, dval.asBoolean());
		dval = builder.buildBoolean(true);
		assertEquals(true, dval.asBoolean());
	}
	@Test(expected=DeliaException.class)
	public void testIntBoolean() {
		builder.buildBoolean("garbage");
	}
	
	@Test
	public void testString() {
		DValue dval = builder.buildString("abc");
		assertEquals("abc", dval.asString());
	}
	@Test(expected=DeliaException.class)
	public void testIntString() {
		builder.buildString(null);
	}
	
	@Test
	public void testDate() {
		DValue dval = builder.buildDate("2020");
		assertEquals("2020-01-01T00:00:00.000+0000", dval.asString());
	}
	@Test(expected=DeliaException.class)
	public void testDateString() {
		builder.buildDate("garbage");
	}
	
	// --
//	protected Log log = new UnitTestLog();
	private ScalarValueBuilder builder;

	@Before
	public void init() {
		runner = initRunner();
		runner.begin("");
		builder = createSvc();
	}
	
	private ScalarValueBuilder createSvc() {
		return factorySvc.createScalarValueBuilder(runner.getRegistry());
	}
//
//	protected void log(String s) {
//		log.log(s);
//	}

}
