package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.manager.HLSManager;
import org.delia.db.hls.manager.HLSManagerResult;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DValue;
import org.delia.zdb.ZDBExecutor;
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
			if (queryExp.filter.cond instanceof BooleanExp) {
				BooleanExp exp = (BooleanExp) queryExp.filter.cond;
				return new BooleanFilterCond(exp.val.booleanValue());
			}
			return null;
		}
	}
	
	
	@Test
	public void test1() {
		chkbuilderBool("let x = Flight[true]", true);
		chkbuilderBool("let x = Flight[false]", false);
	}	
	
	
	//-------------------------
	private boolean generateSQLforMemFlag = true;
	
	@Before
	public void init() {
		//createDao();
	}

	private void chkbuilderBool(String src, boolean expected) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		FilterCondBuilder builder = new FilterCondBuilder();
		FilterCond cond = builder.build(queryExp);
		BooleanFilterCond bfc = (BooleanFilterCond) cond;
		assertEquals(expected, bfc.asBoolean());
	}
}
