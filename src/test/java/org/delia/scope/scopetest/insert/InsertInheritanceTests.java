package org.delia.scope.scopetest.insert;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
import org.delia.runner.QueryResponse;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class InsertInheritanceTests extends ScopeTestBase {

	@Test
	public void testInheritanceFields() {
		createSomeType("User", "struct", "f1 int"); 
		assertEquals(true, runner.getCompileState().compiledTypeMap.containsKey("User"));
		chelper = helper.createCompilerHelper();
		createSomeType("Employee", "User", "f2 int"); 
		DBHelper.createTable(dbInterface, "User"); //!! fake schema
		DBHelper.createTable(dbInterface, "Employee"); //!! fake schema
		
		String src = String.format("insert Employee {f1:10, f2:20}");
		execInsertStatement(src);
		
		//now query it
		src = String.format("let a = Employee");
		QueryResponse qresp = execLetStatementMulti(src, 1);
		DValue dval = qresp.getOne();
		assertEquals(10, dval.asStruct().getField("f1").asInt());
		assertEquals(20, dval.asStruct().getField("f2").asInt());
	}
	
	//TODO
	//DONE. 1- insert values are vars like let x = 10, then reference x
	//2 - insert values are user-def int. let x Grade = 10, ... 
	
	
	@Test
	public void testVarInInsert() {
		createSomeType("User", "struct", "f1 int"); 
		DBHelper.createTable(dbInterface, "User"); //!! fake schema
		
		DValue dval = execLetStatementScalar("let x = 10", "int");
		assertEquals(10, dval.asInt());
		
		String src = String.format("insert User {f1:x}");
		execInsertStatement(src);
		
		//now query it
		src = String.format("let a = User");
		QueryResponse qresp = execLetStatementMulti(src, 1);
		dval = qresp.getOne();
		assertEquals(10, dval.asStruct().getField("f1").asInt());
	}
	
	@Test
	public void testVarInInsert2() {
		this.createScalarSomeType("Grade", "int");
		chelper = helper.createCompilerHelper();
		createSomeType("User", "struct", "f1 Grade"); 
		DBHelper.createTable(dbInterface, "User"); //!! fake schema
		
		DValue dval = execLetStatementScalar("let x = 10", "int");
		assertEquals(10, dval.asInt());
		
		String src = String.format("insert User {f1:x}");
		execInsertStatement(src);
		
		//now query it
		src = String.format("let a = User");
		QueryResponse qresp = execLetStatementMulti(src, 1);
		dval = qresp.getOne();
		assertEquals(10, dval.asStruct().getField("f1").asInt());
	}
	@Test
	public void testVarInInsert2a() {
		this.createScalarSomeType("Grade", "int");
		chelper = helper.createCompilerHelper();
		createSomeType("User", "struct", "f1 Grade"); 
		DBHelper.createTable(dbInterface, "User"); //!! fake schema
		
		DValue dval = execLetStatementScalar("let x Grade = 10", "Grade");
		assertEquals(10, dval.asInt());
		
		String src = String.format("insert User {f1:x}");
		execInsertStatement(src);
		
		//now query it
		src = String.format("let a = User");
		QueryResponse qresp = execLetStatementMulti(src, 1);
		dval = qresp.getOne();
		assertEquals(10, dval.asStruct().getField("f1").asInt());
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
	private void createSomeType(String type, String baseType, String field1) {
		String src = String.format("type %s %s {%s} end", type, baseType, field1);
		this.execTypeStatement(src);
	}
	private void createScalarSomeType(String type, String baseType) {
		String src = String.format("type %s %s end", type, baseType);
		this.execTypeStatement(src);
	}

}
