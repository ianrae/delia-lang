package org.delia.db.hls;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.hls.HLSTests.HLSQueryStatement;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.StrCreator;
import org.delia.type.DTypeRegistry;
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
			StrCreator sc = new StrCreator();
			//SELECT .. from .. ..join.. ..where.. ..order..
			sc.o("SELECT ");
			genFields(sc, hlspan);
			sc.o(" FROM %s", hlspan.mtEl.getTypeName());
			genWhere(sc, hlspan);

			return sc.str;
		}

		private void genWhere(StrCreator sc, HLSQuerySpan hlspan) {
			QuerySpec spec = new QuerySpec();
			spec.queryExp = queryExp;
			QueryType queryType = queryTypeDetector.detectQueryType(spec);

			String s = "";
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
				sc.o(" WHERE ID=%s", s);
			}
		}

		private void genFields(StrCreator sc, HLSQuerySpan hlspan) {
			List<String> fieldL = new ArrayList<>();

			if (hlspan.fEl != null) {
				fieldL.add(hlspan.fEl.getFieldName());
			}

			if (fieldL.isEmpty()) {
				fieldL.add("*");
			}

			StringJoiner joiner = new StringJoiner(",");
			for(String s: fieldL) {
				joiner.add(s);
			}
			sc.o(joiner.toString());
		}

		public void setRegistry(DTypeRegistry registry) {
			this.registry = registry;
			this.queryTypeDetector = new QueryTypeDetector(factorySvc, registry);
		}

	}




	//	@Test
	//	public void testOneSpanNoSub() {
	//		chk("let x = Flight[true]", "{Flight->Flight,MT:Flight,[true],()}");
	//		chk("let x = Flight[55]", "{Flight->Flight,MT:Flight,[55],()}");
	//		
	//		chk("let x = Flight[55].field1", "{Flight->int,MT:Flight,[55],F:field1,()}");
	////		chk("let x = Flight[55].field1", "{Flight->Flight,MT:Flight,FIL:Flight[55],[]}");
	//		chk("let x = Flight[55].field1.min()", "{Flight->int,MT:Flight,[55],F:field1,(min)}");
	//		chk("let x = Flight[55].field1.orderBy('min')", "{Flight->int,MT:Flight,[55],F:field1,(),OLO:min,null,null}");
	//		chk("let x = Flight[55].field1.orderBy('min').offset(3)", "{Flight->int,MT:Flight,[55],F:field1,(),OLO:min,null,3}");
	//		chk("let x = Flight[55].field1.orderBy('min').offset(3).limit(5)", "{Flight->int,MT:Flight,[55],F:field1,(),OLO:min,5,3}");
	//		
	//		chk("let x = Flight[55].count()", "{Flight->long,MT:Flight,[55],(count)}");
	//		chk("let x = Flight[55].field1.count()", "{Flight->long,MT:Flight,[55],F:field1,(count)}");
	//		chk("let x = Flight[55].field1.distinct()", "{Flight->int,MT:Flight,[55],F:field1,(distinct)}");
	//		chk("let x = Flight[55].field1.exists()", "{Flight->boolean,MT:Flight,[55],F:field1,(exists)}");
	//		chk("let x = Flight[55].first()", "{Flight->Flight,MT:Flight,[55],(first)}");
	//	}
	//	
	//	@Test
	//	public void testOneSpanSub() {
	//		useCustomerSrc = true;
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
	//	}
	//	
	//	@Test
	//	public void testOneRelation() {
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
	public void testDebug() {

		sqlchk("let x = Flight[55]", "SELECT * FROM Flight WHERE ID=55");

		useCustomerSrc = true;
		//		chk("let x = Customer[true].fks()", "{Customer->Customer,MT:Customer,[true],(fks),SUB:true}");
		//		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
		//		chk("let x = Customer[true].addr.fks()", "{Customer->Customer,MT:Customer,[true],()},{Address->Address,MT:Address,R:addr,(),SUB:true}");
		//		chk("let x = Customer[true].fks().addr", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,()}");
		//		chk("let x = Customer[true].fks().addr.fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,(),SUB:true}");

		//		chk("let x = Customer[true].orderBy('id').addr.orderBy('y')", "{Customer->Customer,MT:Customer,[true],(),OLO:id,null,null},{Address->Address,MT:Address,R:addr,(),OLO:y,null,null}");
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
