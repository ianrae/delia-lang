package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.StrCreator;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * HLS = High Level SQL
 * 
 * 
 * @author Ian Rae
 *
 */
public class HLSSQLTests extends HLSTestBase {

	public static class SqlJoinHelper {
		
		private AliasAllocator aliasAlloc;
		
		public SqlJoinHelper(AliasAllocator aliasAlloc) {
			this.aliasAlloc = aliasAlloc;
		}

		public void genJoin(SQLCreator sc, HLSQuerySpan hlspan) {
			List<TypePair> joinL = genJoinList(hlspan);

			//do the joins
			for(TypePair pair: joinL) {
				boolean bHasFK = false;
				RelationInfo relinfoA = DRuleHelper.findMatchingRuleInfo(hlspan.fromType, pair);
				switch(relinfoA.cardinality) {
				case ONE_TO_ONE:
					bHasFK = relinfoA.isParent;
					break;
				case ONE_TO_MANY:
					bHasFK = true;
					break;
				case MANY_TO_MANY:
					break;
				}
				
				String s;
				if (bHasFK) {
					DStructType pairType = (DStructType) pair.type; //Address
					RelationInfo relinfoB = DRuleHelper.findOtherSideOne(pairType, hlspan.fromType);
					PrimaryKey pk = pairType.getPrimaryKey();
//					PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
					
					String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
					String on1 = aliasAlloc.buildAlias(hlspan.fromType, pk.getKey()); //a.id
					String on2 = aliasAlloc.buildAlias(pairType, relinfoB.fieldName); //b.cust
					s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
				} else {
					DStructType pairType = (DStructType) pair.type; //Address
					RelationInfo relinfoB = DRuleHelper.findOtherSideOne(pairType, hlspan.fromType);
					PrimaryKey pk = pairType.getPrimaryKey();
//					PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
					
					String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
					String on1 = aliasAlloc.buildAlias(hlspan.fromType, relinfoA.fieldName); //a.addr
					String on2 = aliasAlloc.buildAlias(pairType, pk.getKey()); //b.id
					s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
				}
				
				sc.out(s);
			}
		}

		private List<TypePair> genJoinList(HLSQuerySpan hlspan) {
			List<TypePair> joinL = genFullJoinList(hlspan);
			List<TypePair> join2L = genFKJoinList(hlspan);
			joinL.addAll(join2L);
			return joinL;
		}
		private List<TypePair> genFullJoinList(HLSQuerySpan hlspan) {
			List<TypePair> joinL = new ArrayList<>();

			boolean needJoin = hlspan.subEl != null;
			if (! needJoin) {
				return joinL;
			}

			for (String fieldName: hlspan.subEl.fetchL) {
				if (joinL.contains(fieldName)) {
					continue;
				}
				TypePair pair = DValueHelper.findField(hlspan.fromType, fieldName);
				joinL.add(pair);
			}

			//TODO: later to fk(field)
			return joinL;
		}
		private List<TypePair> genFKJoinList(HLSQuerySpan hlspan) {
			List<TypePair> joinL = new ArrayList<>();

			boolean needJoin = hlspan.subEl != null;
			if (! needJoin) {
				return joinL;
			}

			if (hlspan.subEl.allFKs) {
				for(TypePair pair: hlspan.fromType.getAllFields()) {
					if (pair.type.isStructShape()) {
						joinL.add(pair);
					}
				}
			}

			//TODO: later to fk(field)
			return joinL;
		}

		public void addFKofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
			List<TypePair> joinL = genFKJoinList(hlspan);

			for(TypePair pair: joinL) {
				if (pair.type.isStructShape()) {
					DStructType pairType = (DStructType) pair.type;
					PrimaryKey pk = pairType.getPrimaryKey();
//					fieldL.add(pk.getFieldName());
					fieldL.add(aliasAlloc.buildAlias(pairType, pk.getFieldName()));
				}
			}
		}
		public void addFullofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
			List<TypePair> joinL = genFullJoinList(hlspan);

			for(TypePair pair: joinL) {
				addStructFields((DStructType) pair.type, fieldL);
			}
		}
		public void addStructFields(DStructType fromType, List<String> fieldL) {
			for(TypePair pair: fromType.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
					if (!relinfo.isParent && !RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
						fieldL.add(aliasAlloc.buildAlias(fromType, pair.name));
					}
				} else {
					fieldL.add(aliasAlloc.buildAlias(fromType, pair.name));
				}
			}
		}
	}
	
	public static class AliasAllocator {
		protected int nextAliasIndex = 0;
		private Map<DStructType,String> aliasMap = new HashMap<>();
		
		public AliasAllocator() {
		}
		
		public void createAlias(DStructType structType) {
			char ch = (char) ('a' + nextAliasIndex++);
			String s = String.format("%c", ch);
			aliasMap.put(structType, s);
		}

		public String findOrCreateFor(DStructType structType) {
			if (! aliasMap.containsKey(structType)) {
				createAlias(structType);
			}
			return aliasMap.get(structType);
		}
		public String buildTblAlias(DStructType structType) {
			String alias = findOrCreateFor(structType);
			String s = String.format("%s as %s", structType.getName(), alias);
			return s;
		}
		public String buildAlias(DStructType pairType, TypePair pair) {
			String alias = findOrCreateFor(pairType);
			String s = String.format("%s.%s", alias, pair.name);
			return s;
		}
		public String buildAlias(DStructType pairType, String fieldName) {
			String alias = findOrCreateFor(pairType);
			String s = String.format("%s.%s", alias, fieldName);
			return s;
		}


	}
	

	public static class SQLCreator {
		private StrCreator sc = new StrCreator();
		private boolean isPrevious = false;

		public String out(String fmt, String...args) {
			if (isPrevious) {
				sc.o(" ");
			}
			String s = sc.o(fmt, args);
			isPrevious = true;
			return s;
		}

		public String sql() {
			return sc.str;
		}
	}

	public static class HLSSQLGenerator extends ServiceBase {

		private DTypeRegistry registry;
		private QueryTypeDetector queryTypeDetector;
		private QueryExp queryExp;
		private AliasAllocator aliasAlloc = new AliasAllocator();
		private SqlJoinHelper joinHelper;

		public HLSSQLGenerator(FactoryService factorySvc) {
			super(factorySvc);
			this.joinHelper = new SqlJoinHelper(aliasAlloc);

		}

		public String buildSQL(HLSQueryStatement hls) {
			this.queryExp = hls.queryExp;

			HLSQuerySpan hlspan = hls.getMainHLSSpan();
			SQLCreator sc = new SQLCreator();
			//SELECT .. from .. ..join.. ..where.. ..order..
			sc.out("SELECT");
			genFields(sc, hlspan);
			sc.out("FROM %s", buildTblAlias(hlspan.mtEl.structType));
			genJoin(sc, hlspan);
			genWhere(sc, hlspan);

			genOLO(sc, hlspan);

			return sc.sql();
		}

		private String buildTblAlias(DStructType structType) {
			return aliasAlloc.buildTblAlias(structType);
		}
		private String buildAlias(DStructType pairType, TypePair pair) {
			return aliasAlloc.buildAlias(pairType, pair);
		}
		private String buildAlias(DStructType pairType, String fieldName) {
			return aliasAlloc.buildAlias(pairType, fieldName);
		}

		private void genJoin(SQLCreator sc, HLSQuerySpan hlspan) {
			joinHelper.genJoin(sc, hlspan);
		}
		private void addFKofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
			joinHelper.addFKofJoins(hlspan, fieldL);
		}
		private void addFullofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
			joinHelper.addFullofJoins(hlspan, fieldL);
		}


		private void genOLO(SQLCreator sc, HLSQuerySpan hlspan) {
			boolean needLimit1 = hlspan.hasFunction("exists");

			if (hlspan.oloEl == null) {
				if (needLimit1) {
					sc.out("LIMIT 1");
				}
				return;
			}

			if (hlspan.oloEl.orderBy != null) {
				String ss = buildAlias(hlspan.fromType, hlspan.oloEl.orderBy);
				sc.out("ORDER BY %s",ss);
			}

			if (hlspan.oloEl.limit != null) {
				sc.out("LIMIT %s", hlspan.oloEl.limit.toString());
			} else if (needLimit1) {
				sc.out("LIMIT 1");
			}

			if (hlspan.oloEl.offset != null) {
				sc.out("OFFSET %s", hlspan.oloEl.offset.toString());
			}
		}

		private void genWhere(SQLCreator sc, HLSQuerySpan hlspan) {
			QuerySpec spec = new QuerySpec();
			spec.queryExp = queryExp;
			QueryType queryType = queryTypeDetector.detectQueryType(spec);

			String s = null;
			switch(queryType) {
			case ALL_ROWS:
				break;
			case PRIMARY_KEY:
				s = "55";//fix TODO
				break;
			case OP:
				DeliaExceptionHelper.throwError("notyetsupported-hls", "sdfds");
			}

			if (s != null) {
				sc.out("WHERE %s=%s", buildAlias(hlspan.fromType, "ID"), s);
			}
		}

		private void genFields(SQLCreator sc, HLSQuerySpan hlspan) {
			List<String> fieldL = new ArrayList<>();

			if (hlspan.hasFunction("first")) {
				sc.out("TOP 1");
			}

			boolean isJustFieldName = false;
			if (hlspan.fEl != null) {
				String fieldName = hlspan.fEl.getFieldName();
				String aa = buildAlias(hlspan.fromType, fieldName);
				if (hlspan.hasFunction("count")) {
					String s = String.format("COUNT(%s)", aa);
					fieldL.add(s);
				} else if (hlspan.hasFunction("min")) {
					String s = String.format("MIN(%s)", aa);
					fieldL.add(s);
				} else if (hlspan.hasFunction("max")) {
					String s = String.format("MAX(%s)", aa);
					fieldL.add(s);
				} else if (hlspan.hasFunction("distinct")) {
					String s = String.format("DISTINCT(%s)", aa);
					fieldL.add(s);
				} else if (hlspan.hasFunction("exists")) {
					String s = String.format("COUNT(%s)", aa);
					fieldL.add(s);
				} else {
					fieldL.add(aa);
					isJustFieldName = true;
				}
			} else  {
				if (hlspan.hasFunction("count")) {
					String s = String.format("COUNT(*)");
					fieldL.add(s);
				} else if (hlspan.hasFunction("exists")) {
					String s = String.format("COUNT(*)");
					fieldL.add(s);
				}
			}

			boolean needJoin = hlspan.subEl != null;
			if (needJoin && fieldL.isEmpty()) {
				addStructFields(hlspan.fromType, fieldL);
				addFKofJoins(hlspan, fieldL);
				addFullofJoins(hlspan, fieldL);
			} else if (isJustFieldName) {
				addFKofJoins(hlspan, fieldL);
			}

			if (fieldL.isEmpty()) {
				fieldL.add("*");
			}

			StringJoiner joiner = new StringJoiner(",");
			for(String s: fieldL) {
				joiner.add(s);
			}
			sc.out(joiner.toString());
		}

		private void addStructFields(DStructType fromType, List<String> fieldL) {
			joinHelper.addStructFields(fromType, fieldL);
		}

		public void setRegistry(DTypeRegistry registry) {
			this.registry = registry;
			this.queryTypeDetector = new QueryTypeDetector(factorySvc, registry);
		}
	}


	@Test
	public void testOneSpanNoSubSQL() {
		sqlchk("let x = Flight[55]", 			"SELECT * FROM Flight as a WHERE a.ID=55");
		sqlchk("let x = Flight[55].count()", 	"SELECT COUNT(*) FROM Flight as a WHERE a.ID=55");
		sqlchk("let x = Flight[55].first()", 	"SELECT TOP 1 * FROM Flight as a WHERE a.ID=55");
		sqlchk("let x = Flight[true]", 			"SELECT * FROM Flight as a");

		sqlchk("let x = Flight[55].field1", 						"SELECT a.field1 FROM Flight as a WHERE a.ID=55");
		sqlchk("let x = Flight[55].field1.min()", 					"SELECT MIN(a.field1) FROM Flight as a WHERE a.ID=55");
		sqlchk("let x = Flight[55].field1.orderBy('field2')", 		"SELECT a.field1 FROM Flight as a WHERE a.ID=55 ORDER BY a.field2");
		sqlchk("let x = Flight[55].field1.orderBy('field2').offset(3)", "SELECT a.field1 FROM Flight as a WHERE a.ID=55 ORDER BY a.field2 OFFSET 3");
		sqlchk("let x = Flight[55].field1.orderBy('field2').offset(3).limit(5)", "SELECT a.field1 FROM Flight as a WHERE a.ID=55 ORDER BY a.field2 LIMIT 5 OFFSET 3");
		sqlchk("let x = Flight[55].field1.count()", 				"SELECT COUNT(a.field1) FROM Flight as a WHERE a.ID=55");
		sqlchk("let x = Flight[55].field1.distinct()", 				"SELECT DISTINCT(a.field1) FROM Flight as a WHERE a.ID=55");
		sqlchk("let x = Flight[55].field1.exists()", 				"SELECT COUNT(a.field1) FROM Flight as a WHERE a.ID=55 LIMIT 1");

	}

	@Test
	public void testOneSpanSubSQL() {
		useCustomerSrc = true;
		sqlchk("let x = Customer[55].fks()", 					"SELECT a.id,a.x,b.id FROM Customer as a JOIN Address as b ON a.id=b.id WHERE a.ID=55");
		sqlchk("let x = Customer[true].fetch('addr')", 			"SELECT a.id,a.x,b.id,b.y FROM Customer as a JOIN Address as b ON a.id=b.id");
		sqlchk("let x = Customer[true].fetch('addr').first()", 	"SELECT TOP 1 a.id,a.x,b.id,b.y FROM Customer as a JOIN Address as b ON a.id=b.id");
		sqlchk("let x = Customer[true].fetch('addr').orderBy('id')", "SELECT a.id,a.x,b.id,b.y FROM Customer as a JOIN Address as b ON a.id=b.id ORDER BY a.id");
		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a");
		sqlchk("let x = Customer[true].x.fks()", 				"SELECT a.x,b.id FROM Customer as a JOIN Address as b ON a.id=b.id");
	}

	//	@Test
	//	public void testOneRelationSQL() {
	//		useCustomerSrc = true;
	//		chk("let x = Customer[true].addr", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,()}");
	//		
	//		chk("let x = Customer[true].fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true}");
	//		chk("let x = Customer[true].fetch('addr')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr}");
	//		
	//		chk("let x = Customer[true].fetch('addr').first()", "{Customer->Customer,MT:Customer,[true],(first),SUB:false,addr}");
	//		chk("let x = Customer[true].fetch('addr').orderBy('id')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr,OLO:id,null,null}");
	//
	//		//this one doesn't need to do fetch since just getting x
	//		chk("let x = Customer[true].x.fetch('addr')", "{Customer->int,MT:Customer,[true],F:x,()}");
	//		
	//		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
	//		
	//		chk("let x = Customer[true].addr.fks()", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),SUB:true}");
	//		chk("let x = Customer[true].fks().addr", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,()}");
	//		chk("let x = Customer[true].fks().addr.fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,(),SUB:true}");
	//		
	//		chk("let x = Customer[true].addr.orderBy('id')", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),OLO:id,null,null}");
	//		chk("let x = Customer[true].orderBy('id').addr", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,()}");
	//		chk("let x = Customer[true].orderBy('id').addr.orderBy('y')", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,(),OLO:y,null,null}");
	//	}


	@Test
	public void testDebugSQL() {

		useCustomerSrc = true;

		//		sqlchk("let x = Customer[55].fks()", "SELECT id,x,id FROM Customer JOIN Address ON id=id WHERE ID=55");
//		sqlchk("let x = Customer[true].fetch('addr')", "SELECT id,x,id,y FROM Customer JOIN Address ON id=id");
//		sqlchk("let x = Customer[true].fetch('addr').first()", "SELECT TOP 1 id,x,id,y FROM Customer JOIN Address ON id=id");
//		sqlchk("let x = Customer[true].fetch('addr').orderBy('id')", "SELECT id,x,id,y FROM Customer JOIN Address ON id=id ORDER BY id");
		//
		//		//this one doesn't need to do fetch since just getting x
//		sqlchk("let x = Customer[true].x.fetch('addr')", "SELECT x FROM Customer");
		//		
		sqlchk("let x = Customer[true].x.fks()", "SELECT a.x,b.id FROM Customer as a JOIN Address as b ON a.id=b.id");
	}

	//---
	
	@Before
	public void init() {
		createDao();
	}



	private void sqlchk(String src, String sqlExpected) {
		HLSSQLGenerator gen = new HLSSQLGenerator(delia.getFactoryService());
		HLSQueryStatement hls = buildHLS(src);
		gen.setRegistry(session.getExecutionContext().registry);
		String sql = gen.buildSQL(hls);
		log.log("sql: " + sql);
		assertEquals(sqlExpected, sql);
	}



}
