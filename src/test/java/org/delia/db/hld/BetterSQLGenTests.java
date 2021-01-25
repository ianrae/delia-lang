package org.delia.db.hld;

import static org.junit.Assert.assertEquals;

import org.delia.db.SqlStatement;
import org.delia.hld.HLDQueryStatement;
import org.delia.hld.cud.HLDDeleteStatement;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.hld.simple.SimpleDelete;
import org.delia.hld.simple.SimpleSelect;
import org.delia.hld.simple.SimpleSqlBuilder;
import org.delia.hld.simple.SimpleSqlGenerator;
import org.delia.hld.simple.SimpleUpdate;
import org.junit.Test;

public class BetterSQLGenTests extends NewHLSTestBase {

	@Test
	public void testSelect() {
		useCustomer11Src = true;
		String src = "let x = Customer[55]";

		HLDQueryStatement hld = buildFromSrc(src, 0); 
		SimpleSqlBuilder builder = new SimpleSqlBuilder();
		SimpleSelect sel = builder.buildFrom(hld.hldquery);
		
		SimpleSqlGenerator gen = new SimpleSqlGenerator(this.session.getExecutionContext().registry, delia.getFactoryService());
		SqlStatement stm = new SqlStatement(null);
		String sql = gen.gen(sel, stm);
		log.log(sql);
		assertEquals("SELECT t0.cid, t0.x FROM Customer as t0 WHERE t0.cid=?", sql);
	}
	
	@Test
	public void testDelete() {
		useCustomer11Src = true;
		String src = "delete Customer[x > 10]";

		HLDDeleteStatement hld = buildFromSrcDelete(src, 0); 
		SimpleSqlBuilder builder = new SimpleSqlBuilder();
		SimpleDelete sel = builder.buildFrom(hld.hlddelete);
		
		SimpleSqlGenerator gen = new SimpleSqlGenerator(this.session.getExecutionContext().registry, delia.getFactoryService());
		SqlStatement stm = new SqlStatement(null);
		String sql = gen.gen(sel, stm);
		log.log(sql);
		assertEquals("DELETE FROM Customer as t0 WHERE t0.x > ?", sql);
	}
	
	@Test
	public void testUpdate() {
		useCustomer1NSrc = true;
		String src = "update Customer[1] {x: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SimpleSqlBuilder builder = new SimpleSqlBuilder();
		SimpleUpdate sel = builder.buildFrom(hldupdate.hldupdate);
		
		SimpleSqlGenerator gen = new SimpleSqlGenerator(this.session.getExecutionContext().registry, delia.getFactoryService());
		SqlStatement stm = new SqlStatement(null);
		String sql = gen.gen(sel, stm);
		log.log(sql);
		assertEquals("UPDATE Customer SET x = ? WHERE cid=?", sql);
	}
	
}
