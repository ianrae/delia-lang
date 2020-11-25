package org.delia.db.jointree;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.db.hls.HLSTestBase;
import org.delia.db.hls.JTElement;
import org.delia.db.hls.JoinTreeEngine;
import org.delia.queryresponse.LetSpan;
import org.delia.queryresponse.LetSpanEngine;
import org.junit.Before;
import org.junit.Test;

/**
 * JoinTree extracts all potential JOINS from a query
 * 
 * 
 * @author Ian Rae
 *
 */
public class JoinTreeTests extends HLSTestBase {
	
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
