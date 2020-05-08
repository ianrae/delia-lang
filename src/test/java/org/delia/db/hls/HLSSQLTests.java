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
import org.delia.db.sql.StrCreator;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DeliaExceptionHelper;
import org.junit.Test;

/**
 * HLS = High Level SQL
 * 
 * 
 * @author Ian Rae
 *
 */
public class HLSSQLTests extends HLSTests {
	
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

		public HLSSQLGenerator(FactoryService factorySvc) {
			super(factorySvc);
		}

		public String buildSQL(HLSQueryStatement hls) {
			this.queryExp = hls.queryExp;

			HLSQuerySpan hlspan = hls.getMainHLSSpan();
			SQLCreator sc = new SQLCreator();
			//SELECT .. from .. ..join.. ..where.. ..order..
			sc.out("SELECT");
			genFields(sc, hlspan);
			sc.out("FROM %s", hlspan.mtEl.getTypeName());
			genJoin(sc, hlspan);
			genWhere(sc, hlspan);
			
			genOLO(sc, hlspan);

			return sc.sql();
		}

		private void genJoin(SQLCreator sc, HLSQuerySpan hlspan) {
			boolean needJoin = hlspan.subEl != null;
			if (! needJoin) {
				return;
			}
			
			if (hlspan.subEl.allFKs) {
				for(TypePair pair: hlspan.fromType.getAllFields()) {
					if (pair.type.isStructShape()) {
						DStructType pairType = (DStructType) pair.type;
						PrimaryKey pk = pairType.getPrimaryKey();
						PrimaryKey mainPk = hlspan.fromType.getPrimaryKey();
						String s = String.format("JOIN %s ON %s=%s", pair.type, mainPk.getFieldName(), pk.getFieldName());
						sc.out(s);
					}
				}
				
			}
		}
		private void addFKofJoins(HLSQuerySpan hlspan, List<String> fieldL) {
			boolean needJoin = hlspan.subEl != null;
			if (! needJoin) {
				return;
			}
			
			if (hlspan.subEl.allFKs) {
				for(TypePair pair: hlspan.fromType.getAllFields()) {
					if (pair.type.isStructShape()) {
						DStructType pairType = (DStructType) pair.type;
						PrimaryKey pk = pairType.getPrimaryKey();
						fieldL.add(pk.getFieldName());
					}
				}
			}
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
				sc.out("ORDER BY %s", hlspan.oloEl.orderBy);
			}
			
			if (hlspan.oloEl.limit != null) {
				sc.out("LIMIT %s", hlspan.oloEl.limit.toString());
			} else if (needLimit1) {
				sc.out("LIMIT 1");
			}
			
			if (hlspan.oloEl.offset != null) {
				sc.out("OFFSET %s", hlspan.oloEl.offset.toString());
			}
			
			// TODO Auto-generated method stub
			
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
				sc.out("WHERE ID=%s", s);
			}
		}

		private void genFields(SQLCreator sc, HLSQuerySpan hlspan) {
			List<String> fieldL = new ArrayList<>();
			
			if (hlspan.hasFunction("first")) {
				sc.out("TOP 1");
			}
			
			if (hlspan.fEl != null) {
				String fieldName = hlspan.fEl.getFieldName();
				if (hlspan.hasFunction("count")) {
					String s = String.format("COUNT(%s)", fieldName);
					fieldL.add(s);
				} else if (hlspan.hasFunction("min")) {
					String s = String.format("MIN(%s)", fieldName);
					fieldL.add(s);
				} else if (hlspan.hasFunction("max")) {
					String s = String.format("MAX(%s)", fieldName);
					fieldL.add(s);
				} else if (hlspan.hasFunction("distinct")) {
					String s = String.format("DISTINCT(%s)", fieldName);
					fieldL.add(s);
				} else if (hlspan.hasFunction("exists")) {
					String s = String.format("COUNT(%s)", fieldName);
					fieldL.add(s);
				} else {
					fieldL.add(fieldName);
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
			for(TypePair pair: fromType.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
					if (!relinfo.isParent && !RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
						fieldL.add(pair.name);
					}
				} else {
					fieldL.add(pair.name);
				}
			}
		}

		public void setRegistry(DTypeRegistry registry) {
			this.registry = registry;
			this.queryTypeDetector = new QueryTypeDetector(factorySvc, registry);
		}

	}




		@Test
		public void testOneSpanNoSubSQL() {
			sqlchk("let x = Flight[55]", "SELECT * FROM Flight WHERE ID=55");
			sqlchk("let x = Flight[55].count()", "SELECT COUNT(*) FROM Flight WHERE ID=55");
			sqlchk("let x = Flight[55].first()", "SELECT TOP 1 * FROM Flight WHERE ID=55");
			sqlchk("let x = Flight[true]", "SELECT * FROM Flight");

			sqlchk("let x = Flight[55].field1", "SELECT field1 FROM Flight WHERE ID=55");
			sqlchk("let x = Flight[55].field1.min()", "SELECT MIN(field1) FROM Flight WHERE ID=55");
			sqlchk("let x = Flight[55].field1.orderBy('field2')", "SELECT field1 FROM Flight WHERE ID=55 ORDER BY field2");
			sqlchk("let x = Flight[55].field1.orderBy('field2').offset(3)", "SELECT field1 FROM Flight WHERE ID=55 ORDER BY field2 OFFSET 3");
			sqlchk("let x = Flight[55].field1.orderBy('field2').offset(3).limit(5)", "SELECT field1 FROM Flight WHERE ID=55 ORDER BY field2 LIMIT 5 OFFSET 3");
			sqlchk("let x = Flight[55].field1.count()", "SELECT COUNT(field1) FROM Flight WHERE ID=55");
			sqlchk("let x = Flight[55].field1.distinct()", "SELECT DISTINCT(field1) FROM Flight WHERE ID=55");
			sqlchk("let x = Flight[55].field1.exists()", "SELECT COUNT(field1) FROM Flight WHERE ID=55 LIMIT 1");

		}
		
		@Test
		public void testOneSpanSubSQL() {
			useCustomerSrc = true;
			sqlchk("let x = Customer[55].fks()", "SELECT id,x,id FROM Customer JOIN Address ON id=id WHERE ID=55");
	//		chk("let x = Customer[true].fetch('addr')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr}");
	//		
	//		chk("let x = Customer[true].fetch('addr').first()", "{Customer->Customer,MT:Customer,[true],(first),SUB:false,addr}");
	//		chk("let x = Customer[true].fetch('addr').orderBy('id')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr,OLO:id,null,null}");
	//
	//		//this one doesn't need to do fetch since just getting x
	//		chk("let x = Customer[true].x.fetch('addr')", "{Customer->int,MT:Customer,[true],F:x,()}");
	//		
	//		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
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

		sqlchk("let x = Customer[55].fks()", "SELECT id,x,id FROM Customer JOIN Address ON id=id WHERE ID=55");
		//		chk("let x = Customer[true].fetch('addr')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr}");
		//		
		//		chk("let x = Customer[true].fetch('addr').first()", "{Customer->Customer,MT:Customer,[true],(first),SUB:false,addr}");
		//		chk("let x = Customer[true].fetch('addr').orderBy('id')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr,OLO:id,null,null}");
		//
		//		//this one doesn't need to do fetch since just getting x
		//		chk("let x = Customer[true].x.fetch('addr')", "{Customer->int,MT:Customer,[true],F:x,()}");
		//		
		//		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
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
