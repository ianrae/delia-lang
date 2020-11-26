package org.delia.db.jointree;

import org.delia.db.hls.HLSSQLGeneratorImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class SQLFieldTests extends JoinTreeTestBase {
	
	/**
	 *    1 c[addr]   |  2 c[].addr
	 *    -----------------------
	 *    3 a[cust]   |  4 a[].cust
	 */
	@Test
	public void testBigFour() {
//		//1
		String arg = "111";
//		sqlchkP("let x = C1[addr < 111]", "SELECT a.cid,a.x FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE b.id < ?", arg); 
//		
//		//2
//		arg = "55";
//		sqlchkP("let x = C1[55].addr", "SELECT a.id,a.y,a.cust FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE b.cid = ?", arg); 
//
//		//3
//		arg = "111";
//		sqlchkP("let x = A1[cust < 111]", "SELECT * FROM A1 as a WHERE a.cust < ?", arg); 
		
		//4
		arg = "111";
		sqlchkP("let x = A1[11].cust", "SELECT a.id,a.y,a.cust FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE b.cid = ?", arg); 
	}

	
	
	
	@Test
	public void testPlainFilter() {
		//1 and 2
//		chkJoinTree("let x = C1[55].addr", "C1|addr|A1"); 
		String arg = "55";
		sqlchkP("let x = C1[55].addr", "SELECT a.id,a.y,a.cust FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE b.cid = ?", arg); 
//		chkJoinTree("let x = A1[100].cust", "A1|cust|C1"); 
//		
//		//3 and 4
//		chkJoinTree("let x = CM[55].addr", "CM|addr|AM1"); 
//		chkJoinTree("let x = AM1[100].cust", "AM1|cust|CM");
//		
//		//5 and 6
//		chkJoinTree("let x = CMM[55].addr", "CMM|addr|AMM"); 
//		chkJoinTree("let x = AMM[100].cust", "AMM|cust|CMM"); 
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
		HLSSQLGeneratorImpl.useJoinTreeFlag = true;		
	}
	private void sqlchkP(String src, String sqlExpected, String param1) {
		doSqlchkP(src, sqlExpected, param1);
	}

}
