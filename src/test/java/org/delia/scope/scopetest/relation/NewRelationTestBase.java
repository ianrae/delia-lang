package org.delia.scope.scopetest.relation;

import static org.junit.Assert.assertEquals;

import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.DRelation;
import org.delia.type.DValue;

public class NewRelationTestBase extends DeliaClientTestBase { 
	
	// --
	
	public void init() {
		super.init();
	}
	
	protected void chkRelation(ResultValue res, String fieldName, int expected, String typeName) {
		DValue dval = res.getAsDValue();
		DRelation drel = dval.asStruct().getField(fieldName).asRelation();
		assertEquals(expected, drel.getForeignKey().asInt());
		assertEquals(typeName, drel.getTypeName());
	}
	protected void chkRelationNull(ResultValue res, String fieldName) {
		DValue dval = res.getAsDValue();
		DValue inner = dval.asStruct().getField(fieldName);
		assertEquals(null, inner);
	}
	protected void chkRelationMulti(ResultValue res, String fieldName, int expected1, int expected2, String typeName) {
		DValue dval = res.getAsDValue();
		DRelation drel = dval.asStruct().getField(fieldName).asRelation();
		assertEquals(expected1, drel.getMultipleKeys().get(0).asInt());
		assertEquals(expected2, drel.getMultipleKeys().get(1).asInt());
		assertEquals(typeName, drel.getTypeName());
	}

	protected void chkInt(ResultValue res, String fieldName, int expected) {
		DValue dval = res.getAsDValue();
		assertEquals(expected, dval.asStruct().getField(fieldName).asInt());
	}
	protected void chkEntityNoRel(String typeName, int id, String nullField) {
		String src = String.format("%s%s[%d]", buildLet(), typeName, id);
		ResultValue res = execStatement(src);
		chkInt(res, "id", id);
		chkNullField(res, nullField);
	}
	protected ResultValue chkEntity(String typeName, int id) {
		String src = String.format("%s%s[%d]", buildLet(), typeName, id);
		ResultValue res = execStatement(src);
		chkInt(res, "id", id);
		return res;
	}

	protected ResultValue queryAndChk(String typeName, int expected) {
		ResultValue res = execStatement(String.format("%s %s[%d]", buildLet(), typeName, expected));
		chkInt(res, "id", expected);
	//	chkNullField(res, "addr");
		return res;
	}
	protected ResultValue queryAddressFKAndChk(int id, int expected) {
//		ResultValue res = execStatement(String.format("%s Address[cust==%d]", buildLet(), id));
		ResultValue res = execStatement(String.format("%s Address[cust=%d]", buildLet(), id));
		QueryResponse qresp = (QueryResponse) res.val;
		assertEquals(expected, qresp.dvalList.size());
		return res;
	}
	protected ResultValue queryAddressMulti(int id1, int id2, int expected) {
		ResultValue res = execStatement(String.format("%s Address[id in [%d, %d]]", buildLet(), id1, id2));
		QueryResponse qresp = (QueryResponse) res.val;
		assertEquals(expected, qresp.dvalList.size());
		return res;
	}
	protected void queryAndChkCustomer(int id) {
		ResultValue res = queryAndChk("Customer", id);
		chkInt(res, "id", id);
	}


	protected ResultValue queryAndChkNull(String typeName, int id, String fieldName) {
		ResultValue res = queryAndChk(typeName, id);
		chkNullField(res, fieldName);
		return res;
	}
	protected ResultValue queryAndFetch(String typeName, String fetchField, int expected) {
		ResultValue res = execStatement(String.format("%s %s[%d].fetch('%s')", buildLet(), typeName, expected, fetchField));
		return res;
	}

	
}
