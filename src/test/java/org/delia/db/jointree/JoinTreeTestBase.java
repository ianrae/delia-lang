package org.delia.db.jointree;


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.hls.HLSTestBase;
import org.delia.db.hls.join.JTElement;
import org.delia.db.hls.join.JoinTreeEngine;
import org.delia.queryresponse.LetSpan;
import org.delia.queryresponse.LetSpanEngine;
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
		
		src += " type CMMSelf struct {cid int unique, x int, relation addr CMMSelf 'r1' many optional ";
		src += "\n  relation cust CMMSelf 'r1' many optional } end";
		
		return src;
	}

	protected List<JTElement> chkJoinTree(String src, String ...arExpected) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		LetSpanEngine letEngine = new LetSpanEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		List<LetSpan> spanL = letEngine.buildAllSpans(queryExp);
		
		JoinTreeEngine jtEngine = new JoinTreeEngine(delia.getFactoryService(), session.getExecutionContext().registry);
		List<JTElement> resultL = jtEngine.parse(queryExp, spanL);
		int n = arExpected.length;
		assertEquals(n, resultL.size());
		
		for(String expected: arExpected) {
			String s = resultL.get(0).toString();
			assertEquals(expected, s);
		}
		return resultL;
	}
}
