package org.delia.db.hld;


import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.DateFormatService;
import org.delia.db.hld.HLDQueryStatement;
import org.delia.db.hld.ValType;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cond.FilterCondBuilder;
import org.delia.db.newhls.cond.FilterFunc;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.DeliaException;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class HLDDateTests extends NewHLSTestBase {
	
	@Test
	public void testDateFn() {
		addOrderDate = true;
		chkbuilderOpFnInt("let x = Flight[orderDate.day() == 31]", "orderDate", "day", "==", 31);
		chkbuilderOpIntFn("let x = Flight[31 == orderDate.day()]", 31, "==", "orderDate", "day");
	}	

	@Test
	public void testDate() {
		addOrderDate = true;
		List<String> allFns = Arrays.asList("year", "month", "day", "hour", "minute", "second");
		for(String fn: allFns) {
			chkDate(fn);
		}
	}	
	
	@Test(expected=DeliaException.class)
	public void testUnknownFn() {
		addOrderDate = true;
		chkDate("mississippi");
	}	
	
	@Test
	public void testDateParam() {
		addOrderDate = true;
		String fn = "month";
		String src = String.format("let x = Flight[orderDate.%s() == 31]", fn);
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		SqlStatementGroup stgroup = mgr.generateSql(hld);
		assertEquals(1, stgroup.size());
		SqlStatement stm = stgroup.getFirst();
		assertEquals(1, stm.paramL.size());
		assertEquals(31, stm.paramL.get(0).asInt());
		log.log(stm.sql);
		//not alias would normally be present on orderDate
		String s = String.format("SELECT t0.field1,t0.field2,t0.orderDate FROM Flight as t0 WHERE EXTRACT(%s FROM orderDate) = ?", fn.toUpperCase());
		assertEquals(s, stm.sql);
	}
	
	@Test
	public void testWholeDateParam() {
		String dateStr = "1955";
		Date actualDateVal = createDateFromStr(dateStr);

		addOrderDate = true;
		String src = String.format("let x = Flight[orderDate == '%s']", dateStr);
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		SqlStatementGroup stgroup = mgr.generateSql(hld);
		assertEquals(1, stgroup.size());
		SqlStatement stm = stgroup.getFirst();
		assertEquals(1, stm.paramL.size());
		
		//Dates values are strings in delia. So in FieldVal its a ValType.STRING
		//But during rendering sql we set FieldVal.actualDateVal to a Date so that 
		//it can be passed as a Date to the DB
		DValue param = stm.paramL.get(0);
		assertEquals(Shape.DATE, param.getType().getShape());
		
		ZonedDateTime zdt = param.asDate();
		DateFormatService fmtSvc = delia.getFactoryService().getDateFormatService();
		String ss = fmtSvc.format(zdt);
		assertEquals("1955-01-01T00:00:00.000+0000", ss);
		log.log(stm.sql);
		//not alias would normally be present on orderDate
		String s = String.format("SELECT t0.field1,t0.field2,t0.orderDate FROM Flight as t0 WHERE t0.orderDate = ?");
		assertEquals(s, stm.sql);
	}

	//-------------------------
	private String pkType = "int";
	private boolean addOrderDate = false;

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
	private void chkbuilderOpFnInt(String src, String fieldName, String val1, String op, int val2) {
		FilterCond cond = buildCond(src);
		OpFilterCond ofc = (OpFilterCond) cond;
		chkFn(fieldName, val1, ofc.val1, 0);
		assertEquals(op, ofc.op.toString());
		chkInt(val2, ofc.val2);
	}
	private void chkbuilderOpIntFn(String src, int val1, String op, String fieldName, String val2) {
		FilterCond cond = buildCond(src);
		OpFilterCond ofc = (OpFilterCond) cond;
		chkInt(val1, ofc.val1);
		assertEquals(op, ofc.op.toString());
		chkFn(fieldName, val2, ofc.val2, 0);
	}

	private void chkFn(String fieldName, String fnName, FilterVal fval, int n) {
		assertEquals(ValType.FUNCTION, fval.valType);
		FilterFunc func = fval.filterFn;
		assertEquals(n, func.argL.size());
		assertEquals(fieldName, fval.asString());
		assertEquals(fnName, func.fnName);
	}
	private void chkInt(int val1, FilterVal fval) {
		assertEquals(ValType.INT, fval.valType);
		assertEquals(val1, fval.asInt());
	}

	@Override
	protected String buildSrc() {
		String s = addOrderDate ? ", orderDate date" : "";
		String src = String.format("type Flight struct {field1 %s primaryKey, field2 int %s } end", pkType, s);

		s = addOrderDate ? ", orderDate: '2019'" : "";
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
