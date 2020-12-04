package org.delia.db.jointree;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class SQLBigFourTests extends JoinTreeTestBase {
	
	/**
	 *    1 c[addr]   |  2 c[].addr
	 *    -----------------------
	 *    3 a[cust]   |  4 a[].cust
	 */
	@Test
	public void testBigFour11() {
		//1
		String arg = "111";
		sqlchkP("let x = C1[addr < 111]", "SELECT a.cid,a.x FROM C1 as a LEFT JOIN A1 as b ON a.cid=b.cust WHERE b.id < ?", arg); 
		
		//2
		arg = "55";
		sqlchkP("let x = C1[55].addr", "SELECT a.id,a.y,a.cust FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE b.cid = ?", arg); 

		//3
		arg = "111";
		sqlchkP("let x = A1[cust < 111]", "SELECT * FROM A1 as a WHERE a.cust < ?", arg); 
		
		//4
		arg = "111";
		sqlchkP("let x = A1[111].cust", "SELECT a.cid,a.x FROM C1 as a LEFT JOIN A1 as c ON a.cid=c.cust WHERE c.id = ?", arg); 
	}
	
	/**
	 *    1 c[addr]   |  2 c[].addr
	 *    -----------------------
	 *    3 a[cust]   |  4 a[].cust
	 */
	@Test
	public void testBigFour1M() {
		//1
		String arg = "111";
		sqlchkP("let x = CM[addr < 111]", "SELECT a.cid,a.x FROM CM as a LEFT JOIN AM1 as b ON a.cid=b.cust WHERE b.id < ?", arg); 
		
		//2
		arg = "55";
		sqlchkP("let x = CM[55].addr", "SELECT a.id,a.y,a.cust FROM AM1 as a LEFT JOIN CM as b ON a.cust=b.cid WHERE b.cid = ?", arg); 

		//3
		arg = "111";
		sqlchkP("let x = AM1[cust < 111]", "SELECT * FROM AM1 as a WHERE a.cust < ?", arg); 
		
		//4
		arg = "111";
		sqlchkP("let x = AM1[111].cust", "SELECT a.cid,a.x FROM CM as a LEFT JOIN AM1 as c ON a.cid=c.cust WHERE c.id = ?", arg); 
	}
	
	/**
	 *    1 c[addr]   |  2 c[].addr
	 *    -----------------------
	 *    3 a[cust]   |  4 a[].cust
	 */
	@Test
	public void testBigFourMM() {
		//1
		String arg = "111";
		sqlchkP("let x = CMM[addr < 111]", "SELECT a.cid,a.x,c.rightv as addr FROM CMM as a LEFT JOIN CMMAMMDat1 as c ON a.cid=c.leftv WHERE c.rightv < ?", arg); 
//no		sqlchkP("let x = CMM[addr < 111]", "SELECT a.cid,a.x,c.rightv as cust FROM CMM as a LEFT JOIN CMMAMMDat1 as c ON a.cid=c.leftv LEFT JOIN AMM as b ON b.id=c.rightv WHERE c.rightv < ?", arg); 
//TODO: remove unnecessary join of AMM		
		//2
		arg = "55";
		sqlchkP("let x = CMM[55].addr", "SELECT a.id,a.y,c.leftv as cust FROM AMM as a LEFT JOIN CMMAMMDat1 as c ON a.id=c.rightv WHERE c.leftv = ?", arg); 

		//3
		arg = "111";
		sqlchkP("let x = AM1[cust < 111]", "SELECT * FROM AM1 as a WHERE a.cust < ?", arg); 
		
		//4
		arg = "111";
		sqlchkP("let x = AMM[111].cust", "SELECT a.cid,a.x,c.rightv as addr FROM CMM as a LEFT JOIN CMMAMMDat1 as c ON a.cid=c.leftv WHERE c.rightv = ?", arg); 
	}
	
	/**
	 * Need to ensure don't pull in c.leftv when joining on c.leftv.
	 * -self-join. we pull in parent value c.rightv from assoc table
	 * -we should not be pulling in 'child' value c.leftv because its same as a.id so you end up with Customer 55 having 55 as a worker
	 */
	@Test
	public void testSelfJoinMM() {
		String arg = "111";
		//TODO this seems wrong. without fks() we shouldn't be getting c.rightv or e.leftv
		sqlchkP("let x = CMMSelf[addr < 111]", "SELECT a.cid,a.x,c.rightv as addr,e.leftv as cust FROM CMMSelf as a LEFT JOIN CMMSelfCMMSelfDat2 as c ON a.cid=c.leftv WHERE c.rightv < ?", arg); 

		sqlchkP("let x = CMMSelf[true].fks()", "SELECT a.cid,a.x,c.rightv as addr,e.leftv as cust FROM CMMSelf as a LEFT JOIN CMMSelfCMMSelfDat2 as c ON a.cid=c.leftv LEFT JOIN CMMSelfCMMSelfDat2 as e ON a.cid=e.rightv", null); 
	}
	

	@Test
	public void testDebugSQL() {
		sqlchkP("let x = C1[55].addr", "SELECT a.id,a.y,a.cust FROM A1 as a LEFT JOIN C1 as b ON a.cust=b.cid WHERE b.cid = ?", "55"); 
	}

	//---
	
	@Before
	public void init() {
		createDao();
	}
	private void sqlchkP(String src, String sqlExpected, String param1) {
		doSqlchkP(src, sqlExpected, param1);
	}

}
