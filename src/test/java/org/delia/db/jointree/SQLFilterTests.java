package org.delia.db.jointree;

import org.delia.db.hls.HLSSQLGeneratorImpl;
import org.junit.Before;
import org.junit.Test;

/**
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
		sqlchkP("let x = C1[addr < 111]", "SELECT a.cid,a.x FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE b.id < ?", arg); 
		sqlchkP("let x = A1[cust < 111]", "SELECT * FROM A1 as a WHERE a.cust < ?", arg); 
		
		//3 and 4
		sqlchkP("let x = CM[addr < 111]", "SELECT a.cid,a.x FROM CM as a LEFT JOIN AM1 as b ON a.cid=b.cust WHERE b.id < ?", arg); 
		sqlchkP("let x = AM1[cust < 111]", "SELECT * FROM AM1 as a WHERE a.cust < ?", arg); 
		
//TODO		//5 and 6
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
//		chkJoinTree("let x = C1[addr.y < 111]", "C1|addr|A1"); 
		String arg = "111";
		//TODO: this needs to be supported
		sqlchkP("let x = C1[addr.y < 111]", "SELECT a.cid,a.x FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE b.id < ?", arg); 
	}
	

	@Test
	public void testDebugSQL() {
		String arg = "111";
		sqlchkP("let x = C1[addr < 111]", "SELECT a.cid,a.x FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE b.id < ?", arg); 

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
