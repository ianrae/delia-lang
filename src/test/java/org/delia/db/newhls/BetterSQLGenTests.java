package org.delia.db.newhls;

import static org.junit.Assert.assertEquals;

import org.delia.db.newhls.cud.HLDDeleteStatement;
import org.delia.db.newhls.cud.HLDUpdateStatement;
import org.delia.db.newhls.simple.SimpleDelete;
import org.delia.db.newhls.simple.SimpleSelect;
import org.delia.db.newhls.simple.SimpleSqlBuilder;
import org.delia.db.newhls.simple.SimpleSqlGenerator;
import org.delia.db.newhls.simple.SimpleUpdate;
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
		String sql = gen.gen(sel);
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
		String sql = gen.gen(sel);
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
		String sql = gen.gen(sel);
		log.log(sql);
		assertEquals("UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", sql);
	}
	
}