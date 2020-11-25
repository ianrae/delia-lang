package org.delia.db.jointree;


import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.hls.HLSTestBase;
import org.delia.runner.ResultValue;

/**
 * 
 * 
 * @author Ian Rae
 *
 */
public class JoinTreeTestBase extends HLSTestBase {
	protected boolean useJoinTreeSrc = true;

	protected LetStatementExp compileQueryToLetStatement(String src) {
		String initialSrc;
		if (useJoinTreeSrc) {
			initialSrc = buildCustomerJoinTreeSrc();
		} else {
			return  super.compileQueryToLetStatement(src);
		}
		log.log("initial: " + initialSrc);
		
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(initialSrc);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		this.session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		LetStatementExp letStatement = findLet(sessimpl);
		return letStatement;
	}

	protected String buildCustomerJoinTreeSrc() {
		String src = " type C1 struct {cid int unique, x int, relation addr A1 one parent optional  } end";
		src += "\n type A1 struct {id int unique, y int, relation cust C1 one optional } end";

		src += " type CM struct {cid int unique, x int, relation addr AM1 many optional  } end";
		src += "\n type AM1 struct {id int unique, y int, relation cust CM one optional } end";
		
		src += " type CMM struct {cid int unique, x int, relation addr AMM many optional  } end";
		src += "\n type AMM struct {id int unique, y int, relation cust CMM many optional } end";
		
		return src;
	}

}
