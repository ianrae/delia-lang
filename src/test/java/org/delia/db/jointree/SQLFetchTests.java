package org.delia.db.jointree;

import static org.junit.Assert.*;

import java.util.List;

import org.delia.db.hls.HLSSQLGeneratorImpl;
import org.delia.db.hls.join.JTElement;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class SQLFetchTests extends JoinTreeTestBase {
	
	@Test
	public void testPlainFilter() {
		//1 and 2
		sqlchkP("let x = C1[55].fetch('addr')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE a.cid = ?", "55"); 
		chkJoinTree("let x = A1[100].fetch('cust')", "A1|cust|C1"); 
		
		//3 and 4
		chkJoinTree("let x = CM[55].fetch('addr')", "CM|addr|AM1"); 
		chkJoinTree("let x = AM1[100].fetch('cust')", "AM1|cust|CM");
		
		//5 and 6
		chkJoinTree("let x = CMM[55].fetch('addr')", "CMM|addr|AMM"); 
		chkJoinTree("let x = AMM[100].fetch('cust')", "AMM|cust|CMM"); 
	}

	@Test
	public void testFKFlag() {
		List<JTElement> list = chkJoinTree("let x = C1[55].fetch('addr')", "C1|addr|A1"); 
		assertEquals(false, list.get(0).usedForFK);
		
		list = chkJoinTree("let x = C1[55].fks()", "C1|addr|A1"); 
		assertEquals(true, list.get(0).usedForFK);
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
//		sqlchkP("let x = C1[55]", "SELECT * FROM C1 as a WHERE a.cid = ?", "55"); 
//		sqlchkP("let x = C1[55].fetch('addr')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE a.cid = ?", "55"); 
//		sqlchkP("let x = A1[100].fetch('cust')", "SELECT a.id,a.y,a.cust,b.cid as cust,b.x FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE a.id = ?", "100"); 
		//TODO: 2 cust value in sql. is this a problem?
		
//		//3 and 4
//		sqlchkP("let x = CM[55].fetch('addr')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM CM as a LEFT JOIN AM1 as b ON a.cid=b.cust WHERE a.cid = ?", "55"); 
//		sqlchkP("let x = AM1[100].fetch('cust')", "SELECT a.id,a.y,a.cust,b.cid as cust,b.x FROM AM1 as a LEFT JOIN CM as b ON a.cust=b.cid WHERE a.id = ?", "100");
		
//		//5 and 6
//		sqlchkP("let x = CMM[55].fetch('addr')", "SELECT a.cid,a.x,c.leftv as cust,b.id as addr,b.y FROM CMM as a LEFT JOIN CMMAMMDat1 as c ON a.cid=c.leftv WHERE a.cid = ?", "55");
		//TODO: should we have b.addr as c.rightv??
		sqlchkP("let x = AMM[100].fetch('cust')", "SELECT a.id,a.y,c.rightv as addr,b.cid as cust,b.x FROM AMM as a LEFT JOIN CMMAMMDat1 as c ON a.id=c.rightv WHERE a.id = ?", "100"); 
		//TODO: b.cid is wrong!
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
