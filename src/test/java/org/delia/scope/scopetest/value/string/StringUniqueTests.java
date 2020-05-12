package org.delia.scope.scopetest.value.string;

import org.junit.Before;
import org.junit.Test;

public class StringUniqueTests extends StringTestBase {


	@Test
	public void test6Insert() {
		createScalarType("string", "");
		createStructType("string unique", "");
		beginSession();

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		chkFieldString("''", "");
	}	
	@Test
	public void test6InsertFail() {
		createScalarType("string", "");
		createStructType("string unique", "");
		beginSession();

		deleteBeforeInsertFlag = false;
		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		chkFieldInsertFail("'bob'", "duplicate-unique-value");
	}	
	
	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("string", "");
		createStructType("string unique", "");
		beginSession();
		deleteBeforeInsertFlag = false;

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob"); //1st record
		nextId++;
		chkFieldString("'sue'", "sue"); //add 2nd record
		chkUpdateMulti("'art'", 2);  //change 2nd record it to art
		nextId=44;
		//change 1st record TODO: should fail
		chkFieldUpdateFail("'art'", "duplicate-unique-value");
	}	


	// --
	

	@Before
	public void init() {
		super.init();
	}

}