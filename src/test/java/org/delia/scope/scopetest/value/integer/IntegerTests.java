package org.delia.scope.scopetest.value.integer;

import org.junit.Before;
import org.junit.Test;

public class IntegerTests extends IntegerTestBase {

	@Test
	public void test1Scalar() {
		createScalarType("int", "");
		
		//string
		chkInteger("114", 114);
		chkInteger("0", 0);
		chkInteger("null", null);
		
		//X
		chkInteger("114", "X",  114);
		chkInteger("0", "X",  0);
		chkInteger("null", "X",  null);
		
		//X2
		chkInteger("114", "X2",  114);
		chkInteger("0", "X2",  0);
		chkInteger("null", "X2",  null);
	}
	
	@Test
	public void test2ScalarRulePass() {
		createScalarType("int", "< 1000");
		
		//TODO: run all rules types - pos and neg tests
		
		//string
		chkInteger("114", 114);
		chkInteger("0", 0);
		chkInteger("null", null);
		
		//X
		chkInteger("114", "X", 114);
		chkInteger("0", "X", 0);
		chkInteger("null", "X", null);
		
		//X2
		chkInteger("114", "X2", 114);
		chkInteger("0", "X2", 0);
		chkInteger("null", "X2", null);
	}
	@Test
	public void test2ScalarRuleFail() {
		createScalarType("int", "< 10");
		
		//TODO: run all rules types - pos and neg tests
		
		//primitive types - can't have rules
		
		//X
		chkInteger("1", "X",  1);
		chkIntegerFail("114", "X",  "rule-compare");
		chkInteger("0", "X",  0);
		chkInteger("null", "X",  null);
		
		//X2
		chkInteger("1", "X2",  1);
		chkIntegerFail("114", "X2",  "rule-compare");
		chkInteger("0", "X2",  0);
		chkInteger("null", "X2",  null);
	}
	
	@Test
	public void test4Struct() {
		createStructType("int", "");
		
		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		chkFieldInteger("0", 0);
		chkFieldInsertParseFail("null");

		//X2
		typeNameToUse = "C2";
		chkFieldInteger("114", 114);
		chkFieldInteger("0", 0);
		chkFieldInsertParseFail("null");
	}
	@Test
	public void test4StructLet() {
		createStructType("int", "");
		
		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);

		//field assign to scalar
		//TODO: a1.field1.subfield1
		//TODO: a1.field1.max()
		log("......and...");
//		chkLetFieldOrFnInt("a1.field1", 114);
		
		//TODO: this is strange. should res.dval hold the value or res.qresp.dvallist?
		//right now we're using qresp.
		chkLetFieldOrFnInt("a1.field1.max()", 114);
	}
	
	@Test
	public void test4StructRulePass() {
		createStructType("int", "field1 < 10");
		
		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail("114", "rule-compare");
		chkFieldInsertParseFail("null");

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail("114", "rule-compare");
		chkFieldInsertParseFail("null");
	}
	
	@Test
	public void test5LetScalar() {
		createScalarType("int", "");
		
		//string
		chkInteger("114", 114);
		chkInteger("0", 0);
		chkInteger("null", null);
		
		//X
		chkInteger("114", "X",  114);
		chkInteger("0", "X2", 0);
		chkInteger("null", "X",  null);
		
		//X2
		chkInteger("114", "X2",  114);
		chkInteger("0", "X2", 0);
		chkInteger("null", "X2",  null);
		
		//references
		chkIntegerRef("a1", "", 114);
		chkIntegerRef("a3", "", null);
		
		//references - X
		chkIntegerRef("a4", "", 114);
		chkIntegerRef("a6", "", null);
		chkIntegerRef("a4", "X", 114);
		chkIntegerRef("a6", "X", null);
		
		//references - X2
		chkIntegerRef("a7", "", 114);
		chkIntegerRef("a9", "", null);
		chkIntegerRef("a7", "X2", 114);
		chkIntegerRef("a9", "X2", null);
	}

	@Test
	public void test6Insert() {
		createScalarType("int", "");
		createStructType("int", "");
		chkInteger("66", 66);
		chkInteger("67", "X",  67);
		chkInteger("68", "X2",  68);

		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		chkFieldInsertParseFail("null");
		chkFieldInteger("a1", 66);
		chkFieldInteger("a2", 67);
		chkFieldInteger("a3", 68);
	}	
	
	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("int", "");
		createStructType("int", "");
		chkInteger("66", 66);
		chkInteger("67", "X",  67);
		chkInteger("68", "X2",  68);

		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		chkUpdateFieldInteger("8", 8);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("int", "");
		createStructType("int", "");
		chkInteger("66", 66);
		chkInteger("67", "X",  67);
		chkInteger("68", "X2",  68);

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
		createStructType("int", "");
		chkInteger("66", 66);
		chkInteger("67", "X",  67);
		chkInteger("68", "X2",  68);

		//C
		typeNameToUse = "C";
		chkFieldInteger("114", 114);
		nextId++;
		chkFieldInteger("'1142'", 1142);
		
		//check queries
		chkQueryInteger("[44]", 114);
		chkQueryInteger("[45]", 1142);
		chkQueryInteger("[field1==114]", 114);
		chkQueryString2("[field1 > 0]", 114, 1142);
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	
	
	// --
	

	@Before
	public void init() {
		super.init();
	}
}
