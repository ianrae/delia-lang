package org.delia.scope.scopetest.typestruct;

import static org.junit.Assert.*;

import org.delia.base.DBHelper;
import org.delia.runner.DeliaException;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class TypeDefinitionTests extends ScopeTestBase {
	
	@Test
	public void testEmptyDefOK() {
		createFlightType(""); //empty type may be useful as a base class
	}
	@Test
	public void testEmptyInsertFail() {
		createFlightType(""); 
		insertFail("", 1, "cant-insert-empty-type");
	}
	@Test(expected=DeliaException.class)
	public void testReservedWordFail() {
		createSomeTypeRaw("int", "struct", ""); 
		this.runner.begin(basePendingSrc);
	}
	
	@Test
	public void testInheritance() {
		createSomeType("User", "struct", ""); 
		assertEquals(true, runner.getCompileState().compiledTypeMap.containsKey("User"));
		chelper = helper.createCompilerHelper();
		createSomeType("Employee", "User", ""); 
	}
	
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
		
		//TODO find all occurences of structtype.getFields() -- doesn't handle inheritance
		
		//now query it
		src = String.format("let a = Employee[true]");
		QueryResponse qresp = execLetStatementMulti(src, 1);
		DValue dval = qresp.getOne();
		assertEquals(10, dval.asStruct().getField("f1").asInt());
		assertEquals(20, dval.asStruct().getField("f2").asInt());
	}

	@Test(expected=DeliaException.class)
	public void testInheritanceFailDupFields() {
		createSomeTypeRaw("User", "struct", "f1 int"); 
//		chelper = helper.createCompilerHelper();
		createSomeTypeRaw("Employee", "User", "f1 int"); 
//		assertEquals(true, runner.getCompileState().compiledTypeMap.containsKey("User"));
		this.runner.begin(basePendingSrc);
	}
	
	// --
	@Before
	public void init() {
		runner = initRunner();
	}
	
	private void createSomeType(String type, String baseType, String field1) {
		String src = String.format("type %s %s {%s} end", type, baseType, field1);
		this.execTypeStatement(src);
		baseBeginSession();
	}
	private void createSomeTypeRaw(String type, String baseType, String field1) {
		String src = String.format("type %s %s {%s} end", type, baseType, field1);
		this.execTypeStatement(src);
	}
	private void createFlightType(String body) {
		String src = String.format("type Flight struct {%s} end", body);
		this.execTypeStatement(src);
		baseBeginSession();

	}
	private DValue insertAndQueryEx(String valStr, boolean expectNull, int expectedSize) {
		QueryResponse qresp= insertAndQuery(valStr, expectedSize);
		return getLastOne("field1", qresp, expectNull, expectedSize);
	}
	private QueryResponse insertAndQuery(String valStr, int expectedSize) {
		String src = String.format("insert Flight {field1:%s}", valStr);
		execInsertStatement(src);
		
		//now query it
		src = String.format("let a = Flight");
		return execLetStatementMulti(src, expectedSize);
	}
	private ResultValue insertFail(String valStr, int expectedErrorCount, String errId) {
		String src = String.format("insert Flight {%s}", valStr);
		return execInsertFail(src, expectedErrorCount, errId);
	}
}
