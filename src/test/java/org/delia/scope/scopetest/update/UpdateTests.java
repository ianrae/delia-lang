package org.delia.scope.scopetest.update;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

/**
 * todo
 * -inheritance tests.
 *  -update field in base class
 *  
 * -update 0 fields. should pass and return 0
 * 
 * -update a unique field. re-validate
 * -update a primaryKey field. ""
 * 
 * -test validation
 *  -type int Grade
 *   -update val > 100
 *  -struct rule firstName.maxlen(4)
 *  -struct rule points < 10
 *  
 * -test update of more than one row
 *  -eg. set all with firstname bob to 'Bob'
 * @author Ian Rae
 *
 */

public class UpdateTests extends ScopeTestBase {

	//-- int --
	@Test
	public void testInt() {
		DValue dval = createAndInsert("int", false, "55");
		assertEquals(55, dval.asInt());

		dval = updateAndQuery("56", 1);
		assertEquals(56, dval.asStruct().getField("field1").asInt());
		chkValid(dval);
	}
	@Test
	public void testIntNullExplicit() {
		DValue dval = createAndInsertNull("int", true, "null");
		assertEquals(null, dval);

		dval = updateAndQuery("56", 1);
		assertEquals(56, dval.asStruct().getField("field1").asInt());
		chkValid(dval);
	}
	@Test
	public void testIntNullImplicit() {
		testNullImplicit("int"); 

		DValue dval = updateAndQuery("56", 1);
		assertEquals(56, dval.asStruct().getField("field1").asInt());
		chkValid(dval);
	}

	// -- string --
	@Test
	public void testString() {
		DValue dval = createAndInsert("string", false, "'abc'");
		assertEquals("abc", dval.asString());

		dval = updateAndQuery("'def'", 1);
		assertEquals("def", dval.asStruct().getField("field1").asString());
		chkValid(dval);
	}
	@Test
	public void testStringEmpty() {
		DValue dval = createAndInsert("string", false, "''");
		assertEquals("", dval.asString());

		dval = updateAndQuery("'def'", 1);
		assertEquals("def", dval.asStruct().getField("field1").asString());
		chkValid(dval);
	}
	@Test
	public void testStringNullExplicit() {
		DValue dval = createAndInsertNull("string", true, "null");
		assertEquals(null, dval);

		dval = updateAndQuery("'def'", 1);
		assertEquals("def", dval.asStruct().getField("field1").asString());
		chkValid(dval);
	}
	@Test
	public void testStringNullImplicit() {
		testNullImplicit("string"); 

		DValue dval = updateAndQuery("'def'", 1);
		assertEquals("def", dval.asStruct().getField("field1").asString());
		chkValid(dval);
	}

	//-- boolean --
	@Test
	public void testBoolean() {
		DValue dval = createAndInsert("boolean", false, "true");
		assertEquals(true, dval.asBoolean());

		dval = updateAndQuery("false", 1);
		assertEquals(false, dval.asStruct().getField("field1").asBoolean());
		chkValid(dval);
	}
	@Test
	public void testBooleanNullExplicit() {
		DValue dval = createAndInsertNull("boolean", true, "null");
		assertEquals(null, dval);

		dval = updateAndQuery("false", 1);
		assertEquals(false, dval.asStruct().getField("field1").asBoolean());
		chkValid(dval);
	}
	@Test
	public void testBooleanNullImplicit() {
		testNullImplicit("boolean"); 

		DValue dval = updateAndQuery("false", 1);
		assertEquals(false, dval.asStruct().getField("field1").asBoolean());
		chkValid(dval);
	}

	//-- long --
	@Test
	public void testLong() {
		DValue dval = createAndInsert("long", false, "1234");
		assertEquals(1234L, dval.asLong());

		dval = updateAndQuery("4000", 1);
		assertEquals(4000L, dval.asStruct().getField("field1").asLong());
		chkValid(dval);
	}
	@Test
	public void testLongNullExplicit() {
		DValue dval = createAndInsertNull("long", true, "null");
		assertEquals(null, dval);

		dval = updateAndQuery("4000", 1);
		assertEquals(4000L, dval.asStruct().getField("field1").asLong());
		chkValid(dval);
	}
	@Test
	public void testLongNullImplicit() {
		testNullImplicit("long"); 

		DValue dval = updateAndQuery("4000", 1);
		assertEquals(4000L, dval.asStruct().getField("field1").asLong());
		chkValid(dval);
	}


	//-- number --
	@Test
	public void testNumber() {
		DValue dval = createAndInsert("number", false, "1234.567");
		assertEquals(1234.567, dval.asNumber(), DELTA);

		dval = updateAndQuery("-56.78", 1);
		assertEquals(-56.78, dval.asStruct().getField("field1").asNumber(), DELTA);
		chkValid(dval);
	}
	@Test
	public void testNumberNullExplicit() {
		DValue dval = createAndInsertNull("number", true, "null");
		assertEquals(null, dval);

		dval = updateAndQuery("-56.78", 1);
		assertEquals(-56.78, dval.asStruct().getField("field1").asNumber(), DELTA);
		chkValid(dval);
	}
	@Test
	public void testNumberNullImplicit() {
		testNullImplicit("number"); 

		DValue dval = updateAndQuery("-56.78", 1);
		assertEquals(-56.78, dval.asStruct().getField("field1").asNumber(), DELTA);
		chkValid(dval);
	}

	//-- date --
	@Test
	public void testDate() {
		DValue dval = createAndInsert("date", false, "'1955'");
		assertEquals("1955-01-01T00:00:00.000+0000", dval.asString());

		dval = updateAndQuery("'1966'", 1);
		assertEquals("1966-01-01T00:00:00.000+0000", dval.asStruct().getField("field1").asString());
		chkValid(dval);
	}
	//TODO test full date string, and timezones

	@Test
	public void testDateNullExplicit() {
		DValue dval = createAndInsertNull("date", true, "null");
		assertEquals(null, dval);

		dval = updateAndQuery("'1966'", 1);
		assertEquals("1966-01-01T00:00:00.000+0000", dval.asStruct().getField("field1").asString());
		chkValid(dval);
	}
	@Test
	public void testDateNullImplicit() {
		testNullImplicit("date"); 

		DValue dval = updateAndQuery("'1966'", 1);
		assertEquals("1966-01-01T00:00:00.000+0000", dval.asStruct().getField("field1").asString());
		chkValid(dval);
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


	private DValue updateAndQuery(String valStr, int expectedSize) {
		String src = String.format("update Flight {field1:%s}", valStr);
		execUpdateStatement(src);

		//now query it
		String varName = String.format("a%d", nextVarNum++);
		src = String.format("let %s = Flight", varName);
		QueryResponse qresp = execLetStatementMulti(src, expectedSize);
		return qresp.getOne();
	}

}
