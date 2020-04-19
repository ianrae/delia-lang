package org.delia.scope.scopetest.delete;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * todo
 * @author Ian Rae
 *
 */

public class DeleteTests extends ScopeTestBase {

	//-- int --
	@Test
	public void testInt() {
		DValue dval = createAndInsert("int", false, "55");
		assertEquals(55, dval.asInt());

		dval = deleteAndQuery("55", 0);
		assertEquals(null, dval);
	}
	@Test
	public void testIntNotFound() {
		DValue dval = createAndInsert("int", false, "55");
		assertEquals(55, dval.asInt());

		dval = deleteAndQuery("56", 1);
		assertEquals(55, dval.asStruct().getField("field1").asInt());
		chkValid(dval);
	}
	// -- string --
	@Test
	public void testString() {
		DValue dval = createAndInsert("string", false, "'abc'");
		assertEquals("abc", dval.asString());

		dval = deleteAndQuery("'abc'", 0);
		assertEquals(null, dval);
	}
	@Test
	public void testStringNotFound() {
		DValue dval = createAndInsert("string", false, "'abc'");
		assertEquals("abc", dval.asString());

		dval = deleteAndQuery("'def'", 1);
		assertEquals("abc", dval.asStruct().getField("field1").asString());
		chkValid(dval);
	}
	//-- long --
	@Test
	public void testLong() {
		DValue dval = createAndInsert("long", false, "1234");
		assertEquals(1234L, dval.asLong());

		dval = deleteAndQuery("1234", 0);
		assertEquals(null, dval);
	}
	@Test
	public void testLongNotFound() {
		DValue dval = createAndInsert("long", false, "1234");
		assertEquals(1234L, dval.asLong());

		dval = deleteAndQuery("4000", 1);
		assertEquals(1234L, dval.asStruct().getField("field1").asLong());
		chkValid(dval);
	}
//	//-- number -- not allowed as primarykey
//	@Test
//	public void testNumber() {
//		DValue dval = createAndInsert("number", false, "1234.567");
//		assertEquals(1234.567, dval.asNumber(), DELTA);
//
//		dval = deleteAndQuery("1234.567", 0);
//		assertEquals(null, dval);
//	}
//	@Test
//	public void testNumberNotFound() {
//		DValue dval = createAndInsert("number", false, "1234.567");
//		assertEquals(1234.567, dval.asNumber(), DELTA);
//
//		dval = deleteAndQuery("-56.78", 1);
//		assertEquals(1234.567, dval.asStruct().getField("field1").asNumber(), DELTA);
//		chkValid(dval);
//	}
	//-- date --
	@Test
	public void testDate() {
		DValue dval = createAndInsert("date", false, "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());
		
		//this is our default string format
		//TODO: need to handle timezone
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());

		dval = deleteAndQuery("'1955'", 0);
		assertEquals(null, dval);
	}
	@Test
	public void testDateNotFound() {
		DValue dval = createAndInsert("date", false, "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());

		dval = deleteAndQuery("'1966'", 1);
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asStruct().getField("field1").asString());
		chkValid(dval);
	}

	// --
	private int nextVarNum = 1;

	@Before
	public void init() {
		runner = initRunner();
	}

	private void createFlightType(String type, String modifier) {
		String src = String.format("type Flight struct {field1 %s unique %s} end", type, modifier);
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
		return execLetStatementMulti(src, expectedSize, null);
	}

	private DValue createAndInsert(String type, boolean isOptional, String valStr) {
		createFlightType(type, isOptional ? "optional" : "");
		DValue dval = insertAndQueryEx(valStr, false, 1);
		return dval;
	}


	private DValue deleteAndQuery(String valStr, int expectedSize) {
		String src = String.format("delete Flight[%s]", valStr);
		execDeleteStatement(src);

		//now query it
		String varName = String.format("a%d", nextVarNum++);
		src = String.format("let %s = Flight", varName);
		QueryResponse qresp = execLetStatementMulti(src, expectedSize, null);
		if (expectedSize == 0) {
			return null;
		}
		return qresp.getOne();
	}

}
