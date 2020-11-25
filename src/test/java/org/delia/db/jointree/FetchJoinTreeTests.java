package org.delia.db.jointree;

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
 * C[55].fetch(addr) join since C is parent  
 * A[100].fetch(cust) no join since A is child  
 * -fetch
 * -fk
 * 
 * @author Ian Rae
 *
 */
public class FetchJoinTreeTests extends JoinTreeTestBase {
	
	@Test
	public void testPlainFilter() {
		//1 and 2
		chkJoinTree("let x = C1[55].fetch('addr')", "C1|addr|A1"); 
		chkJoinTree("let x = A1[100].fetch('cust')", "A1|cust|C1"); 
		
		//3 and 4
		chkJoinTree("let x = CM[55].fetch('addr')", "CM|addr|AM1"); 
		chkJoinTree("let x = AM1[100].fetch('cust')", "AM1|cust|CM");
		
		//5 and 6
		chkJoinTree("let x = CMM[55].fetch('addr')", "CMM|addr|AMM"); 
		chkJoinTree("let x = AMM[100].fetch('cust')", "AMM|cust|CMM"); 
	}
	
//	//TODO not yet supported
//	@Test
//	public void testPlainFilterFK() {
//		//1 and 2
//		chkJoinTree("let x = C1[55].fk('addr')", "C1|addr|A1"); 
//		chkJoinTree("let x = A1[100].fk('cust')", "A1|cust|C1"); 
//		
//		//3 and 4
//		chkJoinTree("let x = CM[55].fk('addr')", "CM|addr|AM1"); 
//		chkJoinTree("let x = AM1[100].fk('cust')", "AM1|cust|CM");
//		
//		//5 and 6
//		chkJoinTree("let x = CMM[55].fk('addr')", "CMM|addr|AMM"); 
//		chkJoinTree("let x = AMM[100].fk('cust')", "AMM|cust|CMM"); 
//	}
	
	@Test
	public void testPlainFilterFKs() {
		//1 and 2
		chkJoinTree("let x = C1[55].fks()", "C1|addr|A1"); 
		chkJoinTree("let x = A1[100].fks()"); 
		
		//3 and 4
		chkJoinTree("let x = CM[55].fks()", "CM|addr|AM1"); 
		chkJoinTree("let x = AM1[100].fks()");
		
		//5 and 6
		chkJoinTree("let x = CMM[55].fks()", "CMM|addr|AMM"); 
		chkJoinTree("let x = AMM[100].fks()", "AMM|cust|CMM"); 
	}
	
	@Test
	public void testRefFilter() {
		//1 and 2
		chkJoinTree("let x = C1[addr < 111].fetch('addr')", "C1|addr|A1"); 
		chkJoinTree("let x = A1[cust < 111].fetch('cust')", "A1|cust|C1");
		
		//3 and 4
		chkJoinTree("let x = CM[addr < 111].fetch('addr')", "CM|addr|AM1"); 
		chkJoinTree("let x = AM1[cust < 111].fetch('cust')", "AM1|cust|CM");
		
		//5 and 6
		chkJoinTree("let x = CMM[addr < 111].fetch('addr')", "CMM|addr|AMM"); 
		chkJoinTree("let x = AMM[cust < 111].fetch('cust')", "AMM|cust|CMM"); 
	}
	

	@Test
	public void testDebugSQL() {

	}

	//---
	
	@Before
	public void init() {
		createDao();
	}

}
