package org.delia.scope.scopetest.value.string;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class StringTests extends StringTestBase {

	@Test
	public void test1Scalar() {
		createScalarType("string", "");
		beginSession();
		
		//string
		chkString("'bob'", "bob");
		chkString("''", "");
		chkString("null", null);
		
		//X
		chkString("'bob'", "X",  "bob");
		chkString("''", "X",  "");
		chkString("null", "X",  null);
		
		//X2
		chkString("'bob'", "X2",  "bob");
		chkString("''", "X2",  "");
		chkString("null", "X2",  null);
	}
	
	@Test
	public void test2ScalarRulePass() {
		createScalarType("string", "maxlen(4)");
		beginSession();
		
		//TODO: run all rules types - pos and neg tests
		
		//string
		chkString("'bob'", "bob");
		chkString("''", "");
		chkString("null", null);
		
		//X
		chkString("'bob'", "X",  "bob");
		chkString("''", "X",  "");
		chkString("null", "X",  null);
		
		//X2
		chkString("'bob'", "X2",  "bob");
		chkString("''", "X2",  "");
		chkString("null", "X2",  null);
	}
	@Test
	public void test2ScalarRuleFail() {
		createScalarType("string", "maxlen(2)");
		beginSession();
		
		//TODO: run all rules types - pos and neg tests
		
		//primitive types - can't have rules
		
		//X
		chkString("'bo'", "X",  "bo");
		chkStringFail("'bob'", "X",  "rule-maxlen");
		chkString("''", "X",  "");
		chkString("null", "X",  null);
		
		//X2
		chkString("'bo'", "X2",  "bo");
		chkStringFail("'bob'", "X2",  "rule-maxlen");
		chkString("''", "X2",  "");
		chkString("null", "X2",  null);
	}
	
	@Test
	public void test4Struct() {
		createStructType("string", "");
		beginSession();
		
		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		chkFieldString("''", "");
		chkFieldInsertParseFail("null");

		//X2
		typeNameToUse = "C2";
		chkFieldString("'bob'", "bob");
		chkFieldString("''", "");
		chkFieldInsertParseFail("null");
	}
	@Test
	public void test4StructLet() {
		createStructType("string", "");
		beginSession();

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");

		//field assign to scalar
		//TODO: a1.field1.subfield1
		//TODO: a1.field1.max()
		log("......and...");
		chkLetFieldOrFnString("a1.field1", "bob");
	}
	
	@Test
	public void test4StructRulePass() {
		createStructType("string", "maxlen(2)");
		beginSession();

		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail("'bob'", "rule-maxlen");
		chkFieldInsertFail("''", "rule-maxlen");
		chkFieldInsertParseFail("null");

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail("'bob'", "rule-maxlen");
		chkFieldInsertFail("''", "rule-maxlen");
		chkFieldInsertParseFail("null");
	}
	
	@Test
	public void test5LetScalar() {
		createScalarType("string", "");
		beginSession();

		//string
		chkString("'bob'", "bob");
		chkString("''", "");
		chkString("null", null);
		
		//X
		chkString("'bob'", "X",  "bob");
		chkString("''", "X",  "");
		chkString("null", "X",  null);
		
		//X2
		chkString("'bob'", "X2",  "bob");
		chkString("''", "X2",  "");
		chkString("null", "X2",  null);
		
		//references
		chkStringRef("a1", "", "bob");
		chkStringRef("a3", "", null);
		
		//references - X
		chkStringRef("a4", "", "bob");
		chkStringRef("a6", "", null);
		chkStringRef("a4", "X", "bob");
		chkStringRef("a6", "X", null);
		
		//references - X2
		chkStringRef("a7", "", "bob");
		chkStringRef("a9", "", null);
		chkStringRef("a7", "X2", "bob");
		chkStringRef("a9", "X2", null);
	}

	@Test
	public void test6Insert() {
		createScalarType("string", "");
		createStructType("string", "");
		beginSession();

		chkString("'sue'", "sue");
		chkString("'sue2'", "X",  "sue2");
		chkString("'sue3'", "X2",  "sue3");

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		chkFieldString("''", "");
		chkFieldInsertParseFail("null");
		chkFieldString("a1", "sue");
		chkFieldString("a2", "sue2");
		chkFieldString("a3", "sue3");
	}	
	
	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("string", "");
		createStructType("string", "");
		beginSession();

		chkString("'sue'", "sue");
		chkString("'sue2'", "X",  "sue2");
		chkString("'sue3'", "X2",  "sue3");

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		chkUpdateFieldString("'art'", "art");
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("string", "");
		createStructType("string", "");
		beginSession();

		chkString("'sue'", "sue");
		chkString("'sue2'", "X",  "sue2");
		chkString("'sue3'", "X2",  "sue3");

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
		createStructType("string", "");
		beginSession();

		chkString("'sue'", "sue");
		chkString("'sue2'", "X",  "sue2");
		chkString("'sue3'", "X2",  "sue3");

		//C
		typeNameToUse = "C";
		chkFieldString("'bob'", "bob");
		nextId++;
		chkFieldString("'bob2'", "bob2");
		
		//check queries
		chkQueryString("[44]", "bob");
		chkQueryString("[45]", "bob2");
		chkQueryString("[field1=='bob']", "bob");
		chkQueryString2("[field1>'a']", "bob", "bob2");
		//TODO fix chkQuery2("[field1 != null]", "bob", "bob2");
	}	

	// --
	

	@Before
	public void init() {
		super.init();
	}
}
