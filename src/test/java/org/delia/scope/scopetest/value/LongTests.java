package org.delia.scope.scopetest.value;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class LongTests extends TypeLayerTestBase {

	@Test
	public void test1Scalar() {
		createScalarType("long", "");
		
		//string
		chkLong(actualLongVal.toString(), actualLongVal);
		chkLongInt("114", 114L); //implicity seen as int since we didn't provide a type
		chkLongInt("0", 0L);
		chkLong("null", null);
		
		//X
		chkLong("114", "X",  114L);
		chkLong("0", "X",  0L);
		chkLong("null", "X",  null);
		
		//X2
		chkLong("114", "X2",  114L);
		chkLong("0", "X2",  0L);
		chkLong("null", "X2",  null);
	}
	
	@Test
	public void test2ScalarRulePass() {
		createScalarType("long", "< 10000");
		
		//TODO: run all rules types - pos and neg tests
		
		//string
		chkLong(actualLongVal.toString(), actualLongVal);
		chkLongInt("114", 114L); //implicity seen as int since we didn't provide a type
		chkLongInt("0", 0L);
		chkLong("null", null);
		
		//X
		chkLong("114", "X", 114L);
		chkLong("0", "X", 0L);
		chkLong("null", "X", null);
		
		//X2
		chkLong("114", "X2", 114L);
		chkLong("0", "X2", 0L);
		chkLong("null", "X2", null);
	}
	@Test
	public void test2ScalarRuleFail() {
		createScalarType("long", "< 10");
		
		//TODO: run all rules types - pos and neg tests
		
		//primitive types - can't have rules
		
		//X
		chkLong("1", "X",  1L);
		chkLongFail("114", "X",  "rule-compare");
		chkLong("0", "X",  0L);
		chkLong("null", "X",  null);
		
		//X2
		chkLong("1", "X2",  1L);
		chkLongFail("114", "X2",  "rule-compare");
		chkLong("0", "X2",  0L);
		chkLong("null", "X2",  null);
	}
	
	@Test
	public void test4Struct() {
		createStructType("long", "");
		
		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		chkFieldLong("0", 0L);
		chkFieldInsertParseFail("null");

		//X2
		typeNameToUse = "C2";
		chkFieldLong("114", 114L);
		chkFieldLong("0", 0L);
		chkFieldInsertParseFail("null");
	}
	@Test
	public void test4StructLet() {
		createStructType("long", "");
		
		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);

		//field assign to scalar
		//TODO: a1.field1.subfield1
		//TODO: a1.field1.max()
		log("......and...");
		chkLetFieldOrFnLong("a1.field1", 114L);
	}
	@Test
	public void test4StructRulePass() {
		createStructType("long", "field1 < 10");
		
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
		createScalarType("long", "");
		
		//string
		chkLongInt("114", 114L);
		chkLongInt("0", 0L);
		chkLong("null", null);
		
		//X
		chkLong("114", "X",  114L);
		chkLong("0", "X2", 0L);
		chkLong("null", "X",  null);
		
		//X2
		chkLong("114", "X2",  114L);
		chkLong("0", "X2", 0L);
		chkLong("null", "X2",  null);
		
		//references
		chkLongRef("a1", "", 114L);
		chkLongRef("a3", "", null);
		
		//references - X
		chkLongRef("a4", "", 114L);
		chkLongRef("a6", "", null);
		chkLongRef("a4", "X", 114L);
		chkLongRef("a6", "X", null);
		
		//references - X2
		chkLongRef("a7", "", 114L);
		chkLongRef("a9", "", null);
		chkLongRef("a7", "X2", 114L);
		chkLongRef("a9", "X2", null);
	}

	@Test
	public void test6Insert() {
		createScalarType("long", "");
		createStructType("long", "");
		chkLongInt("66", 66L);
		chkLongInt("67", "X",  67L);
		chkLongInt("68", "X2",  68L);

		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		chkFieldInsertParseFail("null");
		chkFieldLong("a1", 66L);
		chkFieldLong("a2", 67L);
		chkFieldLong("a3", 68L);
	}	
	@Test
	public void test6Insert2() {
		createScalarType("long", "");
		createStructType("long", "");
		chkLong(actualLongVal.toString(), actualLongVal);

		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		chkFieldInsertParseFail("null");
		chkFieldLong("a1", actualLongVal);
	}	
	
	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("long", "");
		createStructType("long", "");
		chkLongInt("66", 66L);
		chkLongInt("67", "X",  67L);
		chkLongInt("68", "X2",  68L);

		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		chkUpdateFieldLong("8", 8L);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("long", "");
		createStructType("long", "");
		chkLongInt("66", 66L);
		chkLongInt("67", "X",  67L);
		chkLongInt("68", "X2",  68L);

		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		chkDeleteRow();
	}	
	
	@Test
	public void test9Query() {
		addIdFlag = true;
		deleteBeforeInsertFlag = false;
		createScalarType("long", "");
		createStructType("long", "");
		chkLongInt("66", 66L);
		chkLongInt("67", "X",  67L);
		chkLongInt("68", "X2",  68L);

		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		nextId++;
		chkFieldLong("'1142'", 1142L);
		
		//check queries
		chkQueryLong("[44]", 114L);
		chkQueryLong("[45]", 1142L);
		chkQueryLong("[field1==114]", 114L);
		chkQueryString2("[field1 > 0]", 114L, 1142L);
		//TODO also test 114==field1 and 0 < field1
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	
	
	@Test
	public void test9Query2() {
		addIdFlag = true;
		deleteBeforeInsertFlag = false;
		createScalarType("long", "");
		createStructType("long", "");
		chkLong(actualLongVal.toString(), actualLongVal);

		//C
		typeNameToUse = "C";
		chkFieldLong(actualLongVal.toString(), actualLongVal);
		
		//check queries
		chkQueryLong("[44]", actualLongVal);
		chkQueryLong(String.format("[field1==%d]", actualLongVal), actualLongVal);
		chkQueryLong("[field1 > 0]", actualLongVal);
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	

	// --
	private Long actualLongVal = new Long(Integer.MAX_VALUE) + 10;


	@Before
	public void init() {
		super.init();
	}

	protected void chkLong(String valStr, Long expected) {
		chkLong(valStr, "", expected);
	}
	protected void chkLong(String valStr, String varType, Long expected) {
		DValue dval = chkOneField(valStr, varType, "long", expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			assertEquals(expected.longValue(), dval.asLong());
		}
	}
	protected void chkLongInt(String valStr, Long expected) {
		chkLongInt(valStr, "", expected);
	}
	protected void chkLongInt(String valStr, String varType, Long expected) {
		DValue dval = chkOneField(valStr, varType, "int", expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			assertEquals(expected.longValue(), dval.asLong());
		}
	}
	protected void chkLongRef(String valStr, String varType, Long expected) {
		DValue dval = chkOneFieldRef(valStr, varType, expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			assertEquals(expected.longValue(), dval.asLong());
		}
	}
	protected void chkLongFail(String valStr, String varType, String expectedErrId) {
		chkOneFieldFail(valStr, varType, "long", expectedErrId);
	}
	protected void chkFieldLong(String str, Long expected) {
		DValue dval = chkOneField(str);
		chkFieldValue(dval, expected);
	}
	private void chkFieldValue(DValue dval, Long expected) {
		assertEquals(expected.longValue(), dval.asStruct().getField("field1").asLong());
	}

	protected void chkUpdateFieldLong(String str, Long expected) {
		DValue dval = chkUpdateOneField(str);
		chkFieldValue(dval, expected);
	}
	protected void chkQueryLong(String string, Long string2) {
		DValue dval = doChkQuery(string);
		chkFieldValue(dval, string2);
	}
	protected void chkQueryString2(String string, Long expected, Long expected2) {
		QueryResponse qresp = doChkQuery2(string);
		DValue dval = qresp.dvalList.get(0);
		chkFieldValue(dval, expected);
		dval = qresp.dvalList.get(1);
		chkFieldValue(dval, expected2);
	}
	protected void chkLetFieldOrFnLong(String valStr, Long expected) {
		DValue dval = doChkLetFieldOrFn(valStr, BuiltInTypes.LONG_SHAPE.name());
		assertEquals(expected.longValue(), dval.asLong());
	}

}
