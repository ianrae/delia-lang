package org.delia.scope.scopetest.typescalar;

import static org.junit.Assert.assertEquals;

import org.delia.runner.DeliaException;
import org.delia.scope.scopetest.ScopeTestBase;
import org.junit.Before;
import org.junit.Test;

public class ScalarTypeDefinitionTests extends ScopeTestBase {
	
	@Test
	public void testDefOK() {
		createGradeType("int"); 
	}
	@Test(expected=DeliaException.class)
	public void testReservedWordFail() {
		createScalarSomeTypeRaw("int", "int"); 
		this.runner.begin(basePendingSrc);
	}
	
	@Test
	public void testInheritance() {
		createScalarSomeType("Grade", "int");
		assertEquals(true, runner.getCompileState().compiledTypeMap.containsKey("Grade"));
		chelper = helper.createCompilerHelper();
		createScalarSomeType("ScienceGrade", "Grade"); 
	}
	@Test
	public void testInsertNotAllowed() {
		createScalarSomeType("Grade", "int");
		String src = String.format("insert Grade {10}");
		execInsertFail(src, 1, "type.not.struct");
	}
	
	
	// --
	@Before
	public void init() {
		runner = initRunner();
	}
	
	private void createScalarSomeType(String type, String baseType) {
		String src = String.format("type %s %s end", type, baseType);
		this.execTypeStatement(src);
		baseBeginSession();
	}
	private void createScalarSomeTypeRaw(String type, String baseType) {
		String src = String.format("type %s %s end", type, baseType);
		this.execTypeStatement(src);
	}
	private void createGradeType(String type) {
		String src = String.format("type Grade %s end", type);
		this.execTypeStatement(src);
		baseBeginSession();
	}
}
