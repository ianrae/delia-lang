package org.delia.scope.scopetest.value;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.error.DeliaError;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.ValueException;
import org.delia.scope.scopetest.ScopeTestBase;
import org.delia.type.BuiltInTypes;
import org.delia.type.DValue;

public class TypeLayerTestBase extends ScopeTestBase {

	protected DValue doChkQuery(String string) {
		String filter = String.format("%s%s", typeNameToUse, string);
		QueryResponse qresp = execLetQuery(filter, "", "queryResponse");
		DValue dval = qresp.getOne();
		return dval;
	}
	protected QueryResponse doChkQuery2(String string) {
		String filter = String.format("%s%s", typeNameToUse, string);
		QueryResponse qresp = execLetQuery(filter, "", "queryResponse", 2);
		return qresp;
	}
	// --
	protected int nextVarNum = 1;
	protected String typeNameToUse;
	protected boolean addIdFlag;
	protected int nextId = 44;
	protected boolean deleteBeforeInsertFlag = true;

	public void init() {
		runner = initRunner();
		DBHelper.createTable(dbInterface, "C"); //!! fake schema
		DBHelper.createTable(dbInterface, "C2"); //!! fake schema
	}

	protected void createScalarType(String type, String rule) {
		String src = String.format("type X %s %s end", type, rule);
		ResultValue res = execTypeStatement(src);
		chkResOK(res);
		chelper = helper.createCompilerHelper();
		src = String.format(" type X2 X end", type);
		res = execTypeStatement(src);
		chkResOK(res);
	}
	protected void createStructType(String type, String rule) {
		String sid = addIdFlag ? String.format(" id int unique") : "";
		String src = String.format("type C struct { %s field1 %s } %s end", sid, type, rule);
		ResultValue res = execTypeStatement(src);
		chkResOK(res);
		chelper = helper.createCompilerHelper();
//		src = String.format("type C2 C { field2 %s } %s end", type, rule);
		src = String.format("type C2 C { } end");
		res = execTypeStatement(src);
		chkResOK(res);
	}
	protected QueryResponse insertAndQuery(String typeName, String valStr, int expectedSize) {
		String sid = addIdFlag ? String.format(" id:%d", nextId) : "";
		String src = String.format("insert %s {%s field1:%s}", typeName, sid, valStr);
		execInsertStatement(src);

		//now query it
		String varName = String.format("a%d", nextVarNum++);
		src = String.format("let %s = %s", varName, typeName);
		return execLetStatementMulti(src, expectedSize);
	}
	protected ResultValue insertRaw(String typeName, String valStr) {
		String sid = addIdFlag ? String.format(" id:%d", nextId) : "";
		String src = String.format("insert %s {%s field1:%s}", typeName, sid, valStr);
		return execInsertStatement(src, false);
	}
	protected void insertFail(String typeName, String valStr, int expectedErrCount, String errId) {
		String sid = addIdFlag ? String.format(" id:%d", nextId) : "";
		String src = String.format("insert %s {%s field1:%s}", typeName, sid, valStr);
		execInsertFail(src, expectedErrCount, errId);
	}
	protected QueryResponse updateAndQuery(String typeName, String valStr, int expectedSize) {
		String sid = addIdFlag ? String.format("[%d]", nextId) : "";
		String src = String.format("update %s%s {field1:%s}", typeName, sid, valStr);
		execUpdateStatement(src);

		//now query it
		String varName = String.format("a%d", nextVarNum++);
		src = String.format("let %s = %s", varName, typeName);
		return execLetStatementMulti(src, expectedSize);
	}
	protected QueryResponse deleteAndQuery(String typeName, int expectedSize) {
		String sid = addIdFlag ? String.format("[%d]", nextId) : "";
		String src = String.format("delete %s%s", typeName, sid);
		execDeleteStatement(src);

		//now query it
		String varName = String.format("a%d", nextVarNum++);
		src = String.format("let %s = %s", varName, typeName);
		return execLetStatementMulti(src, expectedSize);
	}

	
	protected DValue execLet(String valStr, String varType, String type) {
		String varName = String.format("a%d", nextVarNum++);
		String src = String.format("let %s %s = %s", varName, varType, valStr);
		return execLetStatementScalar(src, type);
	}
	protected QueryResponse execLetQuery(String valStr, String varType, String type) {
		return execLetQuery(valStr, varType, type, 1);
	}
	protected QueryResponse execLetQuery(String valStr, String varType, String type, int num) {
		String varName = String.format("a%d", nextVarNum++);
		String src = String.format("let %s %s = %s", varName, varType, valStr);
		return execLetStatementMulti(src, num);
	}
	
	protected ResultValue execLetFail(String valStr, String varType, String type) {
		String varName = String.format("a%d", nextVarNum++);
		String src = String.format("let %s %s = %s", varName, varType, valStr);
		ResultValue res = execLetStatementScalarFail(src, type);
		return res;
	}
	
	protected DValue chkOneField(String valStr, String varType, String typeDefault, Object expected) {
		if (expected == null) {
			String type = varType.isEmpty() ? null : varType;
			DValue dval = execLet(valStr, varType, type);
			return dval;
		} else {
			String type = varType.isEmpty() ? typeDefault : varType;
			DValue dval = execLet(valStr, varType, type);
			return dval;
		}
	}
	protected DValue chkOneFieldRef(String valStr, String varType, Object expected) {
		if (expected == null) {
			String type = varType.isEmpty() ? "queryResponse" : varType;
			DValue dval = execLet(valStr, varType, type);
			return dval;
		} else {
			String type = varType.isEmpty() ? "queryResponse" : varType;
			//it's not really a queryResponse, but parser doesn't know
			DValue dval = execLet(valStr, varType, type);
			return dval;
		}
	}
	protected void chkOneFieldFail(String valStr, String varType, String typeDefault, String expectedErrId) {
		String type = varType.isEmpty() ? typeDefault : varType;
		ResultValue res = execLetFail(valStr, varType, type);
		DeliaError err = res.getLastError();
		assertEquals(expectedErrId, err.getId());
	}
	protected DValue chkOneField(String str) {
		clearTbl(typeNameToUse);
		int num = calcNumRecords();
		QueryResponse qresp = insertAndQuery(typeNameToUse, str, num);
		DValue dval = qresp.dvalList.get(num - 1);
		return dval;
	}
	protected int calcNumRecords() {
		return nextId - 44 + 1;
	}

	protected void chkFieldInsertFail(String str, String errId) {
		clearTbl(typeNameToUse);
		insertFail(typeNameToUse, str, 1, errId);
	}
	protected void chkFieldInsertParseFail(String str) {
		clearTbl(typeNameToUse);
		boolean pass = false;
//		try {
//			QueryResponse qresp = insertAndQuery(typeNameToUse, str, 1);
//		} catch (ValueException e) {
//			log(e.getMessage());
//			pass = true;
//		}
		ResultValue res = insertRaw(typeNameToUse, str);
		if (! res.ok) {
			log(res.getLastError().getMsg());
			pass = true;
		}
		
		assertEquals(true, pass);
	}
	protected DValue chkUpdateOneField(String str) {
		QueryResponse qresp = updateAndQuery(typeNameToUse, str, 1);
		DValue dval = qresp.getOne();
		return dval;
	}
	protected void chkUpdateMulti(String str, int expectedSize) {
		QueryResponse qresp = updateAndQuery(typeNameToUse, str, expectedSize);
	}
	protected void chkFieldUpdateFail(String str, String errId) {
		updateFail(typeNameToUse, str, 1, errId);
	}
	protected void updateFail(String typeName, String valStr, int expectedErrCount, String errId) {
		String sid = addIdFlag ? String.format(" id:%d", nextId) : "";
		String src = String.format("update %s {%s field1:%s}", typeName, sid, valStr);
		this.execUpdateFail(src, expectedErrCount, errId);
	}
	protected void chkDeleteRow() {
		QueryResponse qresp = deleteAndQuery(typeNameToUse, 0);
	}
	
	protected void clearTbl(String typeName) {
		if (!deleteBeforeInsertFlag) {
			return;
		}
		String src = String.format("delete %s", typeName);
		DeleteStatementExp exp = (DeleteStatementExp) chelper.parseOne(src);
		ResultValue res = runner.executeOneStatement(exp);
		chkResOK(res);
	}

	protected DValue doChkLetFieldOrFn(String valStr, String typeName) {
		String varName = String.format("a%d", nextVarNum++);
		String src = String.format("let %s = %s", varName, valStr);
		QueryResponse qresp = execLetStatementOne(src, typeName);
		DValue dval = qresp.getOne();
		return dval;
	}

}
