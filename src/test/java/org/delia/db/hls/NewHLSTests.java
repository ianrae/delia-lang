package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.type.DStructType;
import org.delia.type.DType;
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
		@Override
		public String toString() {
			String s = String.format("%b", flag);
			return s;
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
		@Override
		public String toString() {
			String s = String.format("%d", n);
			return s;
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
		@Override
		public String toString() {
			String s = String.format("%d", n);
			return s;
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
		@Override
		public String toString() {
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
		public List<FilterVal> argL = new ArrayList<>();
		
		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner(",");
			for(FilterVal fval: argL) {
				joiner.add(fval.toString());
			}
			String s = String.format("%s(%s)", fnName, joiner.toString());
			return s;
		}
	}
	public static class FilterVal {
		//name,int,boolean,string,fn
		public ValType valType;
		public Exp exp;
		public FilterFunc filterFn; //normally null
		
		public FilterVal(ValType valType, Exp exp) {
			this.valType = valType;
			this.exp = exp;
		}
		
		public boolean asBoolean() {
			BooleanExp bexp = (BooleanExp) exp;
			return bexp.val.booleanValue();
		}
		public int asInt() {
			IntegerExp exp1 = (IntegerExp) exp;
			return exp1.val.intValue();
		}
		public long asLong() {
			LongExp exp1 = (LongExp) exp;
			return exp1.val.longValue();
		}
		public double asNumber() {
			NumberExp exp1 = (NumberExp) exp;
			return exp1.val.doubleValue();
		}
		public String asString() {
//			StringExp exp1 = (StringExp) exp;
			return exp.strValue();
		}
		public String asSymbol() {
			XNAFSingleExp nafexp = (XNAFSingleExp) exp;
			return nafexp.funcName;
		}
		public FilterFunc asFunc() {
			return null; //TODO!
		}
		
		@Override
		public String toString() {
			String fn = filterFn == null ? "" : ":" + filterFn.toString();
			String s = String.format("%s:%s%s", valType.name(), exp.strValue(), fn);
			return s;
		}
		
	}
	public static class FilterOp {
		public String op; //==,!=,<,>,<=,>=
		
		public FilterOp(String op) {
			this.op = op;
		}

		@Override
		public String toString() {
			return op;
		}
	}
	public static class OpFilterCond implements FilterCond {
		//[not] val op val
		public boolean isNot;
		public FilterVal val1;
		public FilterOp op;
		public FilterVal val2;
		
		@Override
		public String toString() {
			String fn = isNot ? "!" : "";
			String s = String.format("%s%s %s %s", fn, val1.toString(), op.toString(), val2.toString());
			return s;
		}
	}
//	public static class LikeFilterCond implements FilterCond {
//		//[not] val like val
//		public boolean isNot;
//		public FilterVal val1;
//		public List<FilterVal> inList;
//	}
//	public static class AndOrFilterCond implements FilterCond {
//		//[not] cond and/or cond
//		public boolean isNot;
//		public FilterCond val1;
//		public boolean isAnd; //if false then is OR
//		public FilterCond val2;
//	}

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
			} else if (cond instanceof FilterOpFullExp) {
				FilterOpFullExp exp = (FilterOpFullExp) queryExp.filter.cond;
				if (exp.opexp1 instanceof FilterOpExp) {
					FilterOpExp foexp = (FilterOpExp) exp.opexp1;
					if (foexp.op1 instanceof XNAFMultiExp) {
						XNAFMultiExp xnaf = (XNAFMultiExp) foexp.op1;
						OpFilterCond opfiltercond = new OpFilterCond();
						opfiltercond.isNot = exp.negFlag;
						opfiltercond.op = new FilterOp(foexp.op);
						opfiltercond.val1 = buildValOrFunc(exp, foexp, xnaf); 
						opfiltercond.val2 = new FilterVal(createValType(foexp.op2), foexp.op2);
						return opfiltercond;
					} else if (foexp.op2 instanceof XNAFMultiExp) {
						XNAFMultiExp xnaf = (XNAFMultiExp) foexp.op2;
						
						OpFilterCond opfiltercond = new OpFilterCond();
						opfiltercond.isNot = exp.negFlag;
						opfiltercond.op = new FilterOp(foexp.op);
						opfiltercond.val1 = new FilterVal(createValType(foexp.op1), foexp.op1);
						opfiltercond.val2 = buildValOrFunc(exp, foexp, xnaf); 
						return opfiltercond;
					}
				}
			}
			return null;
		}


		private FilterVal buildValOrFunc(FilterOpFullExp exp, FilterOpExp foexp, XNAFMultiExp xnaf) {
			if (xnaf.qfeL.size() == 1) {
				XNAFSingleExp el = xnaf.qfeL.get(0);
				return new FilterVal(ValType.SYMBOL, el);
			} else {
				XNAFNameExp el = (XNAFNameExp) xnaf.qfeL.get(0);
				XNAFSingleExp el2 = xnaf.qfeL.get(1); //TODO handle more than 2 later
				FilterVal fval = new FilterVal(ValType.FUNCTION, el);
				fval.filterFn = new FilterFunc();
				fval.filterFn.fnName = el2.funcName; //TODO: handle args later
				return fval;
			}
		}

		private ValType createValType(Exp op2) {
			if (op2 instanceof BooleanExp) {
				return ValType.BOOLEAN;
			} else if (op2 instanceof IntegerExp) {
				return ValType.INT;
			} else if (op2 instanceof LongExp) {
				return ValType.LONG;
			} else if (op2 instanceof NumberExp) {
				return ValType.NUMBER;
			} else if (op2 instanceof StringExp) {
				return ValType.STRING;
			} else {
				return null; //TODO: error
			}
		}
	}

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
	
	public static class StructField  {
		public DStructType dtype;
		public String fieldName;
		public DStructType fieldType;

		@Override
		public String toString() {
			String s = String.format("%s.%s:%s", dtype.getName(), fieldName, fieldType.getName());
			return s;
		}
	}
	public static class FetchSpec {
		public StructField structField;
		public boolean isFK; //if true then fks, else fetch

		@Override
		public String toString() {
			String s = String.format("%s:%b", structField.toString(), isFK);
			return s;
		}
	}
	public static class QueryFnSpec {
		public StructField structField; //who the func is being applied to. fieldName & fieldType can be null
		public FilterFunc filterFn;

		@Override
		public String toString() {
			String s = String.format("%s %", structField.toString(), filterFn.toString());
			return s;
		}
	}
	public static class HLDQuery {
		public DStructType fromType;
		public DStructType mainStructType; //C[].addr then fromType is A and mainStringType is C
		public DType resultType; //might be string if .firstName
		public FilterCond filter;
		public List<StructField> throughChain = new ArrayList<>();
		public StructField finalField; //eg Customer.addr
		public List<FetchSpec> fetchL = new ArrayList<>(); //order matters: eg. .addr.fetch('country')
		public List<QueryFnSpec> funcL = new ArrayList<>(); //list and calc fns. order matters: eg. .addr.first().city

		@Override
		public String toString() {
			String s = String.format("%s:%s[]:%s", resultType.getName(), fromType.getName(), mainStructType.getName());
			s += String.format(" [%s]", filter.toString());
			
			if (throughChain.isEmpty()) {
				s += " TC[]";
			} else {
				StringJoiner joiner = new StringJoiner(",");
				for(StructField sf: throughChain) {
					joiner.add(sf.toString());
				}
				s += String.format(" TC[%s]", joiner.toString());
			}
			
			if (finalField == null) {
				s += " FF()";
			} else {
				s += String.format(" FF(%s)", finalField.toString());
			}
			
			if (fetchL.isEmpty()) {
				s += " Fetch[]";
			} else {
				StringJoiner joiner = new StringJoiner(",");
				for(FetchSpec sf: fetchL) {
					joiner.add(sf.toString());
				}
				s += String.format(" Fetch[%s]", joiner.toString());
			}
			
			if (funcL.isEmpty()) {
				s += " fn[]";
			} else {
				StringJoiner joiner = new StringJoiner(",");
				for(QueryFnSpec sf: funcL) {
					joiner.add(sf.toString());
				}
				s += String.format(" fn[%s]", joiner.toString());
			}
			return s;
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
		chkbuilderLong("let x = Flight[2147483648]", 2147483648L);
	}	
	@Test
	public void testPKString() {
		pkType = "string";
		chkbuilderString("let x = Flight['abc']", "abc");
	}	

	@Test
	public void testOp1() {
		chkbuilderOpSymbolInt("let x = Flight[field1 < 15]", "field1", "<", 15);
		chkbuilderOpIntSymbol("let x = Flight[15 < field1]", 15, "<", "field1");
	}	

	@Test
	public void testDateFn() {
		addOrderDate = true;
		chkbuilderOpFnInt("let x = Flight[orderDate.day() == 31]", "orderDate", "day", "==", 31);
		chkbuilderOpIntFn("let x = Flight[31 == orderDate.day()]", 31, "==", "orderDate", "day");
	}	


	//-------------------------
	private String pkType = "int";
	private boolean addOrderDate = false;

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
	private void chkbuilderOpSymbolInt(String src, String val1, String op, int val2) {
		FilterCond cond = buildCond(src);
		OpFilterCond ofc = (OpFilterCond) cond;
		chkSymbol(val1, ofc.val1);
		assertEquals(op, ofc.op.toString());
		chkInt(val2, ofc.val2);
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
