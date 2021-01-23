package org.delia.db.hld;


import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.hld.HLDQuery;
import org.delia.hld.HLDQueryBuilder;
import org.delia.hld.HLDQueryStatement;
import org.delia.hld.JoinTreeBuilder;
import org.delia.hld.ValType;
import org.delia.hld.cond.BooleanFilterCond;
import org.delia.hld.cond.FilterCond;
import org.delia.hld.cond.FilterCondBuilder;
import org.delia.hld.cond.FilterFunc;
import org.delia.hld.cond.FilterVal;
import org.delia.hld.cond.InFilterCond;
import org.delia.hld.cond.IntegerFilterCond;
import org.delia.hld.cond.LongFilterCond;
import org.delia.hld.cond.OpFilterCond;
import org.delia.hld.cond.StringFilterCond;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.junit.Before;
import org.junit.Test;

/**
 * New HLS. yet another attempt at a better replacement for exp objects.
 * -get rid of idea of spans!!
 *  
 * type and filter  Customer[id > 10]        initial type and WHERE filter
 *   filter:
 *     true or pkval [15]
 *     [not] val op val
 *     [not] val like str
 *     val in [sdfsdf]
 *     cond AND/OR cond
 *     date fns
 * throughChain    .addr.country             0 or more. contiguous chain
 * field           .name                     0 or 1. query one field, not an object
 * listFn          orderBy/distinct/limit/offset/first,last,ith      sort,paging. optional    
 * fetch           fks,fetch('aaa'),...          load 0 or more sub-objects.
 * calcFn          exists,count,min,max,average,sum,...   query produces a calculated result. 
 * 
 * goals
 *  -convert filter to better objects
 *  -MEM
 *  -H2,PG using SQL 
 *    -joins caused by throughChain,fetch
 *      -implicit join: orderBy, field, and fields mentioned in filter    
 *        -only join pk if parent (in 1:1 or 1:N)
 *  -use newHLS for filters in update/upsert/delete statements
 *    
 * TODO: add delia inject attack prevention tests!
 * 
 * steps
 *  -build filtercond
 *  -build hld
 *  -fill in fieldVal.structField on all SYMBOLS that are fieldnames (they can be a let var or a fieldname. fieldname takes precedence)
 *   -actually resolve varnames to scalar values here.
 *  -build joinL and then aliases
 *  -build fieldL. affected by joins, fetch,fks,count(*), ....
 *    field should have structField.
 *    fields grouped in columnRuns (use a string groupName)
 *	public boolean isAssocField; and probably the alias name b.custId as addr
 *     -we don't want to build or construct anything during query execution. 
 *     all should be in field so that we can cache it.
 *  -should handle scalar results (count() or .firstName)
 *  -should handle select * query (lookup fields by name in rs)    
 * -now we have a high level version of the query in hld.
 * -generate sql. types of sql  
 *   -select *
 *   -count
 *   -regular
 * Development plan
 * -do Customer[true] in MEM and sql (don't actually wire up h2)
 * -do [45]
 * -do [id > 10] //leave in and like for later
 *  -do not, and do bool,int,long,number,date,enum
 * -do order/limit stuff
 * -do .firstName scalar result
 * -do simple join, 1:1, 1:N, M:N
 * -do fetch join, then implicit joins
 * -do through join, and self-join
 * -do first,last,ith,count,...
 * 
 * -idea is a new set of unit tests that fully test MEM and sql generation
 * 
 * @author Ian Rae
 *
 */
public class NewHLSTests extends NewHLSTestBase {
	
	//	 * type and filter  Customer[id > 10]        initial type and WHERE filter
	//	 *   filter:
	//	 *     true or pkval [15]
	//	 *     [not] val op val
	//	 *     [not] val like str
	//	 *     val in [sdfsdf]
	//	 *     cond AND/OR cond
	//	 *     date fns
	//	 * throughChain    .addr.country             0 or more. contiguous chain
	//	 * field           .name                     0 or 1. query one field, not an object
	//	 * listFn          orderBy/distinct/limit/offset/first,last,ith      sort,paging. optional    
	//	 * fetch           fks,fetch('aaa'),...          load 0 or more sub-objects.
	//	 * calcFn          exists,count,min,max,average,sum,...   query produces a calculated result. 

	@Test
	public void testBool() {
		chkbuilderBool("let x = Flight[true]", true);
		chkbuilderBool("let x = Flight[false]", false);
	}
	@Test
	public void testPKInt() {
		chkbuilderInt("let x = Flight[15]", 15);
	}	
	@Test
	public void testPKLong() {
		pkType = "long";
		chkbuilderLong("let x = Flight[2147483648]", 2147483648L);
	}	
	@Test
	public void testPKString() {
		pkType = "string";
		chkbuilderString("let x = Flight['abc']", "abc");
	}	
	//	@Test TODO  FIX
	//	public void testPKSymbol() {
	////		chkbuilderInt("let y = 1\n let x = Flight[y]", 15);
	//		 //need better source to test this
	//	}	
	//	@Test TODO  FIX
	//	public void testPKFn() {
	////		chkbuilderInt("let x = Flight[myfn(13)]", 15);
	//		//need better source to test this
	//	}	

	@Test
	public void testOp1() {
		chkbuilderOpSymbolInt("let x = Flight[field1 < 15]", "field1", "<", 15);
		chkbuilderOpIntSymbol("let x = Flight[15 < field1]", 15, "<", "field1");
	}	
	@Test
	public void testIn() {
		chkbuilderInInt("let x = Flight[field1 in [55]]", "field1", "in", 55);
	}	
	@Test
	public void testLike() {
		useStringSrc = true;
		chkbuilderOpSymbolStr("let x = Flight[field1 like '%ab']", "field1", "like", "%ab");
	}	

	@Test
	public void testHLD() {
		String src = "let x = Flight[15]";
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		HLDQueryBuilder hldBuilder = new HLDQueryBuilder(this.session.getExecutionContext().registry);

		HLDQuery hld = hldBuilder.build(queryExp, new DoNothingVarEvaluator());
		log.log(hld.toString());
		//		assertEquals()

		JoinTreeBuilder joinBuilder = new JoinTreeBuilder();
		joinBuilder.generateJoinTree(hld);
		assertEquals(0, hld.joinL.size());
	}	

	@Test
	public void testHLDField() {
		String src = "let x = Flight[15]";
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		assertEquals("SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1=15", sql);

		SqlStatement stm = doQueryGenSQL(hld); 
		chkStm(stm, "SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1=?", "15");
	}	

	private SqlStatement doQueryGenSQL(HLDQueryStatement hld) {
		SqlStatementGroup stgroup = mgr.generateSql(hld);
		return stgroup.statementL.get(0);
	}
	@Test
	public void testHLDField2() {
		String src = "let x = Flight[field1 < 15]";
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		assertEquals("SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1 < 15", sql);

		SqlStatement stm = doQueryGenSQL(hld); 

		chkStm(stm, "SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1 < ?", "15");
	}	

	@Test
	public void testHLDFieldNot() {
		String src = "let x = Flight[!(field1 < 15)]";
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		assertEquals("SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE NOT t0.field1 < 15", sql);

		SqlStatement stm = doQueryGenSQL(hld); 
		chkStm(stm, "SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE NOT t0.field1 < ?", "15");
	}	
	
	@Test
	public void testHLDFieldIn() {
		String src = "let x = Flight[field1 in [55]]";
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		assertEquals("SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1 IN (55)", sql);
	}	
	@Test
	public void testHLDFieldLike() {
		useStringSrc = true;
		String src = "let x = Flight[field1 like '%a']";
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		assertEquals("SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1 LIKE '%a'", sql);
	}	
	
	
	@Test
	public void testHLDFieldCount() {
		String src = "let x = Flight[field1 < 15].count()";
		HLDQueryStatement hld = buildFromSrc(src, 0); 

		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		assertEquals("SELECT count(*) FROM Flight as t0 WHERE t0.field1 < 15", sql);

		SqlStatement stm = doQueryGenSQL(hld); 
		chkStm(stm, "SELECT count(*) FROM Flight as t0 WHERE t0.field1 < ?", "15");
	}	
	
	
	//-------------------------
	private String pkType = "int";

	@Before
	public void init() {
		//createDao();
	}

	private void chkbuilderBool(String src, boolean expected) {
		FilterCond cond = buildCond(src);
		BooleanFilterCond bfc = (BooleanFilterCond) cond;
		assertEquals(expected, bfc.asBoolean());
	}
	private void chkbuilderInt(String src, int expected) {
		FilterCond cond = buildCond(src);
		IntegerFilterCond bfc = (IntegerFilterCond) cond;
		assertEquals(expected, bfc.asInt());
	}
	private void chkbuilderLong(String src, long expected) {
		FilterCond cond = buildCond(src);
		LongFilterCond bfc = (LongFilterCond) cond;
		assertEquals(expected, bfc.asLong());
	}
	private void chkbuilderString(String src, String expected) {
		FilterCond cond = buildCond(src);
		StringFilterCond bfc = (StringFilterCond) cond;
		assertEquals(expected, bfc.asString());
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
	private void chkbuilderOpSymbolInt(String src, String val1, String op, int val2) {
		FilterCond cond = buildCond(src);
		OpFilterCond ofc = (OpFilterCond) cond;
		chkSymbol(val1, ofc.val1);
		assertEquals(op, ofc.op.toString());
		chkInt(val2, ofc.val2);
	}
	private void chkbuilderOpSymbolStr(String src, String val1, String op, String str) {
		FilterCond cond = buildCond(src);
		OpFilterCond ofc = (OpFilterCond) cond;
		chkSymbol(val1, ofc.val1);
		assertEquals(op, ofc.op.toString());
		chkString(str, ofc.val2);
	}
	private void chkbuilderOpIntSymbol(String src, int val1, String op, String val2) {
		FilterCond cond = buildCond(src);
		OpFilterCond ofc = (OpFilterCond) cond;
		chkInt(val1, ofc.val1);
		assertEquals(op, ofc.op.toString());
		chkSymbol(val2, ofc.val2);
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

	private void chkbuilderInInt(String src, String val1, String op, int val2) {
		FilterCond cond = buildCond(src);
		InFilterCond ofc = (InFilterCond) cond;
		chkSymbol(val1, ofc.val1);
		assertEquals(op, ofc.op.toString());
		chkInt(val2, ofc.list.get(0));
	}
	
	private void chkFn(String fieldName, String fnName, FilterVal fval, int n) {
		assertEquals(ValType.FUNCTION, fval.valType);
		FilterFunc func = fval.filterFn;
		assertEquals(n, func.argL.size());
		assertEquals(fieldName, fval.asString());
		assertEquals(fnName, func.fnName);
	}
	private void chkSymbol(String val1, FilterVal fval) {
		assertEquals(ValType.SYMBOL, fval.valType);
		assertEquals(val1, fval.asSymbol());
	}
	private void chkInt(int val1, FilterVal fval) {
		assertEquals(ValType.INT, fval.valType);
		assertEquals(val1, fval.asInt());
	}
	private void chkString(String val1, FilterVal fval) {
		assertEquals(ValType.STRING, fval.valType);
		assertEquals(val1, fval.asString());
	}


	@Override
	protected String buildSrc() {
		String s = "";
		String src = String.format("type Flight struct {field1 %s primaryKey, field2 int %s } end", pkType, s);

		s = "";
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
