package org.delia.scope.scopetest.value;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.delia.core.DateFormatService;
import org.delia.core.DateFormatServiceImpl;
import org.delia.core.TimeZoneService;
import org.delia.core.TimeZoneServiceImpl;
import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class DateTests extends TypeLayerTestBase {

	@Test
	public void test1Scalar() {
		createScalarType("date", "");
		
		//primitive - implicit not supported. seen as string
		
		//X
		chkDate(actualStr, "X", actualDateVal);
		chkDate("null", "X",  null);
		
		//X2
		chkDate(actualStr, "X2", actualDateVal);
		chkDate("null", "X2",  null);
	}
	
	@Test
	public void test2ScalarRulePass() {
		createScalarType("date", "< date('1999')");
		
		//TODO: run all rules types - pos and neg tests
		
		//X
		chkDate(actualStr, "X", actualDateVal);
		chkDate("null", "X",  null);
		
		//X2
		chkDate(actualStr, "X2", actualDateVal);
		chkDate("null", "X2",  null);
	}
	@Test
	public void test2ScalarRuleFail() {
		createScalarType("date", "> date('1999')");
		
		//TODO: run all rules types - pos and neg tests
		
		//primitive types - can't have rules
		
		//X
		chkDateFail(actualStr, "X", "rule-compare");
		chkDate("null", "X",  null);
		
		//X2
		chkDateFail(actualStr, "X2", "rule-compare");
		chkDate("null", "X2",  null);
	}
	@Test
	public void test4Struct() {
		createStructType("date", "");
		
		//C
		typeNameToUse = "C";
		chkFieldDate(actualStr, actualDateVal);
		chkFieldInsertParseFail("null");

		//X2
		typeNameToUse = "C2";
		chkFieldDate(actualStr, actualDateVal);
		chkFieldInsertParseFail("null");
	}
	@Test
	public void test4StructLet() {
		createStructType("date", "");
		
		//C
		typeNameToUse = "C";
		chkFieldDate(actualStr, actualDateVal);

		//field assign to scalar
		//TODO: a1.field1.subfield1
		//TODO: a1.field1.max()
		log("......and...");
		chkLetFieldOrFnDate("a1.field1", actualDateVal);
	}
	@Test
	public void test4StructRulePass() {
		createStructType("date", "field1 > date('1999')");
		
		//TODO - other rules!!!
		
		//C
		typeNameToUse = "C";
		chkFieldInsertFail(actualStr, "rule-compare");
		chkFieldInsertParseFail("null");

		//C2
		typeNameToUse = "C2";
		chkFieldInsertFail(actualStr, "rule-compare");
		chkFieldInsertParseFail("null");
	}
	
	@Test
	public void test5LetScalar() {
		createScalarType("date", "");
		
		//X
		chkDate(actualStr, "X", actualDateVal);
		chkDate("null", "X",  null);
		
		//X2
		chkDate(actualStr, "X2", actualDateVal);
		chkDate("null", "X2",  null);
		
		
		//references - X
		chkDateRef("a1", "", actualDateVal);
		chkDateRef("a2", "", null);
		chkDateRef("a1", "X", actualDateVal);
		chkDateRef("a2", "X", null);
		
		//references - X2
		chkDateRef("a3", "", actualDateVal);
		chkDateRef("a4", "", null);
		chkDateRef("a3", "X2", actualDateVal);
		chkDateRef("a4", "X2", null);
	}

	@Test
	public void test6Insert() {
		createScalarType("date", "");
		createStructType("date", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldDate(actualStr, actualDateVal);
		chkFieldInsertParseFail("null");
		chkFieldDate("a1", actualDateVal);
		chkFieldDate("a2", actualDateVal2);
	}	
	private void do3Lets() {
//		chkDate(actualStr, actualDateVal);
		chkDate(actualStr, "X", actualDateVal);
		chkDate(actualStr2, "X2", actualDateVal2);
	}
	@Test
	public void test7Update() {
		addIdFlag = true;
		createScalarType("date", "");
		createStructType("date", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldDate(actualStr, actualDateVal);
		chkUpdateFieldDate(actualStr2, actualDateVal2);
	}	

	@Test
	public void test8Delete() {
		addIdFlag = true;
		createScalarType("date", "");
		createStructType("date", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldDate(actualStr, actualDateVal);
		chkDeleteRow();
	}	
	
	@Test
	public void test9Query() {
		addIdFlag = true;
		deleteBeforeInsertFlag = false;
		createScalarType("date", "");
		createStructType("date", "");
		do3Lets();

		//C
		typeNameToUse = "C";
		chkFieldDate(actualStr, actualDateVal);
		nextId++;
		chkFieldDate(actualStr2, actualDateVal2);
		
		//check queries
		chkQueryDate("[44]", actualDateVal);
		chkQueryDate("[45]", actualDateVal2);
		
		String s = String.format("[field1==%s]", actualStr);
		chkQueryDate(s, actualDateVal);
//TODO: fix this		chkQueryString2("[field1 > date('1900')]", actualDateVal, actualDateVal2);
		//TODO also test 114==field1 and 0 < field1
		//TODO fix chkQuery2("[field1 != null]", "114", "1142");
	}	
	
	// --
	private Date actualDateVal;
	private String actualStr;
	private Date actualDateVal2;
	private String actualStr2;


	@Before
	public void init() {
		super.init();
		TimeZoneService tzSvc = new TimeZoneServiceImpl();
		DateFormatService fmtSvc = new DateFormatServiceImpl(tzSvc);
		actualDateVal = fmtSvc.parse("1955");
		actualStr = String.format("'%s'", fmtSvc.format(this.actualDateVal));
		actualDateVal2 = fmtSvc.parse("1956");
		actualStr2 = String.format("'%s'", fmtSvc.format(this.actualDateVal2));
	}

	protected void chkDate(String valStr, Date expected) {
		chkDate(valStr, "", expected);
	}
	protected void chkDate(String valStr, String varType, Date expected) {
		DValue dval = chkOneField(valStr, varType, "date", expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			chkScalarValue(expected, dval);
		}
	}
	private void chkScalarValue(Date dt, DValue dval) {
		assertEquals(dt, dval.asDate());
	}
	protected void chkDateString(String valStr, Date expected) {
		chkDateString(valStr, "", expected);
	}
	protected void chkDateString(String valStr, String varType, Date expected) {
		DValue dval = chkOneField(valStr, varType, "string", expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			chkScalarValue(expected, dval);
		}
	}
	protected void chkDateRef(String valStr, String varType, Date expected) {
		DValue dval = chkOneFieldRef(valStr, varType, expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			chkScalarValue(expected, dval);
		}
	}
	protected void chkDateFail(String valStr, String varType, String expectedErrId) {
		chkOneFieldFail(valStr, varType, "date", expectedErrId);
	}
	protected void chkFieldDate(String str, Date expected) {
		DValue dval = chkOneField(str);
		chkFieldValue(dval, expected);
	}
	private void chkFieldValue(DValue dval, Date expected) {
		assertEquals(expected, dval.asStruct().getField("field1").asDate());
	}

	protected void chkUpdateFieldDate(String str, Date expected) {
		DValue dval = chkUpdateOneField(str);
		chkFieldValue(dval, expected);
	}
	protected void chkQueryDate(String string, Date string2) {
		DValue dval = doChkQuery(string);
		chkFieldValue(dval, string2);
	}
	protected void chkQueryString2(String string, Date expected, Date expected2) {
		QueryResponse qresp = doChkQuery2(string);
		DValue dval = qresp.dvalList.get(0);
		chkFieldValue(dval, expected);
		dval = qresp.dvalList.get(1);
		chkFieldValue(dval, expected2);
	}
	protected void chkLetFieldOrFnDate(String valStr, Date expected) {
		DValue dval = doChkLetFieldOrFn(valStr, BuiltInTypes.DATE_SHAPE.name());
		assertEquals(expected, dval.asDate());
	}

}
