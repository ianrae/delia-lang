package org.delia.scope.scopetest.value;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class BooleanOptionalTests extends TypeLayerTestBase {

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
		createStructType("boolean optional", "");
		beginSession();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		chkFieldBoolean("false", false);
		chkFieldBoolean("null", null);

		//X2
		typeNameToUse = "C2";
		chkFieldBoolean("true", true);
		chkFieldBoolean("false", false);
		chkFieldBoolean("null", null);
	}
	@Test
	public void test4StructRulePass() {
		createStructType("boolean optional", "field1 == true");
		beginSession();

		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail("false", "rule-compare");
		chkFieldBoolean("null", null);

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail("false", "rule-compare");
		chkFieldBoolean("null", null);
	}
	
	@Test
	public void test5LetScalar() {
		//scalar let values are optional, so already tested
	}

	@Test
	public void test6Insert() {
		createScalarType("boolean", "");
		createStructType("boolean optional", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		chkFieldBoolean("null", null);
		chkFieldBoolean("a1", true);
		chkFieldBoolean("a2", true);
		chkFieldBoolean("a3", true);
	}	
	private void do3Lets() {
		chkBoolean("true", true);
		chkBoolean("true", "X",  true);
		chkBoolean("true", "X",  true);
		chkBoolean("null", "X",  null);
	}

	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("boolean", "");
		createStructType("boolean optional", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		chkUpdateFieldBoolean("false" , false);
		chkUpdateFieldBoolean("null" , null);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("boolean", "");
		createStructType("boolean optional", "");
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
		createStructType("boolean optional", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldBoolean("true", true);
		nextId++;
		chkFieldBoolean("null", null);
		
		//check queries
		chkQueryBoolean("[44]", true);
		chkQueryBoolean("[45]", null);
		chkQueryBoolean("[field1==true]", true);
		chkQueryBoolean("[field1 == null]", null);
		//TODO also test 114==field1 and 0 < field1
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	

	// --
//	private Boolean actualBooleanVal = true;


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
		if (expected == null) {
			assertEquals(null, dval.asStruct().getField("field1"));
		} else {
			assertEquals(expected.booleanValue(), dval.asStruct().getField("field1").asBoolean());
		}
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

}
