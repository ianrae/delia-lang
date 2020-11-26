package org.delia.db.jointree;

import org.delia.db.hls.HLSSQLGeneratorImpl;
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
public class SQLFilterTests extends JoinTreeTestBase {
	
	@Test
	public void testPlainFilter() {
		//1 and 2
		String arg = "111";
		sqlchkP("let x = C1[cid < 111]", "SELECT * FROM C1 as a WHERE a.cid < ?", arg); 
		sqlchkP("let x = A1[id < 111]", "SELECT * FROM A1 as a WHERE a.id < ?", arg);
//		
//		//3 and 4
		sqlchkP("let x = CM[cid < 111]", "SELECT * FROM CM as a WHERE a.cid < ?", arg); 
		sqlchkP("let x = AM1[id < 111]", "SELECT * FROM AM1 as a WHERE a.id < ?", arg);
//		
//TODO		//5 and 6
//		chkJoinTree("let x = CMM[cid < 111]"); 
//		chkJoinTree("let x = AMM[cid < 111]"); 
	}
	
	@Test
	public void testRefFilter() {
		//1 and 2
		String arg = "111";
		sqlchkP("let x = C1[addr < 111]", "SELECT * FROM C1 as a WHERE a.cid < ?", arg); 
//		chkJoinTree("let x = C1[addr < 111]", "C1|addr|A1"); 
//		chkJoinTree("let x = A1[cust < 111]");
//		
//		//3 and 4
//		chkJoinTree("let x = CM[addr < 111]", "CM|addr|AM1"); 
//		chkJoinTree("let x = AM1[cust < 111]");
//		
//		//5 and 6
//		chkJoinTree("let x = CMM[addr < 111]", "CMM|addr|AMM"); 
//		chkJoinTree("let x = AMM[cust < 111]", "AMM|cust|CMM"); 
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
		chkJoinTree("let x = C1[addr.y < 111]", "C1|addr|A1"); 
		chkJoinTree("let x = A1[cust.x < 111]");
		
		//3 and 4
		chkJoinTree("let x = CM[addr.y < 111]", "CM|addr|AM1"); 
		chkJoinTree("let x = AM1[cust.x < 111]");
		
		//5 and 6
		chkJoinTree("let x = CMM[addr.y < 111]", "CMM|addr|AMM"); 
		chkJoinTree("let x = AMM[cust.x < 111]", "AMM|cust|CMM"); 
	}
	

	@Test
	public void testDebugSQL() {
		//1 and 2
		String arg = "111";
		sqlchkP("let x = C1[cid < 111]", "SELECT * FROM C1 as a WHERE a.cid < ?", arg); 
		sqlchkP("let x = A1[id < 111]", "SELECT * FROM A1 as a WHERE a.id < ?", arg);
//		
//		//3 and 4
		sqlchkP("let x = CM[cid < 111]", "SELECT * FROM CM as a WHERE a.cid < ?", arg); 
		sqlchkP("let x = AM1[id < 111]", "SELECT * FROM AM1 as a WHERE a.id < ?", arg);
//		
//TODO		//5 and 6
//		chkJoinTree("let x = CMM[cid < 111]"); 
//		chkJoinTree("let x = AMM[cid < 111]"); 

	}

	//---
	
	@Before
	public void init() {
		createDao();
		HLSSQLGeneratorImpl.useJoinTreeFlag = true;		
	}
	private void sqlchk(String src, String sqlExpected) {
		sqlchkP(src, sqlExpected, null);
	}
	private void sqlchkP(String src, String sqlExpected, String param1) {
		doSqlchkP(src, sqlExpected, param1);
	}

}
