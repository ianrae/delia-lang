package org.delia.scope.scopetest.value.string;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.scope.scopetest.value.TypeLayerTestBase;
import org.delia.type.BuiltInTypes;
import org.delia.type.DValue;

public class StringTestBase extends TypeLayerTestBase {


	// --
	protected void chkString(String valStr, String expected) {
		chkString(valStr, "", expected);
	}
	protected void chkString(String valStr, String varType, String expected) {
		DValue dval = chkOneField(valStr, varType, "string", expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			assertEquals(expected, dval.asString());
		}
	}
	protected void chkStringRef(String valStr, String varType, String expected) {
		DValue dval = chkOneFieldRef(valStr, varType, expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			assertEquals(expected, dval.asString());
		}
	}
	protected void chkStringFail(String valStr, String varType, String expectedErrId) {
		chkOneFieldFail(valStr, varType, "string", expectedErrId);
	}
	protected void chkFieldString(String str, String expected) {
		DValue dval = chkOneField(str);
		if (expected == null) {
			assertEquals(expected, dval.asStruct().getField("field1"));
		} else {
			assertEquals(expected, dval.asStruct().getField("field1").asString());
		}
	}
	protected void chkUpdateFieldString(String str, String expected) {
		DValue dval = chkUpdateOneField(str);
		if (expected == null) {
			assertEquals(expected, dval.asStruct().getField("field1"));
		} else {
			assertEquals(expected, dval.asStruct().getField("field1").asString());
		}
	}
	protected void chkQueryString(String string, String expected) {
		DValue dval = doChkQuery(string);
		if (expected == null) {
			assertEquals(expected, dval.asStruct().getField("field1"));
		} else {
			assertEquals(expected, dval.asStruct().getField("field1").asString());
		}
	}
	protected void chkQueryString2(String string, String expected, String expected2) {
		QueryResponse qresp = doChkQuery2(string);
		DValue dval = qresp.dvalList.get(0);
		assertEquals(expected, dval.asStruct().getField("field1").asString());
		dval = qresp.dvalList.get(1);
		assertEquals(expected2, dval.asStruct().getField("field1").asString());
	}
	protected void chkLetFieldOrFnString(String valStr, String expected) {
		DValue dval = doChkLetFieldOrFn(valStr, BuiltInTypes.STRING_SHAPE.name());
		assertEquals(expected, dval.asString());
	}

}
