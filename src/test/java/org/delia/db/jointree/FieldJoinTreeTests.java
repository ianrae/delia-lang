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
 * C[55].orderBy(addr) join since C is parent  
 * A[100].orderBy(cust) no join since A is child  
 * -fetch
 * -fk
 * 
 * @author Ian Rae
 *
 */
public class FieldJoinTreeTests extends JoinTreeTestBase {
	
	@Test
	public void testPlainFilter() {
		//1 and 2
		chkJoinTree("let x = C1[55].addr", "C1|addr|A1"); 
//		chkJoinTree("let x = C1[addr < 111].addr", "C1|addr|A1"); 
		chkJoinTree("let x = A1[100].cust", "A1|cust|C1"); 
//		chkJoinTree("let x = A1[id < 111].cust", "A1|cust|C1"); 
		
		//3 and 4
		chkJoinTree("let x = CM[55].addr", "CM|addr|AM1"); 
		chkJoinTree("let x = AM1[100].cust", "AM1|cust|CM");
		
		//5 and 6
		chkJoinTree("let x = CMM[55].addr", "CMM|addr|AMM"); 
		chkJoinTree("let x = AMM[100].cust", "AMM|cust|CMM"); 
	}
	
	@Test
	public void testRefFilter() {
		//1 and 2
		chkJoinTree("let x = C1[addr < 111].addr", "C1|addr|A1"); 
		chkJoinTree("let x = A1[cust < 111].cust", "A1|cust|C1");
		
		//3 and 4
		chkJoinTree("let x = CM[addr < 111].addr", "CM|addr|AM1"); 
		chkJoinTree("let x = AM1[cust < 111].cust", "AM1|cust|CM");
		
		//5 and 6
		chkJoinTree("let x = CMM[addr < 111].addr", "CMM|addr|AMM"); 
		chkJoinTree("let x = AMM[cust < 111].cust", "AMM|cust|CMM"); 
	}
	
//	@Test
//	public void testDoubleRefFilter() {
//		//1 and 2
//		chkJoinTree("let x = C1[addr < 111 && something..]", "C1|addr|A1"); 
//		chkJoinTree("let x = A1[cust < 111]");
//		
//		//3 and 4
//		chkJoinTree("let x = CM[addr < 111]", "CM|addr|AM1"); 
//		chkJoinTree("let x = AM1[cust < 111]");
//		
//		//5 and 6
//		chkJoinTree("let x = CMM[addr < 111]", "CMM|addr|AMM"); 
//		chkJoinTree("let x = AMM[cust < 111]", "AMM|cust|CMM"); 
//	}
	
//	@Test
//	public void testInRefFilter() {
//		//1 and 2
//		chkJoinTree("let x = C1[addr in [111]]", "C1|addr|A1"); 
//		chkJoinTree("let x = A1[cust < 111]");
//		
//		//3 and 4
//		chkJoinTree("let x = CM[addr < 111]", "CM|addr|AM1"); 
//		chkJoinTree("let x = AM1[cust < 111]");
//		
//		//5 and 6
//		chkJoinTree("let x = CMM[addr < 111]", "CMM|addr|AMM"); 
//		chkJoinTree("let x = AMM[cust < 111]", "AMM|cust|CMM"); 
//	}
	
	
	@Test
	public void testRefSubFieldFilter() {
		//1 and 2
		chkJoinTree("let x = C1[addr.y < 111].addr", "C1|addr|A1"); 
		chkJoinTree("let x = A1[cust.x < 111].cust", "A1|cust|C1");
		//assume the rest work
	}
	
	@Test
	public void testSubField() {
		//1 and 2
		chkJoinTree("let x = C1[55].addr.y", "C1|addr|A1"); 
		chkJoinTree("let x = A1[cust < 111].cust.x", "A1|cust|C1");
		
		//3 and 4
		chkJoinTree("let x = CM[addr < 111].addr.y", "CM|addr|AM1"); 
		chkJoinTree("let x = AM1[cust < 111].cust.x", "AM1|cust|CM");
		
		//5 and 6
		chkJoinTree("let x = CMM[addr < 111].addr.y", "CMM|addr|AMM"); 
		chkJoinTree("let x = AMM[cust < 111].cust.x", "AMM|cust|CMM"); 
	}
	
	//TODO test C[true].addr.country double join

	@Test
	public void testDebugSQL() {
		chkJoinTree("let x = C1[55].addr", "C1|addr|A1"); 
		chkJoinTree("let x = C1[addr < 111].addr", "C1|addr|A1"); 
//		chkJoinTree("let x = AMM[cust < 111]", "AMM|cust|CMM"); 

	}

	//---
	
	@Before
	public void init() {
		createDao();
	}

}
