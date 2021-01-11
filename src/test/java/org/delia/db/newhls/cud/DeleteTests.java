package org.delia.db.newhls.cud;


import static org.junit.Assert.assertEquals;

import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.newhls.NewHLSTestBase;
import org.delia.db.sql.prepared.SqlStatement;
import org.junit.Test;

/**
 * TODO: when delete also delete DAT table, and non-optional child
 * @author Ian Rae
 *
 */
public class DeleteTests extends NewHLSTestBase {
	
	@Test
	public void test1() {
		useCustomer11Src = true;
		String src = "delete Customer[55]";
		
		HLDDeleteStatement hldDelete = buildFromSrcDelete(src, 0); 
		chkDeleteSql(hldDelete, "DELETE FROM Customer as t0 WHERE t0.cid=?", "55");
	}
	@Test
	public void test2() {
		useCustomer11Src = true;
		String src = "delete Address[100]";
		
		HLDDeleteStatement hldDelete = buildFromSrcDelete(src, 0); 
		chkDeleteSql(hldDelete, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}
	@Test
	public void test3() {
		useCustomer11Src = true;
		String src = "delete Customer[x > 10]";
		
		HLDDeleteStatement hldDelete = buildFromSrcDelete(src, 0); 
		chkDeleteSql(hldDelete, "DELETE FROM Customer as t0 WHERE t0.x > ?", "10");
	}
	
	//-------------------------
	protected HLDDeleteStatement buildFromSrcDelete(String src, int expectedJoins) {
		DeleteStatementExp deleteExp = compileToDeleteStatement(src);
		QueryExp queryExp = deleteExp.queryExp;
		log.log(src);
		
		mgr = createManager(); 
		HLDDeleteStatement hlddel = mgr.fullBuildDelete(queryExp);
		log.log(hlddel.toString());
		assertEquals(expectedJoins, hlddel.hlddelete.hld.joinL.size());
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
	
	protected void chkDeleteSql(HLDDeleteStatement hlddel, String expected, String...args) {
		SqlStatement stm = mgr.generateSql(hlddel);
		chkStm(stm, expected, args);
	}
}
