package org.delia.db.jointree;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.hls.HLSTestBase;
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
public class JoinTreeSQLTests extends HLSTestBase {
	
	@Test
	public void test() {
		useCustomer11Src = true;
		chkJoinTree("let x = Customer[55].fks()", "Customer|addr|Address"); 
		chkJoinTree("let x = Customer[55].x"); 
		
		chkJoinTree("let x = Customer[55].fetch('addr')", "Customer|addr|Address"); 
		chkJoinTree("let x = Customer[55].addr", "Customer|addr|Address"); 
		chkJoinTree("let x = Customer[55].addr.y", "Customer|addr|Address"); 
		//FUTURE later support order by doing implicit fetch. orderBy(addr.city)

		//FUTUER test double join   .addr.country
//		
		chkJoinTree("let x = Customer[addr < 111]", "Customer|addr|Address"); 
	}

	@Test
	public void testDouble() {
		useCustomer11Src = true;
		chkJoinTree("let x = Customer[addr < 111].fetch('addr')", "Customer|addr|Address"); 
	}

	@Test
	public void testDebugSQL() {
		useCustomer11Src = true;
		chkJoinTree("let x = Customer[addr < 111]", "Customer|addr|Address"); 

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
