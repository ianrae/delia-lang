package org.delia.parser;

import static org.junit.Assert.assertEquals;

import org.delia.base.UnitTestLog;
import org.delia.runner.CompilerHelper;
import org.delia.runner.RunnerHelper;
import org.junit.Test;



public class CRUDTests {
	
	@Test
	public void testInsert() {
		chkInsert("insert Actor { }", "insert Actor { }");
		chkInsert("insert Actor { 'bob' }", "insert Actor {'bob' }");
		chkInsert("insert Actor { true }", "insert Actor {true }");
	}
	@Test
	public void testInsert2() {
		chkInsert("insert Actor { field1:'bob' }", "insert Actor {field1: 'bob' }");
		chkInsert("insert Actor { field1:'bob', field2:44 }", "insert Actor {field1: 'bob',field2: 44 }");
		chkInsert("insert Actor { field1:'bob', field2:false }", "insert Actor {field1: 'bob',field2: false }");
	}

	@Test
	public void testUpdate() {
		chkUpdate("update Actor[45] { field1:'bob' }", "update Actor[45] {field1: 'bob' }");
//		chkInsert("insert Actor { field1:'bob', field2:44 }", "insert Actor {field1: 'bob',field2: 44 }");
//		chkInsert("insert Actor { field1:'bob', field2:false }", "insert Actor {field1: 'bob',field2: false }");
	}
	
	@Test
	public void testDelete() {
		chkDelete("delete Actor [45]", "delete Actor[45]");
		chkDelete("delete Actor [true]", "delete Actor[true]");
	}
	
	// --
	private CompilerHelper chelper = new CompilerHelper(null, new UnitTestLog());

	private void chkInsert(String input, String output) {
		chelper.chkInsert(input, output);
	}
	private void chkUpdate(String input, String output) {
		chelper.chkUpdate(input, output);
	}
	private void chkDelete(String input, String output) {
		chelper.chkDelete(input, output);
	}
}
