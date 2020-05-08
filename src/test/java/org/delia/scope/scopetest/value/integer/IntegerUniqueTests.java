package org.delia.scope.scopetest.value.integer;

import org.junit.Before;
import org.junit.Test;

public class IntegerUniqueTests extends IntegerTestBase {


	@Test
	public void test6Insert() {
		createScalarType("int", "");
		createStructType("int unique", "");
		beginSession();

		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		chkFieldInsertParseFail("null");
	}	
	@Test
	public void test6InsertFail() {
		createScalarType("int", "");
		createStructType("int unique", "");
		beginSession();

		deleteBeforeInsertFlag = false;
		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		chkFieldInsertFail("'114'", "duplicate-unique-value");
	}	
	
	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("int", "");
		createStructType("int unique", "");
		beginSession();

		deleteBeforeInsertFlag = false;

		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		nextId++;
		chkFieldInteger("115", 115);
		chkUpdateMulti("116", 2);  //change 2nd record 
		nextId=44;
		//change 1st record TODO: should fail
		chkFieldUpdateFail("116", "duplicate-unique-value");
	}	


	// --
	

	@Before
	public void init() {
		super.init();
	}
}
