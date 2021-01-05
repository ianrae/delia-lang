package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.StringExp;
import org.junit.Before;
import org.junit.Test;

/**
 * New HLS. yet another attempt at a better replacement for exp objects.
 * -get rid of idea of spans!!
 *  
 * type and filter  Customer[id > 10]        initial type and WHERE filter
 *   filter:
 *     true
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
 * 
 * @author Ian Rae
 *
 */
public class NewHLSTests extends HLSTestBase {
	
	public interface FilterCond {
		
	}
	public static class BooleanFilterCond implements FilterCond {
		private boolean flag;
		 
		public BooleanFilterCond(boolean flag) {
			this.flag = flag;
		}
		public boolean asBoolean() {
			return flag;
		}
	}
	public static class IntFilterCond implements FilterCond {
		private int n;
		 
		public IntFilterCond(int n) {
			this.n = n;
		}
		public int asInt() {
			return n;
		}
	}
	public static class LongFilterCond implements FilterCond {
		private long n;
		 
		public LongFilterCond(long n) {
			this.n = n;
		}
		public long asLong() {
			return n;
		}
	}
	public static class StringFilterCond implements FilterCond {
		private String str;
		 
		public StringFilterCond(String str) {
			this.str = str;
		}
		public String asString() {
			return str;
		}
	}
	public static enum ValType {
		BOOLEAN,
		INT,
		LONG,
		NUMBER,
		STRING,
		SYMBOL,
		FUNCTION
	}
	public static class FilterFunc {
		public String fnName;
		public List<FilterVal> argL;
	}
	public static class FilterVal {
		//name,int,boolean,string,fn
		public ValType valType;
		public Exp exp;
		public boolean asBoolean() {
			return false;
		}
		public int asInt() {
			return 0; //
		}
		public long asLong() {
			return 0L; //
		}
		public double asNumber() {
			return 0.0;
		}
		public String asString() {
			return "";
		}
		public FilterFunc asFunc() {
			return null;
		}
	}
	public static class FilterOp {
		public String op; //==,!=,<,>,<=,>=
	}
	public static class OpFilterCond implements FilterCond {
		//[not] val op val
		public boolean isNot;
		public FilterVal val1;
		public FilterOp op;
		public FilterVal val2;
	}
	public static class LikeFilterCond implements FilterCond {
		//[not] val like val
		public boolean isNot;
		public FilterVal val1;
		public List<FilterVal> inList;
	}
	public static class AndOrFilterCond implements FilterCond {
		//[not] cond and/or cond
		public boolean isNot;
		public FilterCond val1;
		public boolean isAnd; //if false then is OR
		public FilterCond val2;
	}
	
	public static class FilterCondBuilder {
		
		public FilterCond build(QueryExp queryExp) {
			Exp cond = queryExp.filter.cond;
			if (cond instanceof BooleanExp) {
				BooleanExp exp = (BooleanExp) queryExp.filter.cond;
				return new BooleanFilterCond(exp.val.booleanValue());
			} else if (cond instanceof IntegerExp) {
				IntegerExp exp = (IntegerExp) queryExp.filter.cond;
				return new IntFilterCond(exp.val.intValue());
			} else if (cond instanceof LongExp) {
				LongExp exp = (LongExp) queryExp.filter.cond;
				return new LongFilterCond(exp.val.longValue());
			} else if (cond instanceof StringExp) {
				StringExp exp = (StringExp) queryExp.filter.cond;
				return new StringFilterCond(exp.val);
			}
			return null;
		}
	}
	
	
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
		chkbuilderLong("let x = Flight[15]", 15L);
	}	
	@Test
	public void testPKString() {
		pkType = "string";
		chkbuilderString("let x = Flight['abc']", "abc");
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
		IntFilterCond bfc = (IntFilterCond) cond;
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
		FilterCondBuilder builder = new FilterCondBuilder();
		FilterCond cond = builder.build(queryExp);
		return cond;
	}

	@Override
	protected String buildSrc() {
		String src = String.format("type Flight struct {field1 %s primaryKey, field2 int } end", pkType);
		
		if (pkType.equals("string")) {
			src += "\n insert Flight {field1: 'ab', field2: 10}";
			src += "\n insert Flight {field1: 'cd', field2: 20}";
			
		} else {
			src += "\n insert Flight {field1: 1, field2: 10}";
			src += "\n insert Flight {field1: 2, field2: 20}";
		}
		return src;
	}
	
}
