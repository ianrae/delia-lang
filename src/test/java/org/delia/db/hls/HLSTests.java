package org.delia.db.hls;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * HLS = High Level SQL
 * Yet another way to generate SQL.
 * A high-level representation of a Delia query
 * 
 * 
 * @author Ian Rae
 *
 */
public class HLSTests extends HLSTestBase {
	
	@Test
	public void testOneSpanNoSub() {
		chk("let x = Flight[true]", "{Flight->Flight,MT:Flight,[true],()}");
		chk("let x = Flight[55]", "{Flight->Flight,MT:Flight,[55],()}");
		
		chk("let x = Flight[55].field1", "{Flight->int,MT:Flight,[55],F:field1,()}");
//		chk("let x = Flight[55].field1", "{Flight->Flight,MT:Flight,FIL:Flight[55],[]}");
		chk("let x = Flight[55].field1.min()", "{Flight->int,MT:Flight,[55],F:field1,(min)}");
		chk("let x = Flight[55].field1.orderBy('field1')", "{Flight->int,MT:Flight,[55],F:field1,(),OLO:field1,null,null}");
		chk("let x = Flight[55].field1.orderBy('field1').offset(3)", "{Flight->int,MT:Flight,[55],F:field1,(),OLO:field1,null,3}");
		chk("let x = Flight[55].field1.orderBy('field1').offset(3).limit(5)", "{Flight->int,MT:Flight,[55],F:field1,(),OLO:field1,5,3}");
		
		chk("let x = Flight[55].count()", "{Flight->long,MT:Flight,[55],(count)}");
		chk("let x = Flight[55].field1.count()", "{Flight->long,MT:Flight,[55],F:field1,(count)}");
		chk("let x = Flight[55].field1.distinct()", "{Flight->int,MT:Flight,[55],F:field1,(distinct)}");
		chk("let x = Flight[55].field1.exists()", "{Flight->boolean,MT:Flight,[55],F:field1,(exists)}");
		chk("let x = Flight[55].first()", "{Flight->Flight,MT:Flight,[55],(first)}");
	}
	
	@Test
	public void testOneSpanSub() {
		useCustomerManyToManySrc = true;
		chk("let x = Customer[true].fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true}");
		chk("let x = Customer[true].fetch('addr')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr}");
		
		chk("let x = Customer[true].fetch('addr').first()", "{Customer->Customer,MT:Customer,[true],(first),SUB:false,addr}");
		chk("let x = Customer[true].fetch('addr').orderBy('cid')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr,OLO:cid,null,null}");

		//this one doesn't need to do fetch since just getting x
		chk("let x = Customer[true].x.fetch('addr')", "{Customer->int,MT:Customer,[true],F:x,()}");
		
		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
	}
	
	@Test
	public void testOneRelation() {
		useCustomerManyToManySrc = true;
		chk("let x = Customer[true].addr", "{Address->Address,MT:Address,[true],R:addr,()}");
		
		chk("let x = Customer[true].fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true}");
		chk("let x = Customer[true].fetch('addr')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr}");
		
		chk("let x = Customer[true].fetch('addr').first()", "{Customer->Customer,MT:Customer,[true],(first),SUB:false,addr}");
		chk("let x = Customer[true].fetch('addr').orderBy('cid')", "{Customer->Customer,MT:Customer,[true],(),SUB:false,addr,OLO:cid,null,null}");

		//this one doesn't need to do fetch since just getting x
		chk("let x = Customer[true].x.fetch('addr')", "{Customer->int,MT:Customer,[true],F:x,()}");
		
		chk("let x = Customer[true].x.fks()", "{Customer->int,MT:Customer,[true],F:x,(),SUB:true}");
		
		chk("let x = Customer[true].addr.fks()", "{Address->Address,MT:Address,[true],R:addr,(),SUB:true}");
		chk("let x = Customer[true].fks().addr", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,()}");
		chk("let x = Customer[true].fks().addr.fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,(),SUB:true}");
		
		chk("let x = Customer[true].addr.orderBy('id')", "{Address->Address,MT:Address,[true],R:addr,(),OLO:id,null,null}");
		chk("let x = Customer[true].orderBy('cid').addr", "{Customer->Customer,MT:Customer,[true],(),OLO:cid,null,null},{Address->Address,MT:Address,R:addr,()}");
		chk("let x = Customer[true].orderBy('cid').addr.orderBy('y')", "{Customer->Customer,MT:Customer,[true],(),OLO:cid,null,null},{Address->Address,MT:Address,R:addr,(),OLO:y,null,null}");
	}
	
	
	@Test
	public void testDebug() {
		
		useCustomerManyToManySrc = true;
		chk("let x = Customer[true].fks().addr.fks()", "{Customer->Customer,MT:Customer,[true],(),SUB:true},{Address->Address,MT:Address,R:addr,(),SUB:true}");
	}
	

	protected void chk(String src, String expected) {
		HLSQueryStatement hls = buildHLS(src);
		
		for(HLSQuerySpan hlspan: hls.hlspanL) {
			String hlstr = hlspan.toString();
			log.log(hlstr);
		}
//		assertEquals(1, hls.hlspanL.size());
		String hlstr = hls.toString();
		assertEquals(expected, hlstr);
	}


	//---
	
	@Before
	public void init() {
		createDao();
	}

}
