package org.delia.db.newhls.cud;


import static org.junit.Assert.assertEquals;

import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.newhls.HLDManager;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.NewHLSTestBase;
import org.delia.db.sql.prepared.SqlStatement;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class DeleteTests extends NewHLSTestBase {
	
	@Test
	public void test1() {
		useCustomer11Src = true;
		String src = "delete Customer[55]";
		
		HLDDelete hldDelete = buildFromSrcDelete(src, 0); 
		chkDeleteSql(hldDelete, "DELETE FROM  Customer as t0 WHERE t0.cid=?", "55");
	}
	@Test
	public void test2() {
		useCustomer11Src = true;
		String src = "delete Address[100]";
		
		HLDDelete hldDelete = buildFromSrcDelete(src, 0); 
		chkDeleteSql(hldDelete, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}
	
	//-------------------------
	private boolean use11TwoAddr;
	
	@Override
	protected String buildSrc() {
		if (use11TwoAddr) {
			String src = " type Customer struct {cid int unique, x int, relation addr1 Address 'a1' one optional parent, relation addr2 Address 'a2' one optional parent } end";
			src += "\n type Address struct {id int unique, y int, relation cust1 Customer 'a1' one optional, relation cust2 Customer 'a2' one optional } end";
			return src;
		} else {
			return super.buildSrc();
		}
	}
	protected HLDDelete buildFromSrcDelete(String src, int expectedJoins) {
		DeleteStatementExp deleteExp = compileToDeleteStatement(src);
		QueryExp queryExp = deleteExp.queryExp;
		log.log(src);
		
		mgr = new HLDManager(this.session.getExecutionContext().registry, delia.getFactoryService(), this.session.getDatIdMap());
		HLDDelete hlddel = mgr.fullBuildDelete(queryExp);
		log.log(hlddel.toString());
		assertEquals(expectedJoins, hlddel.hld.joinL.size());
		return hlddel;
	}

	protected DeleteStatementExp compileToDeleteStatement(String src) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		for(Exp exp: sessimpl.mostRecentContinueExpL) {
			if (exp instanceof DeleteStatementExp) {
				return (DeleteStatementExp) exp;
			}
		}
		return null;
	}
	
	protected void chkDeleteSql(HLDDelete hlddel, String expected, String...args) {
		SqlStatement stm = mgr.generateSql(hlddel);
		chkStm(stm, expected, args);
	}
	
}
