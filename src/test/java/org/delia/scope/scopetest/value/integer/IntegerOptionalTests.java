package org.delia.scope.scopetest.value.integer;

import org.junit.Before;
import org.junit.Test;

public class IntegerOptionalTests extends IntegerTestBase {

	@Test
	public void test1Scalar() {
		//scalar let values are optional, so already tested
	}
	
	@Test
	public void test2ScalarRulePass() {
		//scalar let values are optional, so already tested
	}
	@Test
	public void test2ScalarRuleFail() {
		//scalar let values are optional, so already tested
	}
	
	@Test
	public void test4Struct() {
		createStructType("int optional", "");
		beginSession();
		
		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		chkFieldInteger("0", 0);
		chkFieldInteger("null", null);

		//X2
		typeNameToUse = "C2";
		chkFieldInteger("114", 114);
		chkFieldInteger("0", 0);
		chkFieldInteger("null", null);
	}
	@Test
	public void test4StructRulePass() {
		createStructType("int optional", "field1 < 10");
		beginSession();
		
		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail("114", "rule-compare");
		chkFieldInteger("null", null);

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail("114", "rule-compare");
		chkFieldInteger("null", null);
	}
	
	@Test
	public void test5LetScalar() {
		//scalar let values are optional, so already tested
	}

	@Test
	public void test6Insert() {
		createScalarType("int", "");
		createStructType("int optional", "");
		beginSession();
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		chkFieldInteger("null", null);
		chkFieldInteger("a1", 66);
		chkFieldInteger("a2", 67);
		chkFieldInteger("a3", 68);
	}	
	
	private void do3Lets() {
		chkInteger("66", 66);
		chkInteger("67", "X",  67);
		chkInteger("68", "X2",  68);
		chkInteger("null", "X2",  null);
	}

	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("int", "");
		createStructType("int optional", "");
		beginSession();
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		chkUpdateFieldInteger("8", 8);
		chkUpdateFieldInteger("null", null);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("int", "");
		createStructType("int optional", "");
		beginSession();
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		chkDeleteRow();
	}	
	
	@Test
	public void test9Query() {
		addIdFlag = true;
		deleteBeforeInsertFlag = false;
		createScalarType("int", "");
		createStructType("int optional", "");
		beginSession();
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		nextId++;
		chkFieldInteger("null", null);
		
		//check queries
		chkQueryInteger("[44]", 114);
		chkQueryInteger("[45]", null);
		chkQueryInteger("[field1==114]", 114);
		chkQueryInteger("[field1 == null]", null);
		chkQueryInteger("[field1 > 0]", 114);
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	

	// --
	

	@Before
	public void init() {
		super.init();
	}


}
