package org.delia.db.newhls.cud;


import static org.junit.Assert.assertEquals;

import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.newhls.HLDManager;
import org.delia.db.newhls.NewHLSTestBase;
import org.delia.db.sql.prepared.SqlStatement;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class InsertTests extends NewHLSTestBase {
	
	@Test
	public void test1() {
		useCustomer11Src = true;
		String src = "delete Customer[55]";
		
		HLDInsert hldins = buildFromSrcInsert(src, 0); 
		chkInsertSql(hldins, "DELETE FROM Customer as t0 WHERE t0.cid=?", "55");
	}
//	@Test
//	public void test2() {
//		useCustomer11Src = true;
//		String src = "delete Address[100]";
//		
//		HLDDelete hldDelete = buildFromSrcDelete(src, 0); 
//		chkDeleteSql(hldDelete, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
//	}
//	@Test
//	public void test3() {
//		useCustomer11Src = true;
//		String src = "delete Customer[x > 10]";
//		
//		HLDDelete hldDelete = buildFromSrcDelete(src, 0); 
//		chkDeleteSql(hldDelete, "DELETE FROM Customer as t0 WHERE t0.x > ?", "10");
//	}
	
	//-------------------------
	protected HLDInsert buildFromSrcInsert(String src, int expectedJoins) {
		InsertStatementExp insertExp = compileToInsertStatement(src);
		log.log(src);
		
		mgr = new HLDManager(this.session.getExecutionContext().registry, delia.getFactoryService(), log, this.session.getDatIdMap());
		HLDInsert hldins = mgr.fullBuildInsert(insertExp);
		log.log(hldins.toString());
//		assertEquals(expectedJoins, hldins.hld.joinL.size());
		return hldins;
	}

	protected InsertStatementExp compileToInsertStatement(String src) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		for(Exp exp: sessimpl.mostRecentContinueExpL) {
			if (exp instanceof InsertStatementExp) {
				return (InsertStatementExp) exp;
			}
		}
		return null;
	}
	
	protected void chkInsertSql(HLDInsert hldins, String expected, String...args) {
		SqlStatement stm = null; //mgr.generateSql(hldins);
		chkStm(stm, expected, args);
	}
}
