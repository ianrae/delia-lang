package org.delia.db.newhls;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.hls.AliasInfo;
import org.delia.db.hls.HLSTestBase;
import org.delia.db.newhls.cond.BooleanFilterCond;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cond.FilterCondBuilder;
import org.delia.db.newhls.cond.FilterFunc;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.IntFilterCond;
import org.delia.db.newhls.cond.LongFilterCond;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.cond.StringFilterCond;
import org.delia.db.sql.StrCreator;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
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

	public static class FetchSpec {
		public RelationField structField;
		public boolean isFK; //if true then fks, else fetch

		@Override
		public String toString() {
			String s = String.format("%s:%b", structField.toString(), isFK);
			return s;
		}
	}
	public static class QueryFnSpec {
		public RelationField structField; //who the func is being applied to. fieldName & fieldType can be null
		public FilterFunc filterFn;

		@Override
		public String toString() {
			String s = String.format("%s %", structField.toString(), filterFn.toString());
			return s;
		}
	}

	public static class JoinElement  {
		public RelationField structField;
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
		public String fromAlias;
		public DStructType mainStructType; //C[].addr then fromType is A and mainStringType is C
		public DType resultType; //might be string if .firstName
		public FilterCond filter;
		public List<RelationField> throughChain = new ArrayList<>();
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
				for(RelationField sf: throughChain) {
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
			el.structField = new RelationField(dtype, field, fieldType);
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
//		public String groupName; just use alias??
		public String alias;
		public String asStr;

		@Override
		public String toString() {
			String fldType = BuiltInTypes.convertDTypeNameToDeliaName(fieldType.getName());
			String s = String.format("%s.%s(%s,%s)", structType.getName(), fieldName, fldType, alias);
			return s;
		}
	}

	public static class HLDFieldBuilder {
		private HLDAliasManager aliasMgr;

		public HLDFieldBuilder(HLDAliasManager aliasMgr) {
			this.aliasMgr = aliasMgr;
		}

		public void generateJoinTree(HLDQuery hld) {
			//TODO much more code needed here!
			addStructFields(hld, hld.fieldL);

			assignAliases(hld);
		}

		private void assignAliases(HLDQuery hld) {
			AliasInfo info = aliasMgr.createMainTableAlias(hld.fromType);
			hld.fromAlias = info.alias;
			for(HLDField rf: hld.fieldL) {
				if (rf.structType == hld.fromType) {
					rf.alias = info.alias;
				} else {
					//TODO!!
				}
			}
			
			//now populate SYMBOL FilterdVal
			if (hld.filter instanceof SingleFilterCond) {
				SingleFilterCond sfc = (SingleFilterCond) hld.filter;
				doFilterPKVal(sfc.val1, hld);
			} else if (hld.filter instanceof OpFilterCond) {
				OpFilterCond ofc = (OpFilterCond) hld.filter;
				doFilterVal(ofc.val1, hld);
				doFilterVal(ofc.val2, hld);
			}
		}

		private void doFilterVal(FilterVal val1, HLDQuery hld) {
			if (val1.isSymbol()) {
				String fieldName = val1.exp.strValue();
				DType fieldType = DValueHelper.findFieldType(hld.fromType, fieldName);
				val1.structField = new StructField(hld.fromType, fieldName, fieldType);
				val1.alias = hld.fromAlias;
			}
		}
		private void doFilterPKVal(FilterVal val1, HLDQuery hld) {
			TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(hld.fromType);
			String fieldName = pkpair.name;
			val1.structField = new StructField(hld.fromType, fieldName, pkpair.type);
			val1.alias = hld.fromAlias;
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
//			rf.groupName = "__MAINGROUP__";
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
	
	public static class HLDException extends RuntimeException {
		HLDException(String s) {
			super(s);
		}
	}
	
	public static class HLDSQLGenerator {
		
		public String generateSQL(HLDQuery hld) {
			StrCreator sc = new StrCreator();
			sc.o("SELECT ");
			
			StringJoiner joiner = new StringJoiner(",");
			for(HLDField rf: hld.fieldL) {
				if (rf.asStr != null) {
					joiner.add(String.format("%s.%s as %s", rf.alias, rf.fieldName, rf.asStr));
				} else {
					joiner.add(String.format("%s.%s", rf.alias, rf.fieldName));
				}
			}
			sc.o(joiner.toString());
			
			sc.o(" FROM %s as %s", hld.fromType.getName(), hld.fromAlias);
			
			generateWhere(sc, hld);
			return sc.toString();
		}

		private void generateWhere(StrCreator sc, HLDQuery hld) {
			FilterCond filter = hld.filter;
			String fragment = null;
			if (filter instanceof SingleFilterCond) {
				SingleFilterCond sfc = (SingleFilterCond) filter;
				String alias = sfc.val1.alias;
				String fieldName = sfc.val1.structField.fieldName;
				fragment = String.format("%s.%s=%s", alias, fieldName, sfc.renderSql());
			} else if (filter instanceof OpFilterCond) {
				OpFilterCond ofc = (OpFilterCond) filter;
				String s1 = renderVal(ofc.val1);
				String s2 = renderVal(ofc.val2);
				fragment = String.format("%s %s %s", s1, ofc.op.op, s2);
			}
			sc.o(" WHERE %s", fragment);
		}

		private String renderVal(FilterVal val1) {
			switch(val1.valType) {
			case BOOLEAN:
			case INT:
			case LONG:
			case NUMBER:
				return val1.exp.strValue();
			case STRING:
				return String.format("'%s'", val1.exp.strValue());
			case SYMBOL:
				return String.format("%s.%s", val1.alias, val1.structField.fieldName);
				
			case FUNCTION:
			default:
				throw new HLDException("renderVal not impl1");
			}
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

		HLDAliasManager aliasMgr = new HLDAliasManager(delia.getFactoryService());
		HLDFieldBuilder fieldBuilder = new HLDFieldBuilder(aliasMgr);
		fieldBuilder.generateJoinTree(hld);
		log.log(hld.toString());
		
		HLDSQLGenerator sqlgen = new HLDSQLGenerator();
		String sql = sqlgen.generateSQL(hld);
		log.log(sql);
		assertEquals("SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1=15", sql);
	}	

	@Test
	public void testHLDField2() {
		String src = "let x = Flight[field1 < 15]";
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		HLDQueryBuilder hldBuilder = new HLDQueryBuilder(this.session.getExecutionContext().registry);

		HLDQuery hld = hldBuilder.build(queryExp);
		log.log(hld.toString());
		//		assertEquals()

		JoinTreeBuilder joinBuilder = new JoinTreeBuilder();
		joinBuilder.generateJoinTree(hld);
		assertEquals(0, hld.joinL.size());

		HLDAliasManager aliasMgr = new HLDAliasManager(delia.getFactoryService());
		HLDFieldBuilder fieldBuilder = new HLDFieldBuilder(aliasMgr);
		fieldBuilder.generateJoinTree(hld);
		log.log(hld.toString());
		
		HLDSQLGenerator sqlgen = new HLDSQLGenerator();
		String sql = sqlgen.generateSQL(hld);
		log.log(sql);
		assertEquals("SELECT t0.field1,t0.field2 FROM Flight as t0 WHERE t0.field1 < 15", sql);
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
