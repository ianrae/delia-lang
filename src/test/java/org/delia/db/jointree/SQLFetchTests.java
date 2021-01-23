//package org.delia.db.jointree;
//
//import static org.junit.Assert.*;
//
//import java.util.List;
//
//import org.delia.db.hls.HLSSQLGeneratorImpl;
//import org.delia.db.hls.join.JTElement;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * @author Ian Rae
// *
// */
//public class SQLFetchTests extends JoinTreeTestBase {
//	
//	@Test
//	public void testPlainFilter() {
//		//1 and 2
//		sqlchkP("let x = C1[55].fetch('addr')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE a.cid = ?", "55"); 
//		sqlchkP("let x = A1[100].fetch('cust')", "SELECT a.id,a.y,a.cust,b.cid as cust,b.x FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE a.id = ?", "100"); 
//		
////		//3 and 4
//		sqlchkP("let x = CM[55].fetch('addr')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM CM as a LEFT JOIN AM1 as b ON a.cid=b.cust WHERE a.cid = ?", "55"); 
//		sqlchkP("let x = AM1[100].fetch('cust')", "SELECT a.id,a.y,a.cust,b.cid as cust,b.x FROM AM1 as a LEFT JOIN CM as b ON a.cust=b.cid WHERE a.id = ?", "100");
//		
//		//5 and 6
//		sqlchkP("let x = CMM[55].fetch('addr')", "SELECT a.cid,a.x,c.rightv as addr,b.id as addr,b.y FROM CMM as a LEFT JOIN CMMAMMDat1 as c ON a.cid=c.leftv LEFT JOIN AMM as b ON b.id=c.rightv WHERE a.cid = ?", "55"); 
//		sqlchkP("let x = AMM[100].fetch('cust')", "SELECT a.id,a.y,c.leftv as cust,b.cid as cust,b.x FROM AMM as a LEFT JOIN CMMAMMDat1 as c ON a.id=c.rightv LEFT JOIN CMM as b ON b.cid=c.leftv WHERE a.id = ?", "100"); 
//	}
//
////	//TODO not yet supported
////	@Test
////	public void testPlainFilterFK() {
////		//1 and 2
////		chkJoinTree("let x = C1[55].fk('addr')", "C1|addr|A1"); 
////		chkJoinTree("let x = A1[100].fk('cust')", "A1|cust|C1"); 
////		
////		//3 and 4
////		chkJoinTree("let x = CM[55].fk('addr')", "CM|addr|AM1"); 
////		chkJoinTree("let x = AM1[100].fk('cust')", "AM1|cust|CM");
////		
////		//5 and 6
////		chkJoinTree("let x = CMM[55].fk('addr')", "CMM|addr|AMM"); 
////		chkJoinTree("let x = AMM[100].fk('cust')", "AMM|cust|CMM"); 
////	}
//	
//	@Test
//	public void testPlainFilterFKs() {
//		//1 and 2
//		sqlchkP("let x = C1[55].fks()", "SELECT a.cid,a.x,b.id as addr FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE a.cid = ?", "55"); 
//		sqlchkP("let x = A1[100].fks()", "SELECT * FROM A1 as a WHERE a.id = ?", "100"); 
//		
//		//3 and 4
//		sqlchkP("let x = CM[55].fks()", "SELECT a.cid,a.x,b.id as addr FROM CM as a LEFT JOIN AM1 as b ON a.cid=b.cust WHERE a.cid = ?", "55"); 
//		sqlchkP("let x = AM1[100].fks()", "SELECT * FROM AM1 as a WHERE a.id = ?", "100");
//		
//		//5 and 6
//		sqlchkP("let x = CMM[55].fks()", "SELECT a.cid,a.x,c.rightv as addr FROM CMM as a LEFT JOIN CMMAMMDat1 as c ON a.cid=c.leftv WHERE a.cid = ?", "55"); 
//		sqlchkP("let x = AMM[100].fks()", "SELECT a.id,a.y,c.leftv as cust FROM AMM as a LEFT JOIN CMMAMMDat1 as c ON a.id=c.rightv WHERE a.id = ?", "100"); 
//	}
//	
//	@Test
//	public void testRefFilter() {
//		//1 and 2
//		sqlchkP("let x = C1[addr < 111].fetch('addr')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE b.id < ?", "111"); 
//		sqlchkP("let x = A1[cust < 111].fetch('cust')", "SELECT a.id,a.y,a.cust,b.cid as cust,b.x FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE a.cust < ?", "111");
//		
//		//3 and 4
//		sqlchkP("let x = CM[addr < 111].fetch('addr')", "SELECT a.cid,a.x,b.id as addr,b.y,b.cust FROM CM as a LEFT JOIN AM1 as b ON a.cid=b.cust WHERE b.id < ?", "111"); 
//		sqlchkP("let x = AM1[cust < 111].fetch('cust')", "SELECT a.id,a.y,a.cust,b.cid as cust,b.x FROM AM1 as a LEFT JOIN CM as b ON a.cust=b.cid WHERE a.cust < ?", "111");
//		
////		//5 and 6
//		sqlchkP("let x = CMM[addr < 111].fetch('addr')", "SELECT a.cid,a.x,c.rightv as addr,b.id as addr,b.y FROM CMM as a LEFT JOIN CMMAMMDat1 as c ON a.cid=c.leftv LEFT JOIN AMM as b ON b.id=c.rightv WHERE c.rightv < ?", "111"); 
//		sqlchkP("let x = AMM[cust < 111].fetch('cust')", "SELECT a.id,a.y,c.leftv as cust,b.cid as cust,b.x FROM AMM as a LEFT JOIN CMMAMMDat1 as c ON a.id=c.rightv LEFT JOIN CMM as b ON b.cid=c.leftv WHERE c.leftv < ?", "111"); 
//	}
//	
//
//	@Test
//	public void testDebugSQL() {
//	}
//
//	//---
//	
//	@Before
//	public void init() {
//		createDao();
////		HLSSQLGeneratorImpl.useJoinTreeFlag = true;		
//	}
//	private void sqlchkP(String src, String sqlExpected, String param1) {
//		doSqlchkP(src, sqlExpected, param1);
//	}
//
//}
