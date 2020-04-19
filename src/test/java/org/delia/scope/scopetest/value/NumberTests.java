package org.delia.scope.scopetest.value;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class NumberTests extends TypeLayerTestBase {

	@Test
	public void test1Scalar() {
		createScalarType("number", "");
		
		//string
		chkNumber(actualNumberVal.toString(), actualNumberVal);
		chkNumber("114.0", 114.0);
		chkNumber("0.0", 0.0);
		chkNumberInt("114", 114.0); //implicity seen as int since we didn't provide a type
		chkNumberInt("0", 0.0);
		chkNumber("null", null);
		
		//X
		chkNumber("114.0", "X",  114.0);
		chkNumber("0.0", "X",  0.0);
		chkNumber("null", "X",  null);
		
		//X2
		chkNumber("114.0", "X2",  114.0);
		chkNumber("0.0", "X2",  0.0);
		chkNumber("null", "X2",  null);
	}
	
	@Test
	public void test2ScalarRulePass() {
		createScalarType("number", "< 10000");
		
		//TODO: run all rules types - pos and neg tests
		
		//string
		chkNumber(actualNumberVal.toString(), actualNumberVal);
		chkNumber("114.0", 114.0);
		chkNumber("0.0", 0.0);
		chkNumberInt("114", 114.0); //implicity seen as int since we didn't provide a type
		chkNumberInt("0", 0.0);
		chkNumber("null", null);
		
		//X
		chkNumber("114.0", "X", 114.0);
		chkNumber("0.0", "X", 0.0);
		chkNumber("null", "X", null);
		
		//X2
		chkNumber("114.0", "X2", 114.0);
		chkNumber("0.0", "X2", 0.0);
		chkNumber("null", "X2", null);
	}
	@Test
	public void test2ScalarRuleFail() {
		createScalarType("number", "< 10");
		
		//TODO: run all rules types - pos and neg tests
		
		//primitive types - can't have rules
		
		//X
		chkNumber("1.0", "X",  1.0);
		chkNumberFail("114.0", "X",  "rule-compare");
		chkNumber("0.0", "X",  0.0);
		chkNumber("null", "X",  null);
		
		//X2
		chkNumber("1.0", "X2",  1.0);
		chkNumberFail("114.0", "X2",  "rule-compare");
		chkNumber("0.0", "X2",  0.0);
		chkNumber("null", "X2",  null);
	}
	@Test
	public void test2ScalarRuleFail2() {
		createScalarType("number", "< 10.0");
		
		//TODO: run all rules types - pos and neg tests
		
		//primitive types - can't have rules
		
		//X
		chkNumber("1.0", "X",  1.0);
		chkNumberFail("114.0", "X",  "rule-compare");
		chkNumber("0.0", "X",  0.0);
		chkNumber("null", "X",  null);
		
		//X2
		chkNumber("1.0", "X2",  1.0);
		chkNumberFail("114.0", "X2",  "rule-compare");
		chkNumber("0.0", "X2",  0.0);
		chkNumber("null", "X2",  null);
	}
	
	@Test
	public void test4Struct() {
		createStructType("number", "");
		
		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		chkFieldNumber("0.0", 0.0);
		chkFieldInsertParseFail("null");

		//X2
		typeNameToUse = "C2";
		chkFieldNumber("114.0", 114.0);
		chkFieldNumber("0.0", 0.0);
		chkFieldInsertParseFail("null");
	}
	@Test
	public void test4StructLet() {
		createStructType("number", "");
		
		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);

		//field assign to scalar
		//TODO: a1.field1.subfield1
		//TODO: a1.field1.max()
		log("......and...");
		chkLetFieldOrFnNumber("a1.field1", 114.0);
	}
	@Test
	public void test4StructRulePass() {
		createStructType("number", "field1 < 10");
		
		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail("114.0", "rule-compare");
		chkFieldInsertParseFail("null");

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail("114.0", "rule-compare");
		chkFieldInsertParseFail("null");
	}
	
	@Test
	public void test5LetScalar() {
		createScalarType("number", "");
		
		//string
		chkNumber("114.0", 114.0);
		chkNumber("0.0", 0.0);
		chkNumberInt("114", 114.0); //implicity seen as int since we didn't provide a type
		chkNumberInt("0", 0.0);
		chkNumber("null", null);
		
		//X
		chkNumber("114.0", "X",  114.0);
		chkNumber("0.0", "X2", 0.0);
		chkNumber("null", "X",  null);
		
		//X2
		chkNumber("114.0", "X2",  114.0);
		chkNumber("0.0", "X2", 0.0);
		chkNumber("null", "X2",  null);
		
		//references
		chkNumberRef("a1", "", 114.0);
		chkNumberRef("a5", "", null);
		
		//references - X
		chkNumberRef("a6", "", 114.0);
		chkNumberRef("a8", "", null);
		chkNumberRef("a6", "X", 114.0);
		chkNumberRef("a8", "X", null);
		
		//references - X2
		chkNumberRef("a9", "", 114.0);
		chkNumberRef("a11", "", null);
		chkNumberRef("a9", "X2", 114.0);
		chkNumberRef("a11", "X2", null);
	}

	@Test
	public void test6Insert() {
		createScalarType("number", "");
		createStructType("number", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		chkFieldInsertParseFail("null");
		chkFieldNumber("a1", 66.0);
		chkFieldNumber("a2", 67.0);
		chkFieldNumber("a3", 68.0);
	}	
	private void do3Lets() {
		chkNumber("66.0", 66.0);
		chkNumber("67.0", "X",  67.0);
		chkNumber("68.0", "X2",  68.0);
	}

	@Test
	public void test6Insert2() {
		createScalarType("number", "");
		createStructType("number", "");
		chkNumber(actualNumberVal.toString(), actualNumberVal);

		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		chkFieldInsertParseFail("null");
		chkFieldNumber("a1", actualNumberVal);
	}	
	
	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("number", "");
		createStructType("number", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		chkUpdateFieldNumber("8.0", 8.0);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("number", "");
		createStructType("number", "");
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
		createStructType("number", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldNumber("114.0", 114.0);
		nextId++;
		chkFieldNumber("'1142.0'", 1142.0);
		
		//check queries
		chkQueryNumber("[44]", 114.0);
		chkQueryNumber("[45]", 1142.0);
		chkQueryNumber("[field1==114.0]", 114.0);
		chkQueryString2("[field1 > 0.0]", 114.0, 1142.0);
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
	private void chkFieldValue(DValue dval, Double expected) {
		assertEquals(expected.doubleValue(), dval.asStruct().getField("field1").asNumber(), DELTA);
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
	protected void chkLetFieldOrFnNumber(String valStr, Double expected) {
		DValue dval = doChkLetFieldOrFn(valStr, BuiltInTypes.NUMBER_SHAPE.name());
		assertEquals(expected.doubleValue(), dval.asNumber(), DELTA);
	}

}
