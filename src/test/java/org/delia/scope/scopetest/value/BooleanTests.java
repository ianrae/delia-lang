package org.delia.scope.scopetest.value;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class BooleanTests extends TypeLayerTestBase {

	@Test
	public void test1Scalar() {
		createScalarType("boolean", "");
		beginSession();

		//string
		chkBoolean(actualBooleanVal.toString(), actualBooleanVal);
		chkBoolean("true", true);
		chkBoolean("false", false);
		chkBoolean("null", null);
		
		//X
		chkBoolean("true", "X",  true);
		chkBoolean("false", "X",  false);
		chkBoolean("null", "X",  null);
		
		//X2
		chkBoolean("true", "X2",  true);
		chkBoolean("false", "X2",  false);
		chkBoolean("null", "X2",  null);
	}
	
	@Test
	public void test2ScalarRulePass() {
		createScalarType("boolean", "== true");
		beginSession();

		//TODO: run all rules types - pos and neg tests
		
		//string
		chkBoolean(actualBooleanVal.toString(), actualBooleanVal);
		chkBoolean("true", true);
		chkBoolean("false", false);
		chkBoolean("null", null);
		
		//X
		chkBoolean("true", "X",  true);
		chkBoolean("null", "X", null);
		
		//X2
		chkBoolean("true", "X2",  true);
		chkBoolean("null", "X2", null);
	}
	@Test
	public void test2ScalarRuleFail() {
		createScalarType("boolean", "== true");
		beginSession();

		//TODO: run all rules types - pos and neg tests
		
		//primitive types - can't have rules
		
		//X
		chkBoolean("true", "X",  true);
		chkBooleanFail("false", "X",  "rule-compare");
		chkBoolean("null", "X",  null);
		
		//X2
		chkBoolean("true", "X2",  true);
		chkBooleanFail("false", "X2",  "rule-compare");
		chkBoolean("null", "X2",  null);
	}
	
	@Test
	public void test4Struct() {
		createStructType("boolean", "");
		beginSession();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		chkFieldBoolean("false", false);
		chkFieldInsertParseFail("null");

		//X2
		typeNameToUse = "C2";
		chkFieldBoolean("true", true);
		chkFieldBoolean("false", false);
		chkFieldInsertParseFail("null");
	}
	@Test
	public void test4StructLet() {
		createStructType("boolean", "");
		beginSession();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);

		//field assign to scalar
		//TODO: a1.field1.subfield1
		//TODO: a1.field1.max()
		log("......and...");
		chkLetFieldOrFnBoolean("a1.field1", true);
	}
	@Test
	public void test4StructRulePass() {
		createStructType("boolean", "field1 == true");
		beginSession();

		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail("false", "rule-compare");
		chkFieldInsertParseFail("null");

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail("false", "rule-compare");
		chkFieldInsertParseFail("null");
	}
	
	@Test
	public void test5LetScalar() {
		createScalarType("boolean", "");
		beginSession();

		//boolean
		chkBoolean("true", true);
		chkBoolean("false", false);
		chkBoolean("null", null);
		
		//X
		chkBoolean("true", "X",  true);
		chkBoolean("false", "X", false);
		chkBoolean("null", "X",  null);
		
		//X2
		chkBoolean("true", "X2",  true);
		chkBoolean("false", "X2", false);
		chkBoolean("null", "X2",  null);
		
		//references
		chkBooleanRef("a1", "", true);
		chkBooleanRef("a3", "", null);
		
		//references - X
		chkBooleanRef("a4", "", true);
		chkBooleanRef("a6", "", null);
		
		//references - X2
		chkBooleanRef("a7", "", true);
		chkBooleanRef("a9", "", null);
		chkBooleanRef("a7", "X2", true);
		chkBooleanRef("a9", "X2", null);
	}

	@Test
	public void test6Insert() {
		createScalarType("boolean", "");
		createStructType("boolean", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		chkFieldInsertParseFail("null");
		chkFieldBoolean("a1", true);
		chkFieldBoolean("a2", true);
		chkFieldBoolean("a3", true);
	}	
	private void do3Lets() {
		chkBoolean("true", true);
		chkBoolean("true", "X",  true);
		chkBoolean("true", "X",  true);
	}

	@Test
	public void test6Insert2() {
		createScalarType("boolean", "");
		createStructType("boolean", "");
		beginSession();

		chkBoolean(actualBooleanVal.toString(), actualBooleanVal);

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		chkFieldInsertParseFail("null");
		chkFieldBoolean("a1", actualBooleanVal);
	}	
	
	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("boolean", "");
		createStructType("boolean", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		chkUpdateFieldBoolean("false" , false);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("boolean", "");
		createStructType("boolean", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		chkDeleteRow();
	}	
	
	@Test
	public void test9Query() {
		addIdFlag = true;
		deleteBeforeInsertFlag = false;
		createScalarType("boolean", "");
		createStructType("boolean", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		nextId++;
		chkFieldBoolean("false", false);
		
		//check queries
		chkQueryBoolean("[44]", true);
		chkQueryBoolean("[45]", false);
		chkQueryBoolean("[field1==true]", true);
//		chkQueryString2("[field1 > 0.0]", 114.0, 1142.0);
		//TODO also test 114==field1 and 0 < field1
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	

	// --
	private Boolean actualBooleanVal = true;


	@Before
	public void init() {
		super.init();
	}

	protected void chkBoolean(String valStr, Boolean expected) {
		chkBoolean(valStr, "", expected);
	}
	protected void chkBoolean(String valStr, String varType, Boolean expected) {
		DValue dval = chkOneField(valStr, varType, "boolean", expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			chkScalarValue(expected, dval);
		}
	}
	private void chkScalarValue(Boolean b, DValue dval) {
		assertEquals(b.booleanValue(), dval.asBoolean());
	}
//	protected void chkBooleanInt(String valStr, Boolean expected) {
//		chkBooleanInt(valStr, "", expected);
//	}
//	protected void chkBooleanInt(String valStr, String varType, Boolean expected) {
//		DValue dval = chkOneField(valStr, varType, "int", expected);
//		if (expected == null) {
//			assertEquals(null, dval);
//		} else {
//			chkScalarValue(expected, dval);
//		}
//	}
	protected void chkBooleanRef(String valStr, String varType, Boolean expected) {
		DValue dval = chkOneFieldRef(valStr, varType, expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			chkScalarValue(expected, dval);
		}
	}
	protected void chkBooleanFail(String valStr, String varType, String expectedErrId) {
		chkOneFieldFail(valStr, varType, "boolean", expectedErrId);
	}
	protected void chkFieldBoolean(String str, Boolean expected) {
		DValue dval = chkOneField(str);
		chkFieldValue(dval, expected);
	}
	private void chkFieldValue(DValue dval, Boolean expected) {
		assertEquals(expected.booleanValue(), dval.asStruct().getField("field1").asBoolean());
	}

	protected void chkUpdateFieldBoolean(String str, Boolean expected) {
		DValue dval = chkUpdateOneField(str);
		chkFieldValue(dval, expected);
	}
	protected void chkQueryBoolean(String string, Boolean string2) {
		DValue dval = doChkQuery(string);
		chkFieldValue(dval, string2);
	}
	protected void chkQueryString2(String string, Boolean expected, Boolean expected2) {
		QueryResponse qresp = doChkQuery2(string);
		DValue dval = qresp.dvalList.get(0);
		chkFieldValue(dval, expected);
		dval = qresp.dvalList.get(1);
		chkFieldValue(dval, expected2);
	}
	protected void chkLetFieldOrFnBoolean(String valStr, Boolean expected) {
		DValue dval = doChkLetFieldOrFn(valStr, BuiltInTypes.BOOLEAN_SHAPE.name());
		assertEquals(expected, dval.asBoolean());
	}

}
