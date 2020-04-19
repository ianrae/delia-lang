package org.delia.scope.scopetest.insert;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class InsertTests extends ScopeTestBase {

	//-- int --
	@Test
	public void testInt() {
		DValue dval = createAndInsert("int", false, "55");
		assertEquals(55, dval.asInt());
	}
	@Test
	public void testIntMax() {
		Integer n = Integer.MAX_VALUE;
		DValue dval = createAndInsert("int", false, n.toString());
		assertEquals(n.intValue(), dval.asInt());
	}
	@Test
	public void testIntNullExplicit() {
		DValue dval = createAndInsertNull("int", true, "null");
		assertEquals(null, dval);
	}
	@Test
	public void testIntNullImplicit() {
		testNullImplicit("int"); 
	}

	// -- string --
	@Test
	public void testString() {
		DValue dval = createAndInsert("string", false, "'abc'");
		assertEquals("abc", dval.asString());
	}
	@Test
	public void testStringEmpty() {
		DValue dval = createAndInsert("string", false, "''");
		assertEquals("", dval.asString());
	}
	@Test
	public void testStringNullExplicit() {
		DValue dval = createAndInsertNull("string", true, "null");
		assertEquals(null, dval);
	}
	@Test
	public void testStringNullImplicit() {
		testNullImplicit("string"); 
	}
		
	//-- boolean --
	@Test
	public void testBoolean() {
		DValue dval = createAndInsert("boolean", false, "true");
		assertEquals(true, dval.asBoolean());
	}
	@Test
	public void testBooleanNullExplicit() {
		DValue dval = createAndInsertNull("boolean", true, "null");
		assertEquals(null, dval);
	}
	@Test
	public void testBooleanNullImplicit() {
		testNullImplicit("boolean"); 
	}
	
		//-- long --
	@Test
	public void testLong() {
		DValue dval = createAndInsert("long", false, "1234");
		assertEquals(1234L, dval.asLong());
	}
	@Test
	public void testLongMax() {
		Long n = Long.MAX_VALUE;
		DValue dval = createAndInsert("long", false, n.toString());
		assertEquals(n.longValue(), dval.asLong());
	}
	@Test
	public void testLongNullExplicit() {
		DValue dval = createAndInsertNull("long", true, "null");
		assertEquals(null, dval);
	}
	@Test
	public void testLongNullImplicit() {
		testNullImplicit("long"); 
	}
	
		
		//-- number --
	@Test
	public void testNumber() {
		DValue dval = createAndInsert("number", false, "1234.567");
		assertEquals(1234.567, dval.asNumber(), DELTA);
	}
	//TODO: fix. support 'e' notation
//	@Test
//	public void testNumberMax() {
//		Double n = Double.MAX_VALUE;
//		DValue dval = createAndInsert("number", false, n.toString());
//		assertEquals(n.longValue(), dval.asNumber(), DELTA);
//	}
	@Test
	public void testNumberNullExplicit() {
		DValue dval = createAndInsertNull("number", true, "null");
		assertEquals(null, dval);
	}
	@Test
	public void testNumberNullImplicit() {
		testNullImplicit("number"); 
	}
	
		//-- date --
	@Test
	public void testDate() {
		DValue dval = createAndInsert("date", false, "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());
	}
	//TODO test full date string, and timezones
	
	@Test
	public void testDateNullExplicit() {
		DValue dval = createAndInsertNull("date", true, "null");
		assertEquals(null, dval);
	}
	@Test
	public void testDateNullImplicit() {
		testNullImplicit("date"); 
	}
	
	// --
	private int nextVarNum = 1;

	@Before
	public void init() {
		runner = initRunner();
	}

	private void createFlightType(String type, String modifier) {
		String src = String.format("type Flight struct {field1 %s %s} end", type, modifier);
		this.execTypeStatement(src);
	}
	private DValue insertAndQueryEx(String valStr, boolean expectNull, int expectedSize) {
		QueryResponse qresp= insertAndQuery(valStr, expectedSize);
		return getLastOne("field1", qresp, expectNull, expectedSize);
	}
	private QueryResponse insertAndQuery(String valStr, int expectedSize) {
		String src = String.format("insert Flight {field1:%s}", valStr);
		execInsertStatement(src);

		//now query it
		String varName = String.format("a%d", nextVarNum++);
		src = String.format("let %s = Flight", varName);
		return execLetStatementMulti(src, expectedSize);
	}

	private DValue createAndInsert(String type, boolean isOptional, String valStr) {
		createFlightType(type, isOptional ? "optional" : "");
		DValue dval = insertAndQueryEx(valStr, false, 1);
		return dval;
	}
	private DValue createAndInsertNull(String type, boolean isOptional, String valStr) {
		createFlightType(type, isOptional ? "optional" : "");
		DValue dval = insertAndQueryEx(valStr, true, 1);
		return dval;
	}
	private void testNullImplicit(String type) {
		createFlightType(type, "optional");

		String src = String.format("insert Flight {}"); //no value passed
		execInsertStatement(src);

		//now query it
		String varName = String.format("a%d", nextVarNum++);
		src = String.format("let %s = Flight", varName);
		QueryResponse qresp = execLetStatementMulti(src, 1);
		DValue dval = qresp.getOne();
		assertEquals(null, dval.asStruct().getField("field1"));
	}
	
}
