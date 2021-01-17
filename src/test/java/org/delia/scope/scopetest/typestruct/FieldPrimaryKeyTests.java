package org.delia.scope.scopetest.typestruct;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class FieldPrimaryKeyTests extends ScopeTestBase {
	
	//-- int --
	@Test
	public void testInt() {
		DValue dval = createAndInsert("int", "55");
		assertEquals(55, dval.asInt());
	}
	@Test
	public void testInt2() {
		DValue dval = createAndInsert("int", "55");
		assertEquals(55, dval.asInt());
		dval = insertAndQueryEx("56", false, 2);
		assertEquals(56, dval.asInt());
	}
	@Test
	public void testInt2Fail() {
		DValue dval = createAndInsert("int", "55");
		assertEquals(55, dval.asInt());
		insertFail("55", 1, "duplicate-unique-value");
	}
	@Test
	public void testIntNull() {
		createAndInsertNull("int", "null");
	}
	
	// -- string --
	@Test
	public void testString() {
		DValue dval = createAndInsert("string", "'abc'");
		assertEquals("abc", dval.asString());
	}
	@Test
	public void testString2() {
		DValue dval = createAndInsert("string", "'abc'");
		assertEquals("abc", dval.asString());
		dval = insertAndQueryEx("'def'", false, 2);
		assertEquals("def", dval.asString());
	}
	@Test
	public void testStringFail() {
		DValue dval = createAndInsert("string", "'abc'");
		assertEquals("abc", dval.asString());
		insertFail("'abc'", 1, "duplicate-unique-value");
	}
	@Test
	public void testStringNull() {
		createAndInsertNull("string", "null");
	}
	
	//-- boolean --
	@Test
	public void testBoolean() {
		DValue dval = createAndInsert("boolean", "true");
		assertEquals(true, dval.asBoolean());
	}
	@Test
	public void testBoolean2() {
		DValue dval = createAndInsert("boolean", "true");
		assertEquals(true, dval.asBoolean());
		dval = insertAndQueryEx("false", false, 2);
		assertEquals(false, dval.asBoolean());
	}
	@Test
	public void testBooleanFail() {
		DValue dval = createAndInsert("boolean", "true");
		assertEquals(true, dval.asBoolean());
		insertFail("true", 1, "duplicate-unique-value");
	}
	@Test
	public void testBooleanNull() {
		createAndInsertNull("boolean", "null");
	}
	
	//-- long --
	@Test
	public void testLong() {
		DValue dval = createAndInsert("long", "55");
		assertEquals(55, dval.asLong());
	}
	@Test
	public void testLong2() {
		DValue dval = createAndInsert("long", "55");
		assertEquals(55, dval.asLong());
		dval = insertAndQueryEx("56", false, 2);
		assertEquals(56, dval.asLong());
	}
	@Test
	public void testLongFail() {
		DValue dval = createAndInsert("long", "55");
		assertEquals(55, dval.asLong());
		insertFail("55", 1, "duplicate-unique-value");
	}
	@Test
	public void testLongNull() {
		createAndInsertNull("long", "null");
	}
	
	//-- number --
//	@Test
//	public void testNumber() {
//		DValue dval = createAndInsert("number", "55");
//		assertEquals(55.0, dval.asNumber(), DELTA);
//	}
//	@Test
//	public void testNumber2() {
//		DValue dval = createAndInsert("number", "55");
//		assertEquals(55.0, dval.asNumber(), DELTA);
//		dval = insertAndQueryEx("56", false, 2);
//		assertEquals(56.0, dval.asNumber(), DELTA);
//	}
//	@Test
//	public void testNumberFail() {
//		DValue dval = createAndInsertFail("number", "55");
//		assertEquals(55.0, dval.asNumber(), DELTA);
//		insertFail("55", 1, "duplicate-unique-value");
//	}
//	@Test
//	public void testNumberNull() {
//		createAndInsertNull("number", "null");
//	}

	//-- date --
	@Test
	public void testDate() {
		DValue dval = createAndInsert("date", "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());
	}
	@Test
	public void testDate2() {
		DValue dval = createAndInsert("date", "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());
		dval = insertAndQueryEx("'1956'", false, 2);
		assertEquals("1956-01-01T00:00:00.000+0000", dval.asString());
	}
	@Test
	public void testDateFail() {
		DValue dval = createAndInsert("date", "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());
		insertFail("'1955'", 1, "duplicate-unique-value");
	}
	@Test
	public void testDateNull() {
		createAndInsertNull("date", "null");
	}
	
	// --
	private int nextVarNum = 1;
	
	@Before
	public void init() {
		runner = initRunner();
	}
	
	private void createFlightType(String type) {
		String src = String.format("type Flight struct {field1 %s primaryKey} end", type);
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
		src = String.format("let %s = Fligh[true]t", varName);
		return execLetStatementMulti(src, expectedSize);
	}
	private ResultValue insertFail(String valStr, int expectedErrorCount, String errId) {
		String src = String.format("insert Flight {field1:%s}", valStr);
		return execInsertFail(src, expectedErrorCount, errId);
	}

	private DValue createAndInsert(String type, String valStr) {
		createFlightType(type);
		baseBeginSession();

		DValue dval = insertAndQueryEx(valStr, false, 1);
		return dval;
	}
	private DValue createAndInsertNull(String type, String valStr) {
		createFlightType(type);
		baseBeginSession();

		insertFail(valStr, 1, "NODATA");
		return null;
	}
}
