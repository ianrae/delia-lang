package org.delia.scope.scopetest.typestruct;

import static org.junit.Assert.assertEquals;

import org.delia.db.sql.NewLegacyRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class FieldTests extends ScopeTestBase {

	//-- int --
	@Test
	public void testInt() {
		DValue dval = createAndInsert("int", "55");
		assertEquals(55, dval.asInt());
	}
	@Test
	public void testInt2() {
		DValue dval = createAndInsert("int", "-55");
		assertEquals(-55, dval.asInt());
	}
	@Test
	public void testInt3() {
		DValue dval = createAndInsert("int", "0");
		assertEquals(0, dval.asInt());
	}
	@Test
	public void testInt4a() {
		Integer n = Integer.MAX_VALUE;
		DValue dval = createAndInsert("int", n.toString());
		assertEquals(n.intValue(), dval.asInt());
	}
	@Test
	public void testInt4b() {
		Integer n = Integer.MIN_VALUE;
		DValue dval = createAndInsert("int", n.toString());
		assertEquals(n.intValue(), dval.asInt());
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
	public void testString1() {
		DValue dval = createAndInsert("string", "\"abc\"");
		assertEquals("abc", dval.asString());
	}
	@Test
	public void testString2() {
		DValue dval = createAndInsert("string", "''");
		assertEquals("", dval.asString());
	}
	@Test
	public void testString3() {
		DValue dval = createAndInsert("string", "'©'");
		assertEquals("©", dval.asString());
	}
	@Test
	public void testStringNull() {
		createAndInsertNull("string", "null");
	}
	
	//-- boolean --
	@Test
	public void testBoolean() {
		DValue dval = createAndInsert("boolean", "false");
		assertEquals(false, dval.asBoolean());
	}
	@Test
	public void testBoolean2() {
		DValue dval = createAndInsert("boolean", "true");
		assertEquals(true, dval.asBoolean());
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
		assertEquals(55, dval.asInt());
	}
	@Test
	public void testLong2() {
		DValue dval = createAndInsert("long", "-55");
		assertEquals(-55, dval.asLong());
	}
	@Test
	public void testLong3() {
		DValue dval = createAndInsert("long", "0");
		assertEquals(0, dval.asLong());
	}
	@Test
	public void testLong4a() {
		Long n = Long.MAX_VALUE;
		DValue dval = createAndInsert("long", n.toString());
		assertEquals(n.longValue(), dval.asLong());
	}
	@Test
	public void testLong4b() {
		Long n = Long.MIN_VALUE;
		DValue dval = createAndInsert("long", n.toString());
		assertEquals(n.longValue(), dval.asLong());
	}
	@Test
	public void testLongNull() {
		createAndInsertNull("long", "null");
	}
	
	//-- number --
	@Test
	public void testNumber() {
		DValue dval = createAndInsert("number", "55");
		assertEquals(55.0, dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber2() {
		DValue dval = createAndInsert("number", "-55");
		assertEquals(-55, dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber3() {
		DValue dval = createAndInsert("number", "0");
		assertEquals(0, dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber3a() {
		DValue dval = createAndInsert("number", "1025.3476");
		assertEquals(1025.3476, dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber4a() {
		Long n = Long.MAX_VALUE;
		DValue dval = createAndInsert("number", n.toString());
		assertEquals(n.doubleValue(), dval.asNumber(), DELTA);
	}
	@Test
	public void testNumber4b() {
		Long n = Long.MIN_VALUE;
		DValue dval = createAndInsert("number", n.toString());
		assertEquals(n.doubleValue(), dval.asNumber(), DELTA);
	}
	@Test
	public void testNumberNull() {
		createAndInsertNull("number", "null");
	}
	
	//-- date --
	@Test
	public void testDate() {
		DValue dval = createAndInsert("date", "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());
	}
	@Test
	public void testDate2() {
		DValue dval = createAndInsert("date", "'2007-06-11'");
		assertEquals("2007-06-11T00:00:00.000+0000", dval.asString());
	}
	@Test
	public void testDate3() {
		//yyyy-MM-dd'T'HH:mm:ss
		DValue dval = createAndInsert("date", "'2007-06-11T11:15:51'");
		assertEquals("2007-06-11T11:15:51.000+0000", dval.asString());
	}
	@Test
	public void testDateNull() {
		createAndInsertNull("date", "null");
	}
	
	@Before
	public void init() {
		runner = initRunner();
	}
	
	// --
	private void createFlightType(String type) {
		String src = String.format("type Flight struct {field1 %s} end", type);
		execTypeStatement(src);
	}
	private DValue insertAndQueryEx(NewLegacyRunner runner, String valStr) {
		QueryResponse qresp= insertAndQuery(runner, valStr);
		return getOne("field1", qresp, false);
	}
	private QueryResponse insertAndQuery(NewLegacyRunner runner, String valStr) {
		String src = String.format("insert Flight {field1:%s}", valStr);
		execInsertStatement(src);
		
		//now query it
		src = String.format("let a = Flight[true]");
		return execLetStatementOne(src, "Flight");
	}

	private DValue createAndInsert(String type, String valStr) {
		createFlightType(type);
		baseBeginSession();
		DValue dval = insertAndQueryEx(runner, valStr);
		return dval;
	}
	private DValue createAndInsertNull(String type, String valStr) {
		createFlightType(type);
		baseBeginSession();
		insertFail(valStr, 1, "NODATA");
		return null;
	}
	private ResultValue insertFail(String valStr, int expectedErrorCount, String errId) {
		String src = String.format("insert Flight {field1:%s}", valStr);
		return execInsertFail(src, expectedErrorCount, errId);
	}
}
