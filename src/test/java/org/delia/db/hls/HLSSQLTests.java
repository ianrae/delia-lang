package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
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
	
	public static class AssocTblManager {
		public boolean flip = false;
		
		public String getTableFor(DStructType type1, DStructType type2) {
			return flip ? "AddressCustomerAssoc" : "CustomerAddressAssoc"; //type1 on left
		}
		public boolean isFlipped() {
			return flip;
		}
	}
	

	public static class SqlJoinHelper {
		private AliasAllocator aliasAlloc;
		private AssocTblManager assocTblMgr;
		
		public SqlJoinHelper(AliasAllocator aliasAlloc, AssocTblManager assocTblMgr) {
			this.aliasAlloc = aliasAlloc;
			this.assocTblMgr = assocTblMgr;
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
					bHasFK = relinfoA.isParent;
					break;
				case MANY_TO_MANY:
					doManyToMany(sc, hlspan, pair, relinfoA);
					return;
				}
				
				String s;
				if (bHasFK) {
					DStructType pairType = (DStructType) pair.type; //Address
					RelationInfo relinfoB = findOtherSide(pairType, hlspan.fromType);
					PrimaryKey pk = pairType.getPrimaryKey();
//					PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
					
					String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
					String on1 = aliasAlloc.buildAlias(hlspan.fromType, pk.getKey()); //a.id
					String on2 = aliasAlloc.buildAlias(pairType, relinfoB.fieldName); //b.cust
					s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
				} else {
					DStructType pairType = (DStructType) pair.type; //Address
					RelationInfo relinfoB = findOtherSide(pairType, hlspan.fromType);
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

		private void doManyToMany(SQLCreator sc, HLSQuerySpan hlspan, TypePair pair, RelationInfo relinfoA) {
			String s;
			PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
			String assocTable = assocTblMgr.getTableFor(hlspan.fromType, (DStructType) pair.type); //"CustomerAddressAssoc"; //TODO fix
			boolean flipLeftRight = assocTblMgr.isFlipped();
			if (hlspan.doubleFlip) {
				flipLeftRight = !flipLeftRight;
			}
			
			if (flipLeftRight) {
				String tbl1 = aliasAlloc.buildTblAliasAssoc(assocTable);
				String on1 = aliasAlloc.buildAliasAssoc(hlspan.fromType.getName(), mainPk.getFieldName()); //b.cust
				String on2 = aliasAlloc.buildAliasAssoc(assocTable, "rightv"); //a.id
				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
			} else {
				// SELECT a.x,c.leftv FROM Customer as a zzLEFT JOIN AddressCustomerAssoc as c ON a.id=c.rightv
				String tbl1 = aliasAlloc.buildTblAliasAssoc(assocTable);
				String on1 = aliasAlloc.buildAliasAssoc(hlspan.fromType.getName(), mainPk.getFieldName()); //b.cust
				String on2 = aliasAlloc.buildAliasAssoc(assocTable, "leftv"); //a.id
				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
			}
			
			sc.out(s);
			
			//and now 2nd join of Address table
			List<TypePair> fullJoinL = genFullJoinList(hlspan);
			boolean found = false;
			for(TypePair tt: fullJoinL) {
				if (tt.name.equals(pair.name) && tt.type.equals(pair.type)) {
					found = true;
				}
			}
			if (! found) {
				return;
			}
			
			s = null;
			
			if (flipLeftRight) {
				DStructType pairType = (DStructType) pair.type; //Address
//				RelationInfo relinfoB = findOtherSide(pairType, hlspan.fromType);
				PrimaryKey pk = pairType.getPrimaryKey();
//				PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
				
				String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
				String on1 = aliasAlloc.buildAlias(pairType, pk.getFieldName()); //b.id
				String on2 = aliasAlloc.buildAliasAssoc(assocTable, "rigthv"); //c.rightv
				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
			} else {
				DStructType pairType = (DStructType) pair.type; //Address
//				RelationInfo relinfoB = findOtherSide(pairType, hlspan.fromType);
				PrimaryKey pk = pairType.getPrimaryKey();
//				PrimaryKey mainPk = hlspan.fromType.getPrimaryKey(); //Customer
				
				String tbl1 = aliasAlloc.buildTblAlias((DStructType) pair.type);
				String on1 = aliasAlloc.buildAlias(pairType, pk.getFieldName()); //b.id
				String on2 = aliasAlloc.buildAliasAssoc(assocTable, "rigthv"); //c.leftv
				s = String.format("LEFT JOIN %s ON %s=%s", tbl1, on1, on2);
			}
			sc.out(s);
		}

		private RelationInfo findOtherSide(DStructType pairType, DStructType fromType) {
			RelationInfo relinfo = DRuleHelper.findOtherSideOne(pairType, fromType);
			if (relinfo != null) {
				return relinfo;
			}

			relinfo = DRuleHelper.findOtherSideMany(pairType, fromType);
			if (relinfo != null) {
				return relinfo;
			}
			//err!!
			return null;
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
				DStructType pairType = (DStructType) pair.type;
				PrimaryKey pk = pairType.getPrimaryKey();
				
				RelationInfo relinfoA = DRuleHelper.findMatchingRuleInfo(hlspan.fromType, pair);
				switch(relinfoA.cardinality) {
				case ONE_TO_ONE:
				case ONE_TO_MANY:
					break;
				case MANY_TO_MANY:
					doManyToManyAddFKofJoins(hlspan, fieldL, pair, relinfoA);
					return;
				}
				
				//					fieldL.add(pk.getFieldName());
				fieldL.add(aliasAlloc.buildAlias(pairType, pk.getFieldName()));
			}
		}
		private void doManyToManyAddFKofJoins(HLSQuerySpan hlspan, List<String> fieldL, TypePair pair,
				RelationInfo relinfoA) {
			String assocTbl = assocTblMgr.getTableFor(hlspan.fromType, (DStructType) pair.type);
			String fieldName = assocTblMgr.isFlipped() ? "leftv" : "rightv";
			fieldL.add(aliasAlloc.buildAliasAssoc(assocTbl, fieldName));
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
		
		public List<TypePair> genTwoStatementJoinList(HLSQuerySpan hlspan1, HLSQuerySpan hlspan2, SQLCreator sc) {
			List<TypePair> joinL = new ArrayList<>();

			boolean needJoin = true; //hlspan.subEl != null;
			if (! needJoin) {
				return joinL;
			}

			
			//1 is address
			//2 is customer
			
			String targetTypeName = hlspan2.fromType.getName();
			for(TypePair pair: hlspan1.fromType.getAllFields()) {
				if (pair.type.isStructShape()) {
					if (pair.type.getName().equals(targetTypeName)) {
						
						RelationInfo relinfoA = DRuleHelper.findMatchingRuleInfo(hlspan1.fromType, pair);
						if (RelationCardinality.MANY_TO_MANY.equals(relinfoA.cardinality)) {
							TypePair tmp = new TypePair("xx", relinfoA.farType);
							doManyToMany(sc, hlspan1, tmp, relinfoA);
							
							joinL.add(pair);
						}
					}
				}
			}
			
			
			
			
			return joinL;
		}
	}
	
	public static class HLSSQLGenerator extends ServiceBase {

		private DTypeRegistry registry;
		private QueryTypeDetector queryTypeDetector;
		private QueryExp queryExp;
		private AliasAllocator aliasAlloc = new AliasAllocator();
		private SqlJoinHelper joinHelper;

		public HLSSQLGenerator(FactoryService factorySvc, AssocTblManager assocTblMgr) {
			super(factorySvc);
			this.joinHelper = new SqlJoinHelper(aliasAlloc, assocTblMgr);
		}

		public String buildSQL(HLSQueryStatement hls) {
			this.queryExp = hls.queryExp;
			if (hls.hlspanL.size() == 1) {
				return processOneStatement(hls.getMainHLSSpan(), false);
			} else if (hls.hlspanL.size() == 2) {
				
				HLSQuerySpan hlspan1 = hls.hlspanL.get(1); //Address
				if (hlspan1.filEl.isAll()) {
					SUBElement subEl = new SUBElement();
					subEl.allFKs = true;
					hlspan1.subEl = subEl;
					hlspan1.doubleFlip = true;
					//1 - Address, 2 - Customer
					
					String sql = processOneStatement(hlspan1, false);
					return sql;
				} else {
					return "sss";
				}
//				String sql = processOneStatement(hlspan1, false);
//				String secondaryJoin = ""; //generateSecondaryJoin(hlspan1, hls.hlspanL.get(0));
//				
//				String ss = String.format("%s %s", sql, secondaryJoin);
//				return ss;
			} else {
				return null; //not supported
			}
		}
		
		private String generateSecondaryJoin(HLSQuerySpan hlspan1, HLSQuerySpan hlspan2) {
			//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv LEFT JOIN Customer as c WHERE b.leftv=c.id AND c.x > 10 

			SQLCreator sc = new SQLCreator();
			List<TypePair> joinL = joinHelper.genTwoStatementJoinList(hlspan1, hlspan2, sc);
			//Address.cust
			
			
			
			return sc.sql();
		}

		public String processOneStatement(HLSQuerySpan hlspan, boolean forceAllFields) {
			SQLCreator sc = new SQLCreator();
			//SELECT .. from .. ..join.. ..where.. ..order..
			sc.out("SELECT");
			genFields(sc, hlspan, forceAllFields);
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

		private void genFields(SQLCreator sc, HLSQuerySpan hlspan, boolean forceAllFields) {
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

			if (forceAllFields) {
				addStructFields(hlspan.fromType, fieldL);
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
		assocTblMgr.flip = false;
		sqlchk("let x = Customer[55].fks()", 					"SELECT a.id,a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.leftv WHERE a.ID=55");
		sqlchk("let x = Customer[true].fetch('addr')", 			"SELECT a.id,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.id=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv");
		sqlchk("let x = Customer[true].fetch('addr').first()", 	"SELECT TOP 1 a.id,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.id=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv");
		sqlchk("let x = Customer[true].fetch('addr').orderBy('id')", "SELECT a.id,a.x,b.id,b.y FROM Customer as a LEFT JOIN CustomerAddressAssoc as c ON a.id=c.leftv LEFT JOIN Address as b ON b.id=c.rigthv ORDER BY a.id");
		sqlchk("let x = Customer[true].x.fetch('addr')", 		"SELECT a.x FROM Customer as a");
		sqlchk("let x = Customer[true].x.fks()", 				"SELECT a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.leftv");
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
	public void testAssocTableFlip() {
		useCustomerSrc = true;
		assocTblMgr.flip = true;
		sqlchk("let x = Customer[true].x.fks()", "SELECT a.x,b.leftv FROM Customer as a LEFT JOIN AddressCustomerAssoc as b ON a.id=b.rightv");
		
		assocTblMgr.flip = false;
		sqlchk("let x = Customer[true].x.fks()", "SELECT a.x,b.rightv FROM Customer as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.leftv");
	}

	
	
	@Test
	public void testDebugSQL() {
		useCustomerSrc = true;
		assocTblMgr.flip = false;
		
		//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv 
		sqlchk("let x = Customer[true].addr", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,()}");

		//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv WHERE b.leftv=55 
//		sqlchk("let x = Customer[55].addr", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,()}");

		//SELECT a.id,a.y FROM Address as a LEFT JOIN CustomerAddressAssoc as b ON a.id=b.rightv LEFT JOIN Customer as c WHERE b.leftv=c.id AND c.x > 10 
//		sqlchk("let x = Customer[x > 10].addr", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,()}");
		
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
		
	}


	@Before
	public void init() {
		createDao();
	}



	private void sqlchk(String src, String sqlExpected) {
		HLSSQLGenerator gen = new HLSSQLGenerator(delia.getFactoryService(), assocTblMgr);
		HLSQueryStatement hls = buildHLS(src);
		gen.setRegistry(session.getExecutionContext().registry);
		String sql = gen.buildSQL(hls);
		log.log("sql: " + sql);
		assertEquals(sqlExpected, sql);
	}



}
