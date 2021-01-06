package org.delia.db.newhls;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.delia.db.hls.HLSTestBase;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
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
public class NewHLSTests extends HLSTestBase {

	public interface FilterCond {

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
		
		//resolved later
		public StructField structField; //only set if SYMBOL
		
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
			return filterFn; 
		}
		
		boolean isSymbol() {
			return valType.equals(ValType.SYMBOL);
		}
		boolean isFn() {
			return valType.equals(ValType.FUNCTION);
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
	
	//for [true], [16], [x], [myfunc(13)]
	public static class SingleFilterCond implements FilterCond {
		public FilterVal val1;

		@Override
		public String toString() {
			return val1.toString();
		}
	}
	public static class BooleanFilterCond extends SingleFilterCond {
		public BooleanFilterCond(BooleanExp exp) {
			this.val1 = new FilterVal(ValType.BOOLEAN, exp);
		}
		public boolean asBoolean() {
			return val1.asBoolean();
		}
	}
	public static class IntFilterCond extends SingleFilterCond  {
		public IntFilterCond(IntegerExp exp) {
			this.val1 = new FilterVal(ValType.INT, exp);
		}
		public int asInt() {
			return val1.asInt();
		}
	}
	public static class LongFilterCond extends SingleFilterCond {
		public LongFilterCond(LongExp exp) {
			this.val1 = new FilterVal(ValType.LONG, exp);
		}
		public long asLong() {
			return val1.asLong();
		}
	}
	public static class StringFilterCond extends SingleFilterCond {
		public StringFilterCond(StringExp exp) {
			this.val1 = new FilterVal(ValType.STRING, exp);
		}
		public String asString() {
			return val1.asString();
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
				return new BooleanFilterCond(exp);
			} else if (cond instanceof IntegerExp) {
				IntegerExp exp = (IntegerExp) queryExp.filter.cond;
				return new IntFilterCond(exp);
			} else if (cond instanceof LongExp) {
				LongExp exp = (LongExp) queryExp.filter.cond;
				return new LongFilterCond(exp);
			} else if (cond instanceof StringExp) {
				StringExp exp = (StringExp) queryExp.filter.cond;
				return new StringFilterCond(exp);
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

		public StructField(DStructType dtype, String field, DStructType fieldType) {
			this.dtype = dtype;
			this.fieldName = field;
			this.fieldType = fieldType;
		}

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
	
	public static class JoinElement  {
		public StructField structField;
//		public List<JTElement> nextL = new ArrayList<>();
		public RelationInfo relinfo;
		public boolean usedForFK; //if true then fks(). but this join for other reasons too
		public boolean usedForFetch; //if true then fettch. but this join for other reasons too
		
		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner("|");
			joiner.add(structField.dtype.getName());
			joiner.add(structField.fieldName);
			joiner.add(structField.fieldType.getName());
			return joiner.toString();
		}

		public boolean matches(TypePair pair) {
			if (pair.name.equals(structField.fieldName) && pair.type == structField.fieldType) {
				return true;
			}
			return false;
		}

		public TypePair createPair() {
			return new TypePair(structField.fieldName, structField.fieldType);
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
		public List<HLDField> fieldL = new ArrayList<>(); 

		//added after
		public List<JoinElement> joinL = new ArrayList<>();
		
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
				s += " fld()";
			} else {
				s += String.format(" fld(%s)", finalField.toString());
			}
			
			if (fetchL.isEmpty()) {
				s += " fetch[]";
			} else {
				StringJoiner joiner = new StringJoiner(",");
				for(FetchSpec sf: fetchL) {
					joiner.add(sf.toString());
				}
				s += String.format(" fetch[%s]", joiner.toString());
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
			
			if (fieldL.isEmpty()) {
				s += " {}";
			} else {
				StringJoiner joiner = new StringJoiner(",");
				for(HLDField rf: fieldL) {
					joiner.add(rf.toString());
				}
				s += String.format(" {%s}", joiner.toString());
			}
			return s;
		}
	}
	
	public static class HLDQueryBuilder {
		private DTypeRegistry registry;

		public HLDQueryBuilder(DTypeRegistry registry) {
			this.registry = registry;
		}

		public HLDQuery build(QueryExp queryExp) {
			HLDQuery hld = new HLDQuery();
			hld.fromType = (DStructType) registry.getType(queryExp.typeName);
			hld.mainStructType = hld.fromType; //TODO fix
			hld.resultType = hld.fromType; //TODO fix
			
			FilterCondBuilder builder = new FilterCondBuilder();
			hld.filter = builder.build(queryExp);
//			public List<StructField> throughChain = new ArrayList<>();
//			public StructField finalField; //eg Customer.addr
//			public List<FetchSpec> fetchL = new ArrayList<>(); //order matters: eg. .addr.fetch('country')
//			public List<QueryFnSpec> funcL = new ArrayList<>(); //list and calc fns. order matters: eg. .addr.first().city

			return hld;
		}
	}

	public static class JoinTreeBuilder {
		public void generateJoinTree(HLDQuery hld) {
			for(FetchSpec spec: hld.fetchL) {
				if (spec.isFK) {
					addFKs(spec, hld.joinL);
				} else {
					addFetch(spec, hld.joinL);
				}
			}
			
			for(QueryFnSpec fnspec: hld.funcL) {
				//if parent does .orderBy('addr') then we need a join.
				if (fnspec.filterFn.fnName.equals("orderBy")) {
					addOrderBy(fnspec, hld.joinL);
				}
			}
			
			//TODO: add throughChain
			
			if (hld.filter instanceof OpFilterCond) {
				OpFilterCond ofc = (OpFilterCond) hld.filter;
				addImplicitJoin(hld.fromType, ofc.val1, hld.joinL);
				addImplicitJoin(hld.fromType, ofc.val2, hld.joinL);
			}
			//TODO: do like and IN filters too, and AndOr
		}
		
		private void addImplicitJoin(DStructType fromType, FilterVal fval, List<JoinElement> resultL) {
			if (fval.isSymbol()) {
				String fieldName = fval.asSymbol();
				addFieldJoinIfNeeded(fromType, fieldName, resultL);
			} else if (fval.isFn()) {
				FilterFunc filterFn = fval.asFunc();
				for(FilterVal inner: filterFn.argL) {
					addImplicitJoin(fromType, inner, resultL); //*** recursion ***
				}
			}
		}

		private void addFieldJoinIfNeeded(DStructType fromType, String fieldName, List<JoinElement> resultL) {
			TypePair pair = DRuleHelper.findMatchingStructPair(fromType, fieldName);
			if (pair != null) {
				JoinElement el = buildElement(fromType, fieldName, (DStructType) pair.type);
				switch(el.relinfo.cardinality) {
				case ONE_TO_ONE:
				case ONE_TO_MANY:
					if (el.relinfo.isParent) {
						addElement(el, resultL);
					}
					break;
				case MANY_TO_MANY:
					addElement(el, resultL);
					break;
				}
			}
		}

		private void addOrderBy(QueryFnSpec fnspec, List<JoinElement> resultL) {
			DStructType structType = fnspec.structField.dtype;
			String fieldName = fnspec.structField.fieldName;
			TypePair pair = DRuleHelper.findMatchingPair(structType, fieldName);
			//ignore sort by scalar fields
			if (pair != null && pair.type instanceof DStructType) {
//				addElement(structType, fieldName, (DStructType) pair.type, resultL);
				JoinElement el = buildElement(structType, fieldName, (DStructType) pair.type);
				addElement(el, resultL);
			}
		}
		
		private void addFKs(FetchSpec spec, List<JoinElement> resultL) {
			DStructType structType = spec.structField.dtype;
			for(TypePair pair: structType.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
					if (relinfo.isParent || RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
						JoinElement el = buildElement(structType, pair.name, (DStructType) pair.type);
						el.usedForFK = true;
						addElement(el, resultL);
					}
				}
			}
		}
		private void addFetch(FetchSpec spec, List<JoinElement> resultL) {
			DStructType structType = spec.structField.dtype;
			String fieldName = spec.structField.fieldName;
			TypePair pair = DRuleHelper.findMatchingStructPair(structType, fieldName);
			if (pair != null) {
				JoinElement el = buildElement(structType, pair.name, (DStructType) pair.type);
				el.usedForFK = false;
				addElement(el, resultL);
			}
		}
		private JoinElement buildElement(DStructType dtype, String field, DStructType fieldType) {
			JoinElement el = new JoinElement();
			el.structField = new StructField(dtype, field, fieldType);
			el.relinfo = DRuleHelper.findMatchingRuleInfo(dtype, el.createPair());
			return el;
		}
		private void addElement(JoinElement el, List<JoinElement> resultL) {
			String target = el.toString();
			Optional<JoinElement> optExisting = resultL.stream().filter(x -> x.toString().equals(target)).findAny();
			if (optExisting.isPresent()) {
				if (el.usedForFK) {
					optExisting.get().usedForFK = true; //propogate
				}
				if (el.usedForFetch) {
					optExisting.get().usedForFetch = true; //propogate
				}
				return;
			}

			resultL.add(el);
		}
	}
	
	public static class HLDField {
		public DStructType structType;
		public String fieldName;
		public DType fieldType;
//		public boolean isAssocField;
		public String groupName;
		
		@Override
		public String toString() {
			String fldType = BuiltInTypes.convertDTypeNameToDeliaName(fieldType.getName());
			String s = String.format("%s.%s(%s)", structType.getName(), fieldName, fldType);
			return s;
		}
	}
	
	public static class HLDFieldBuilder {
		public void generateJoinTree(HLDQuery hld) {
			//TODO much more code needed here!
			addStructFields(hld, hld.fieldL);
			
		}
		
		private void addStructFields(HLDQuery hld, List<HLDField> fieldL) {
			DStructType fromType = hld.fromType;
			
			for(TypePair pair: fromType.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
					if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
						doManyToManyAddFKofJoins(fieldL, pair, relinfo, null, hld);
					} else if (!relinfo.isParent) {
						addField(fieldL, fromType, pair);
					}
				} else {
					addField(fieldL, fromType, pair);
				}
			}
		}

		private HLDField addField(List<HLDField> fieldL, DStructType fromType, TypePair pair) {
			HLDField rf = new HLDField();
			rf.structType = fromType;
			rf.fieldName = pair.name;
			rf.fieldType = pair.type;
			rf.groupName = "__MAINGROUP__";
			fieldL.add(rf);
			return rf;
		}
		private void doManyToManyAddFKofJoins(List<HLDField> fieldL, TypePair pair, RelationInfo relinfoA, JoinElement el, HLDQuery hld) {
//			String assocTbl = datIdMap.getAssocTblName(relinfoA.getDatId()); 
////			String fieldName = datIdMap.getAssocFieldFor(relinfoA);
//			String fieldName = datIdMap.getAssocFieldFor(relinfoA.otherSide);
//
//			AliasInfo aliasInfo = aliasManager.getAssocAlias(relinfoA.nearType, relinfoA.fieldName, assocTbl);
//			String s = aliasManager.buildFieldAlias(aliasInfo, fieldName);
//			s = String.format("%s as %s", s, pair.name);
//			RenderedField rff = addField(fieldL, null, fieldName, s);
//			rff.isAssocField = true;
//			rff.fieldGroup = new FieldGroup((el == null), el);
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
	public void testDateFn() {
		addOrderDate = true;
		chkbuilderOpFnInt("let x = Flight[orderDate.day() == 31]", "orderDate", "day", "==", 31);
		chkbuilderOpIntFn("let x = Flight[31 == orderDate.day()]", 31, "==", "orderDate", "day");
	}	

	@Test
	public void testHLD() {
		String src = "let x = Flight[15]";
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		HLDQueryBuilder hldBuilder = new HLDQueryBuilder(this.session.getExecutionContext().registry);
		
		HLDQuery hld = hldBuilder.build(queryExp);
		log.log(hld.toString());
//		assertEquals()
		
		JoinTreeBuilder joinBuilder = new JoinTreeBuilder();
		joinBuilder.generateJoinTree(hld);
		assertEquals(0, hld.joinL.size());
	}	
	
	@Test
	public void testHLDField() {
		String src = "let x = Flight[15]";
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		HLDQueryBuilder hldBuilder = new HLDQueryBuilder(this.session.getExecutionContext().registry);
		
		HLDQuery hld = hldBuilder.build(queryExp);
		log.log(hld.toString());
//		assertEquals()
		
		JoinTreeBuilder joinBuilder = new JoinTreeBuilder();
		joinBuilder.generateJoinTree(hld);
		assertEquals(0, hld.joinL.size());
		
		HLDFieldBuilder fieldBuilder = new HLDFieldBuilder();
		fieldBuilder.generateJoinTree(hld);
		log.log(hld.toString());
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
