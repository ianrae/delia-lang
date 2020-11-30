package org.delia.db.jointree;

import org.delia.db.hls.HLSSQLGeneratorImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class SQLFieldTests extends JoinTreeTestBase {
	
	@Test
	public void testPlainFilter() {
		//1 and 2
		sqlchkP("let x = C1[55].addr", "SELECT a.id,a.y,a.cust FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE b.cid = ?", "55"); 
		sqlchkP("let x = A1[100].cust", "SELECT a.cid,a.x FROM C1 as a LEFT JOIN A1 as c ON a.cid=c.cust WHERE c.id = ?", "100"); 
		
		//3 and 4
		sqlchkP("let x = CM[55].addr", "SELECT a.id,a.y,a.cust FROM AM1 as a LEFT JOIN CM as b ON a.cust=b.cid WHERE b.cid = ?", "55"); 
		sqlchkP("let x = AM1[100].cust", "SELECT a.cid,a.x FROM CM as a LEFT JOIN AM1 as c ON a.cid=c.cust WHERE c.id = ?", "100");
		
		//5 and 6
		sqlchkP("let x = CMM[55].addr", "SELECT a.id,a.y,c.leftv as cust FROM AMM as a LEFT JOIN CMMAMMDat1 as c ON a.id=c.rightv WHERE c.leftv = ?", "55"); 
		sqlchkP("let x = AMM[100].cust", "SELECT a.cid,a.x,c.rightv as addr FROM CMM as a LEFT JOIN CMMAMMDat1 as c ON a.cid=c.leftv WHERE c.rightv = ?", "100"); 
	}
	
	@Test
	public void testRefFilter() {
		//1 and 2 
		sqlchkP("let x = C1[addr < 111].addr", "SELECT a.id,a.y,a.cust FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE b.id < ?", "111"); 
		sqlchkP("let x = A1[cust < 111].cust", "SELECT a.cid,a.x FROM C1 as a LEFT JOIN A1 as c ON a.cid=c.cust WHERE c.cust < ?", "111");
		
		//3 and 4
		sqlchkP("let x = CM[addr < 111].addr", "SELECT a.id,a.y,a.cust FROM AM1 as a LEFT JOIN CM as b ON a.cust=b.cid WHERE b.id < ?", "111"); 
		sqlchkP("let x = AM1[cust < 111].cust", "SELECT a.cid,a.x FROM CM as a LEFT JOIN AM1 as c ON a.cid=c.cust WHERE c.cust < ?", "111");
		
		//5 and 6
		sqlchkP("let x = CMM[addr < 111].addr", "SELECT a.id,a.y,c.leftv as cust FROM AMM as a LEFT JOIN CMMAMMDat1 as c ON a.id=c.rightv WHERE c.rightv < ?", "111"); 
		sqlchkP("let x = AMM[cust < 111].cust", "SELECT a.cid,a.x,c.rightv as addr FROM CMM as a LEFT JOIN CMMAMMDat1 as c ON a.cid=c.leftv WHERE c.leftv < ?", "111"); 
	}
	
//	@Test
//	public void testDoubleRefFilter() {
//		//1 and 2
//		sqlchkP("let x = C1[addr < 111 && something..]", "C1|addr|A1"); 
//		sqlchkP("let x = A1[cust < 111]");
//		
//		//3 and 4
//		sqlchkP("let x = CM[addr < 111]", "CM|addr|AM1"); 
//		sqlchkP("let x = AM1[cust < 111]");
//		
//		//5 and 6
//		sqlchkP("let x = CMM[addr < 111]", "CMM|addr|AMM"); 
//		sqlchkP("let x = AMM[cust < 111]", "AMM|cust|CMM"); 
//	}
	
	
	@Test
	public void testRefSubFieldFilter() {
		//TODO: fix NOT YET SUPPORTED
		//1 and 2  
//		sqlchkP("let x = C1[addr.y < 111].addr", "C1|addr|A1", "111"); 
//		sqlchkP("let x = A1[cust.x < 111].cust", "A1|cust|C1", "111");
		//assume the rest work
	}
	
	@Test
	public void testSubField() {
		//TODO: fix NOT YET SUPPORTED
		//1 and 2 sssssssss
//		sqlchkP("let x = C1[55].addr.y", "C1|addr|A1"); 
//		sqlchkP("let x = A1[cust < 111].cust.x", "A1|cust|C1");
//		
//		//3 and 4
//		sqlchkP("let x = CM[addr < 111].addr.y", "CM|addr|AM1"); 
//		sqlchkP("let x = AM1[cust < 111].cust.x", "AM1|cust|CM");
//		
//		//5 and 6
//		sqlchkP("let x = CMM[addr < 111].addr.y", "CMM|addr|AMM"); 
//		sqlchkP("let x = AMM[cust < 111].cust.x", "AMM|cust|CMM"); 
	}
	
	//TODO test C[true].addr.country double join

	@Test
	public void testDebugSQL() {

	}

	//---
	
	@Before
	public void init() {
		createDao();
//		HLSSQLGeneratorImpl.useJoinTreeFlag = true;		
	}
	private void sqlchkP(String src, String sqlExpected, String param1) {
		doSqlchkP(src, sqlExpected, param1);
	}

}
