package org.delia.scope.scopetest.letquery;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.junit.Before;
import org.junit.Test;

public class QueryCountTests extends ScopeTestBase {

	//-- int --
	@Test
	public void testInt() {
		createAndInsert("int", "55");
		
		queryOneInt("Flight[true].count()", 1);
		queryOneInt("Flight[true].count()", 1); //again
		queryOneInt("Flight[55].count()", 1);
		
		queryOneInt("Flight[66].count()", 0);
		
		runLetInt("a1.count()", 1);
	}
	
	@Test
	public void testInt2() {
		createAndInsert("int", "55");
		insert("56");
		
		queryOneInt("Flight[true].count()", 2);
		queryOneInt("Flight[true].count()", 2); //again
		queryOneInt("Flight[55].count()", 1);
		queryOneInt("Flight[56].count()", 1);
		
		queryOneInt("Flight[66].count()", 0);
		
		runLetInt("a1.count()", 2);
	}
	
	@Test
	public void testMax() {
		createAndInsert("int", "55");
		insert("56");
		
		queryOneInt("Flight[true].field1.max()", 56);
		runLetInt("a1.max()", 56);
	}
	
	@Test
	public void testMin() {
		createAndInsert("int", "55");
		insert("56");
		
		queryOneInt("Flight[true].field1.min()", 55);
		runLetInt("a1", 55);
		runLetInt("a1.min()", 55); //this is wierd. a1 is 55.
		//TODO - fix. we store qresp results a list, even if single value
		//how can we tell the difference between a list[1] and a single value??
	}
	
	@Test
	public void testEmpty() {
		createFlightType("int", "unique");

		//https://dba.stackexchange.com/questions/25435/why-does-ansi-sql-define-sumno-rows-as-null
		//sql standard returns NULL for min of empty set.
		queryNullInt("Flight[true].field1.min()");
		runLetNull("a1");
	}
	
	@Test
	public void testMinMaxNull() {
		createFlightType("int", "optional");
		insert("55");
		insert("null");
		insert("47");
		
		queryOneInt("Flight[true].field1.min()", 47);
		queryOneInt("Flight[true].field1.max()", 55);
		queryOneInt("Flight[true].field1.count()", 2); //we don't count null values
		queryOneInt("Flight[true].count()", 3); //are 3 records
	}
	
	// --
	private int nextVarNum = 1;

	@Before
	public void init() {
		runner = initRunner();
	}

	private void createFlightType(String type, String modifier) {
		String src = String.format("type Flight struct {field1 %s %s} end", type, modifier);
		ResultValue res = execTypeStatement(src);
		baseBeginSession();
		chkResOK(res);
	}
	private QueryResponse execQuery(String letSrc, int expectedSize) {
		//now query it
		String varName = String.format("a%d", nextVarNum++);
		String src = String.format("let %s = %s", varName, letSrc);
		return execLetStatementMulti(src, expectedSize);
	}
	private DValue execQueryOne(String letSrc, Shape expectedShape) {
		//now query it
		String varName = String.format("a%d", nextVarNum++);
		String src = String.format("let %s = %s", varName, letSrc);
		QueryResponse qresp = execLetStatementMulti(src, 1, expectedShape);
		return qresp.getOne();
	}
	private DValue execQueryNull(String letSrc) {
		//now query it
		String varName = String.format("a%d", nextVarNum++);
		String src = String.format("let %s = %s", varName, letSrc);
		QueryResponse qresp = this.execLetStatementNull(src);
		return null;
	}

	private void createAndInsert(String type, String valStr) {
		createFlightType(type, "unique");
		insert(valStr);
	}
	private void insert(String valStr) {
		String src = String.format("insert Flight {field1:%s}", valStr);
		ResultValue res = execInsertStatement(src);
		chkResOK(res);
	}
	private void queryOneInt(String src, int expected) {
		DValue dval = execQueryOne(src, null);
		assertEquals(expected, dval.asInt());
	}
	private DValue queryOneIntWithShape(String src, int expected) {
		DValue dval = execQueryOne(src, null);
		assertEquals(expected, dval.asInt());
		return dval;
	}
	protected DValue runLetInt(String valStr, int expected) {
		return queryOneIntWithShape(valStr, expected);
	}
	protected void runLetNull(String valStr) {
		//use explicit type since otherwise 55 will be seen as int, not long
		String src = String.format("let a = %s", valStr);
//		LetStatementExp exp2 = chelper.chkScalarLet(src, "queryResponse");
		ResultValue res = runner.continueExecution(src);
		assertEquals(true, res.ok);
		DValue dval = (DValue) res.val;
		assertEquals(null, dval);
	}
	
	
	
	private void queryNullInt(String src) {
		DValue dval = execQueryNull(src);
		assertEquals(null, dval);
	}

}
