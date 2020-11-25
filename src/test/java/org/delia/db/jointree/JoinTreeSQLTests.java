package org.delia.db.jointree;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.hls.join.JTElement;
import org.delia.db.hls.join.JoinTreeEngine;
import org.delia.queryresponse.LetSpan;
import org.delia.queryresponse.LetSpanEngine;
import org.junit.Before;
import org.junit.Test;

/**
 * JoinTree extracts all potential JOINS from a query
 * Here are all the types of queries and their joins
 * 
 * C[name]    no join
 * C[addr == 111]  join if C is parent
 * A[cust == 111]  no join (A is child)
 * C[addr == 111 && profile==44]  2 joins
 *  
 * C[addr in(111)]  same as C[addr == 111]
 * C[addr.city == 'toronto'] ""  
 *  
 * C[55].firstName no join
 * C[55].addr   no join since returning Address and its child
 * A[100].cust   join since returning Customer and its parent
 * C[addr == 111].addr  no join. select from address where a.id=111
 * A[cust == 111].cust  no join
 * C[55].addr.city  same as just .addr
 * C[55].addr.country  double join!
 * C[55].orderBy(addr) join since C is parent  
 * A[100].orderBy(cust) no join since A is child  
 * -fetch
 * -fk
 * 
 * @author Ian Rae
 *
 */
public class JoinTreeSQLTests extends JoinTreeTestBase {
	
	@Test
	public void testPlainFilter() {
		chkJoinTree("let x = C1[cid < 111]"); 
		chkJoinTree("let x = A1[id < 111]"); 
		chkJoinTree("let x = CM[cid < 111]"); 
		chkJoinTree("let x = AM1[cid < 111]"); 
		chkJoinTree("let x = CMM[cid < 111]"); 
		chkJoinTree("let x = AMM[cid < 111]"); 
	}
	
	@Test
	public void testRefFilter() {
		//1 and 2
		chkJoinTree("let x = C1[addr < 111]", "C1|addr|A1"); 
		chkJoinTree("let x = A1[cust < 111]");
		
		//3 and 4
		chkJoinTree("let x = CM[addr < 111]", "CM|addr|AM1"); 
		chkJoinTree("let x = AM1[cust < 111]");
		
		//5 and 6
		chkJoinTree("let x = CMM[addr < 111]", "CMM|addr|AMM"); 
		chkJoinTree("let x = AMM[cust < 111]", "AMM|cust|CMM"); 
	}
	

	@Test
	public void testDebugSQL() {
//		chkJoinTree("let x = C1[addr < 111]", "C1|addr|A1"); 
		chkJoinTree("let x = AMM[cust < 111]", "AMM|cust|CMM"); 

	}

	//---
	
	@Before
	public void init() {
		createDao();
	}

	protected void chkJoinTree(String src, String ...arExpected) {
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
	}
}
