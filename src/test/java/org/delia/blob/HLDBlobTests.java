package org.delia.blob;


import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.hld.NewHLSTestBase;
import org.delia.hld.HLDQueryStatement;
import org.delia.hld.ValType;
import org.delia.hld.cond.FilterCond;
import org.delia.hld.cond.FilterCondBuilder;
import org.delia.hld.cond.FilterFunc;
import org.delia.hld.cond.FilterVal;
import org.delia.hld.cond.OpFilterCond;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class HLDBlobTests extends NewHLSTestBase {
	
	@Test
	public void test() {
		addBlob = true;
		chkbuilderOpBlob("let x = Flight[field3 == '4E/QIA==']", "field3", "==", "4E/QIA==");
	}	
	@Test
	public void testBlobParam() {
		String src = String.format("let x = Flight[field3 == '4E/QIA==']");
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		SqlStatementGroup stgroup = mgr.generateSql(hld);
		assertEquals(1, stgroup.size());
		SqlStatement stm = stgroup.getFirst();
		assertEquals(1, stm.paramL.size());
		assertEquals(31, stm.paramL.get(0).asInt());
		log.log(stm.sql);
		//not alias would normally be present on orderDate
		String s = String.format("SELECT t0.field1,t0.field2,t0.orderDate FROM Flight as t0 WHERE EXTRACT(%s FROM orderDate) = ?", "FF");
		assertEquals(s, stm.sql);
	}

	//-------------------------
	private String pkType = "int";
	private boolean addBlob = false;

	@Before
	public void init() {
	}
	
	
	private void chkDate(String fn) {
		String src = String.format("let x = Flight[orderDate.%s() == 31]", fn);
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		//not alias would normally be present on orderDate
		String s = String.format("SELECT t0.field1,t0.field2,t0.orderDate FROM Flight as t0 WHERE EXTRACT(%s FROM orderDate) = 31", fn.toUpperCase());
		assertEquals(s, sql);
	}

	private FilterCond buildCond(String src) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		DTypeRegistry registry = session.getExecutionContext().registry;
		DStructType dtype = (DStructType) registry.getType("Flight");
		FilterCondBuilder builder = new FilterCondBuilder(registry, dtype, new DoNothingVarEvaluator());
		FilterCond cond = builder.build(queryExp);
		return cond;
	}
	private void chkbuilderOpBlob(String src, String fieldName, String op, String val2) {
		FilterCond cond = buildCond(src);
		OpFilterCond ofc = (OpFilterCond) cond;
		chkSymbol(fieldName, ofc.val1);
		assertEquals(op, ofc.op.toString());
		chkStr(val2, ofc.val2);
	}
//	private void chkbuilderOpIntFn(String src, int val1, String op, String fieldName, String val2) {
//		FilterCond cond = buildCond(src);
//		OpFilterCond ofc = (OpFilterCond) cond;
//		chkInt(val1, ofc.val1);
//		assertEquals(op, ofc.op.toString());
//		chkFn(fieldName, val2, ofc.val2, 0);
//	}

	private void chkSymbol(String fieldName, FilterVal fval) {
		assertEquals(ValType.SYMBOL, fval.valType);
		assertEquals(fieldName, fval.asString());
	}
	private void chkFn(String fieldName, String fnName, FilterVal fval, int n) {
		assertEquals(ValType.FUNCTION, fval.valType);
		FilterFunc func = fval.filterFn;
		assertEquals(n, func.argL.size());
		assertEquals(fieldName, fval.asString());
		assertEquals(fnName, func.fnName);
	}
	private void chkStr(String val1, FilterVal fval) {
		assertEquals(ValType.STRING, fval.valType);
		assertEquals(val1, fval.asString());
	}

	@Override
	protected String buildSrc() {
		String s = addBlob ? ", field3 blob" : "";
		String src = String.format("type Flight struct {field1 %s primaryKey, field2 int %s } end", pkType, s);

		s = addBlob ? ", field3: '4E/QIA=='" : "";
		if (pkType.equals("string")) {
			src += String.format("\n insert Flight {field1: 'ab', field2: 10 %s}", s);
			src += String.format("\n insert Flight {field1: 'cd', field2: 20 %s}", s);
		} else {
			src += String.format("\n insert Flight {field1: 1, field2: 10 %s}", s);
			src += String.format("\n insert Flight {field1: 2, field2: 20 %s}", s);
		}
		return src;
	}

}
