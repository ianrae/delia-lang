package org.delia.scope.scopetest.value;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class NumberOptionalTests extends TypeLayerTestBase {

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
	public void test2ScalarRuleFail2() {
		//scalar let values are optional, so already tested
	}
	
	@Test
	public void test4Struct() {
		createStructType("number optional", "");
		
		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		chkFieldNumber("0.0", 0.0);
		chkFieldNumber("null", null);

		//X2
		typeNameToUse = "C2";
		chkFieldNumber("114.0", 114.0);
		chkFieldNumber("0.0", 0.0);
		chkFieldNumber("null", null);
	}
	@Test
	public void test4StructRulePass() {
		createStructType("number optional", "field1 < 10");
		
		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail("114.0", "rule-compare");
		chkFieldNumber("null", null);

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail("114.0", "rule-compare");
		chkFieldNumber("null", null);
	}
	
	@Test
	public void test5LetScalar() {
		//scalar let values are optional, so already tested
	}

	@Test
	public void test6Insert() {
		createScalarType("number", "");
		createStructType("number optional", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		chkFieldNumber("null", null);
		chkFieldNumber("a1", 66.0);
		chkFieldNumber("a2", 67.0);
		chkFieldNumber("a3", 68.0);
	}	
	private void do3Lets() {
		chkNumber("66.0", 66.0);
		chkNumber("67.0", "X",  67.0);
		chkNumber("68.0", "X2",  68.0);
		chkNumber("null", "X2",  null);
	}

	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("number", "");
		createStructType("number optional", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		chkUpdateFieldNumber("8.0", 8.0);
		chkUpdateFieldNumber("null", null);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("number", "");
		createStructType("number optional", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		chkDeleteRow();
	}	
	
	@Test
	public void test9Query() {
		addIdFlag = true;
		deleteBeforeInsertFlag = false;
		createScalarType("number", "");
		createStructType("number optional", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		nextId++;
		chkFieldNumber("null", null);
		
		//check queries
		chkQueryNumber("[44]", 114.0);
		chkQueryNumber("[45]", null);
		chkQueryNumber("[field1==114.0]", 114.0);
		chkQueryNumber("[field1 == null]", null);
		chkQueryNumber("[field1 > 0]", 114.0);
		//TODO also test 114==field1 and 0 < field1
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	
	
	@Test
	public void test9Query2() {
		addIdFlag = true;
		deleteBeforeInsertFlag = false;
		createScalarType("number", "");
		createStructType("number", "");
		chkNumber(actualNumberVal.toString(), actualNumberVal);

		//C
		typeNameToUse = "C";
		chkFieldNumber(actualNumberVal.toString(), actualNumberVal);
		
		//check queries
		chkQueryNumber("[44]", actualNumberVal);
		chkQueryNumber(String.format("[field1==%s]", actualNumberVal.toString()), actualNumberVal);
		chkQueryNumber("[field1 > 0]", actualNumberVal);
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	

	// --
	private Double actualNumberVal = 12345.678;


	@Before
	public void init() {
		super.init();
	}

	protected void chkNumber(String valStr, Double expected) {
		chkNumber(valStr, "", expected);
	}
	protected void chkNumber(String valStr, String varType, Double expected) {
		DValue dval = chkOneField(valStr, varType, "number", expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			chkScalarValue(expected, dval);
		}
	}
	private void chkScalarValue(Double n, DValue dval) {
		assertEquals(n.doubleValue(), dval.asNumber(), DELTA);
	}
	protected void chkNumberInt(String valStr, Double expected) {
		chkNumberInt(valStr, "", expected);
	}
	protected void chkNumberInt(String valStr, String varType, Double expected) {
		DValue dval = chkOneField(valStr, varType, "int", expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			chkScalarValue(expected, dval);
		}
	}
	protected void chkNumberRef(String valStr, String varType, Double expected) {
		DValue dval = chkOneFieldRef(valStr, varType, expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			chkScalarValue(expected, dval);
		}
	}
	protected void chkNumberFail(String valStr, String varType, String expectedErrId) {
		chkOneFieldFail(valStr, varType, "number", expectedErrId);
	}
	protected void chkFieldNumber(String str, Double expected) {
		DValue dval = chkOneField(str);
		chkFieldValue(dval, expected);
	}
	private void chkFieldValue(DValue dval, Number expected) {
		if (expected == null) {
			assertEquals(null, dval.asStruct().getField("field1"));
		} else {
			assertEquals(expected.doubleValue(), dval.asStruct().getField("field1").asNumber(), DELTA);
		}
	}

	protected void chkUpdateFieldNumber(String str, Double expected) {
		DValue dval = chkUpdateOneField(str);
		chkFieldValue(dval, expected);
	}
	protected void chkQueryNumber(String string, Double string2) {
		DValue dval = doChkQuery(string);
		chkFieldValue(dval, string2);
	}
	protected void chkQueryString2(String string, Double expected, Double expected2) {
		QueryResponse qresp = doChkQuery2(string);
		DValue dval = qresp.dvalList.get(0);
		chkFieldValue(dval, expected);
		dval = qresp.dvalList.get(1);
		chkFieldValue(dval, expected2);
	}

}
