package org.delia.db.hls;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AliasManagerTests extends HLSTestBase {
	
	@Test
	public void testOneSpanSubSQL() {
		useCustomer11Src = true;
		
		aliasChk("let x = Customer[55].fks()", "a=Customer,b=.addr");
		
		aliasChk("let x = Customer[true].fetch('addr')", "a=Customer,b=.addr");
		aliasChk("let x = Customer[addr < 111].fks()", "a=Customer,b=.addr");
	}


	private void aliasChk(String src, String expected) {
		HLSQueryStatement hls = buildHLS(src);
		HLSQuerySpan hlspan = hls.hlspanL.get(0);
		
		AliasManager aliasMgr = new AliasManager(delia.getFactoryService());
		aliasMgr.buildAliases(hlspan, session.getDatIdMap());
		String s = aliasMgr.dumpToString();
		log.log(s);
		assertEquals(expected, s);
	}


	@Test
	public void testDebugSQL() {
		useCustomer11Src = true;

	}

	//---
	
	@Before
	public void init() {
		createDao();
	}

	private void sqlchk(String src, String sqlExpected) {
		sqlchkP(src, sqlExpected, null);
	}
	private void sqlchkP(String src, String sqlExpected, String param1) {
		doSqlchkP(src, sqlExpected, param1);
	}
}
