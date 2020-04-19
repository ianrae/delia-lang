package org.delia.scope.scopetest.value.string;

import org.junit.Before;
import org.junit.Test;

public class StringOptionalTests extends StringTestBase {

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
		createStructType("string optional", "");
		
		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		chkFieldString("''", "");
		chkFieldString("null", null);

		//X2
		typeNameToUse = "C2";
		chkFieldString("'bob'", "bob");
		chkFieldString("''", "");
		chkFieldString("null", null);
	}
	@Test
	public void test4StructRulePass() {
		createStructType("string optional", "field1.maxlen(2)");
		
		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail("'bob'", "rule-maxlen");
		chkFieldString("''", "");
		chkFieldString("null", null);

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail("'bob'", "rule-maxlen");
		chkFieldString("''", "");
		chkFieldString("null", null);
	}
	
	@Test
	public void test5LetScalar() {
		//scalar let values are optional, so already tested
	}

	@Test
	public void test6Insert() {
		createScalarType("string", "");
		createStructType("string optional", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		chkFieldString("''", "");
		chkFieldString("null", null);
		chkFieldString("a1", "sue");
		chkFieldString("a2", "sue2");
		chkFieldString("a3", "sue3");
		chkFieldString("a4", null);
	}	
	
	private void do3Lets() {
		chkString("'sue'", "sue");
		chkString("'sue2'", "X",  "sue2");
		chkString("'sue3'", "X2",  "sue3");
		chkString("null", "X2",  null);
	}

	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("string", "");
		createStructType("string optional", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		chkUpdateFieldString("'art'", "art");
		chkUpdateFieldString("null", null);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("string", "");
		createStructType("string optional", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		chkDeleteRow();
	}	
	
	@Test
	public void test9Query() {
		addIdFlag = true;
		deleteBeforeInsertFlag = false;
		createScalarType("string", "");
		createStructType("string optional", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		nextId++;
		chkFieldString("null", null);
		
		//check queries
		chkQueryString("[44]", "bob");
		chkQueryString("[45]", null);
		chkQueryString("[field1=='bob']", "bob");
		chkQueryString("[field1 == null]", null);
		chkQueryString("[field1>'a']", "bob");
		//TODO fix chkQuery2("[field1 != null]", "bob", "bob2");
	}	

	// --
	

	@Before
	public void init() {
		super.init();
	}

}
