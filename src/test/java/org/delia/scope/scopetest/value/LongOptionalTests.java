package org.delia.scope.scopetest.value;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class LongOptionalTests extends TypeLayerTestBase {

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
		createStructType("long optional", "");
		beginSession();

		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		chkFieldLong("0", 0L);
		chkFieldLong("null", null);

		//X2
		typeNameToUse = "C2";
		chkFieldLong("114", 114L);
		chkFieldLong("0", 0L);
		chkFieldLong("null", null);
	}
	@Test
	public void test4StructRulePass() {
		createStructType("long optional", "field1 < 10");
		beginSession();

		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail("114", "rule-compare");
		chkFieldLong("null", null);

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail("114", "rule-compare");
		chkFieldLong("null", null);
	}
	
	@Test
	public void test5LetScalar() {
		//scalar let values are optional, so already tested
	}

	@Test
	public void test6Insert() {
		createScalarType("long", "");
		createStructType("long optional", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		chkFieldLong("null", null);
		chkFieldLong("a1", 66L);
		chkFieldLong("a2", 67L);
		chkFieldLong("a3", 68L);
	}	
	private void do3Lets() {
		chkLongInt("66", 66L);
		chkLongInt("67", "X",  67L);
		chkLongInt("68", "X2",  68L);
		chkLongInt("null", "X2",  null);
	}

	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("long", "");
		createStructType("long optional", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		chkUpdateFieldLong("8", 8L);
		chkUpdateFieldLong("null", null);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("long", "");
		createStructType("long optional", "");
		beginSession();

		do3Lets();

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
		createStructType("long optional", "");
		beginSession();

		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldLong("114", 114L);
		nextId++;
		chkFieldLong("null", null);
		
		//check queries
		chkQueryLong("[44]", 114L);
		chkQueryLong("[45]", null);
		chkQueryLong("[field1==114]", 114L);
		chkQueryLong("[field1 == null]", null);
		chkQueryLong("[field1 > 0]", 114L);
		//TODO also test 114==field1 and 0 < field1
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	
	

	// --
//	private Long actualLongVal = new Long(Integer.MAX_VALUE) + 10;


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
		if (expected == null) {
			assertEquals(null, dval.asStruct().getField("field1"));
		} else {
			assertEquals(expected.longValue(), dval.asStruct().getField("field1").asLong());
		}
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

}
