package org.delia.scope.scopetest.typestruct;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class FieldOptionalTests extends ScopeTestBase {

	//-- int --
	@Test
	public void testInt() {
		DValue dval = createAndInsert("int", "55");
		assertEquals(55, dval.asInt());
	}
	@Test
	public void testIntNull() {
		DValue dval = createAndInsertNull("int", "null");
		assertEquals(null, dval);
	}
	
	// -- string --
	@Test
	public void testString() {
		DValue dval = createAndInsert("string", "'abc'");
		assertEquals("abc", dval.asString());
	}
	@Test
	public void testStringNull() {
		DValue dval = createAndInsertNull("string", "null");
		assertEquals(null, dval);
	}
	
	//-- boolean --
	@Test
	public void testBoolean() {
		DValue dval = createAndInsert("boolean", "true");
		assertEquals(true, dval.asBoolean());
	}
	@Test
	public void testBoolean2() {
		DValue dval = createAndInsertNull("boolean", "null");
		assertEquals(null, dval);
	}
	
	//-- long --
	@Test
	public void testLong() {
		DValue dval = createAndInsert("long", "55");
		assertEquals(55, dval.asLong());
	}
	@Test
	public void testLong2() {
		DValue dval = createAndInsertNull("long", "null");
		assertEquals(null, dval);
	}
	
	//-- number --
	@Test
	public void testNumber() {
		DValue dval = createAndInsert("number", "55");
		assertEquals(55.0, dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber2() {
		DValue dval = createAndInsertNull("number", "null");
		assertEquals(null, dval);
	}

	//-- date --
	@Test
	public void testDate() {
		DValue dval = createAndInsert("date", "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());
	}
	@Test
	public void testDate2() {
		DValue dval = createAndInsertNull("date", "null");
		assertEquals(null, dval);
	}
	
	// --
	@Before
	public void init() {
		runner = initRunner();
	}
	
	private void createFlightType(String type) {
		String src = String.format("type Flight struct {field1 %s optional} end", type);
		this.execTypeStatement(src);
	}
	private DValue insertAndQueryEx(String valStr, boolean expectNull) {
		QueryResponse qresp= insertAndQuery(valStr);
		return getOne("field1", qresp, expectNull);
	}
	private QueryResponse insertAndQuery(String valStr) {
		String src = String.format("insert Flight {field1:%s}", valStr);
		execInsertStatement(src);
		
		//now query it
		src = String.format("let a = Flight");
		return execLetStatementOne(src, "Flight");
	}

	private DValue createAndInsert(String type, String valStr) {
		createFlightType(type);
		baseBeginSession();
		DValue dval = insertAndQueryEx(valStr, false);
		return dval;
	}
	private DValue createAndInsertNull(String type, String valStr) {
		createFlightType(type);
		baseBeginSession();
		DValue dval = insertAndQueryEx(valStr, true);
		return dval;
	}
}
