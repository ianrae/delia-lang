package org.delia.db.newhls.cud;


import static org.junit.Assert.assertEquals;

import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.db.newhls.HLDManager;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.NewHLSTestBase;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class DeleteTests extends NewHLSTestBase {
	
	public static class HLDDelete {
		public HLDQuery hld;
		
		public HLDDelete(HLDQuery hld) {
			this.hld = hld;
		}
	}
	
	
	//one addr
	@Test
	public void testFKS11Parent() {
		useCustomer11Src = true;
		String src = "delete Customer[55]";
		
		HLDDelete hldDelete = buildFromSrcDelete(src, 0); 
		chkFullSql(hldDelete.hld, "SELECT t0.cid,t0.x,t1.id FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust WHERE t0.cid=?", "55");
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
		HLDQuery hld = mgr.fullBuildQuery(queryExp);
		log.log(hld.toString());
		assertEquals(expectedJoins, hld.joinL.size());
		return new HLDDelete(hld);
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
}
