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
		src = String.format("let a = Employee[true]");
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
		src = String.format("let a = User[true]");
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
		src = String.format("let a = User[true]");
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
		src = String.format("let a = User[true]");
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

	private void createSomeType(String type, String baseType, String field1) {
		String src = String.format("type %s %s {%s} end", type, baseType, field1);
		this.execTypeStatement(src);
		baseBeginSession();
	}
	private void createScalarSomeType(String type, String baseType) {
		String src = String.format("type %s %s end", type, baseType);
		this.execTypeStatement(src);
	}

}
