package org.delia.scope.scopetest.value.integer;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.scope.scopetest.value.TypeLayerTestBase;
import org.delia.type.BuiltInTypes;
import org.delia.type.DValue;

public class IntegerTestBase extends TypeLayerTestBase {

	// --
	protected void chkInteger(String valStr, Integer expected) {
		chkInteger(valStr, "", expected);
	}
	protected void chkInteger(String valStr, String varType, Integer expected) {
		DValue dval = chkOneField(valStr, varType, "int", expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			assertEquals(expected.intValue(), dval.asInt());
		}
	}
	protected void chkIntegerRef(String valStr, String varType, Integer expected) {
		DValue dval = chkOneFieldRef(valStr, varType, expected);
		if (expected == null) {
			assertEquals(null, dval);
		} else {
			assertEquals(expected.intValue(), dval.asInt());
		}
	}
	protected void chkIntegerFail(String valStr, String varType, String expectedErrId) {
		chkOneFieldFail(valStr, varType, "int", expectedErrId);
	}
	protected void chkFieldInteger(String str, Integer expected) {
		DValue dval = chkOneField(str);
		chkFieldValue(dval, expected);
	}
	private void chkFieldValue(DValue dval, Integer expected) {
		if (expected == null) {
			assertEquals(null, dval.asStruct().getField("field1"));
		} else {
			assertEquals(expected.intValue(), dval.asStruct().getField("field1").asInt());
		}
	}

	protected void chkUpdateFieldInteger(String str, Integer expected) {
		DValue dval = chkUpdateOneField(str);
		chkFieldValue(dval, expected);
	}
	protected void chkQueryInteger(String string, Integer string2) {
		DValue dval = doChkQuery(string);
		chkFieldValue(dval, string2);
	}
	protected void chkQueryString2(String string, Integer expected, Integer expected2) {
		QueryResponse qresp = doChkQuery2(string);
		DValue dval = qresp.dvalList.get(0);
		chkFieldValue(dval, expected);
		dval = qresp.dvalList.get(1);
		chkFieldValue(dval, expected2);
	}
	protected void chkLetFieldOrFnInt(String valStr, Integer expected) {
		DValue dval = doChkLetFieldOrFn(valStr, BuiltInTypes.INTEGER_SHAPE.name());
		assertEquals(expected.intValue(), dval.asInt());
	}

}
