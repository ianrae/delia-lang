package org.delia.db.newhls;


import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class Rel11OtherWayTests extends NewHLSTestBase {
	//one addr
	@Test
	public void testFKS11Parent() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Customer[55].fks()";
		
		HLDQueryStatement hld = buildFromSrc(src, 0); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t0.addr FROM Customer as t0 WHERE t0.cid=?", "55");
	}
	@Test
	public void testFKS11Child() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Address[100].fks()";
		
		HLDQueryStatement hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t1.cid FROM Address as t0 LEFT JOIN Customer as t1 ON t0.id=t1.addr WHERE t0.id=?", "100");
	}	
	
	@Test
	public void testFetch11Parent() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Customer[55].fetch('addr')";
		
		HLDQueryStatement hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t0.addr,t1.id,t1.y FROM Customer as t0 LEFT JOIN Address as t1 ON t0.addr=t1.id WHERE t0.cid=?", "55");
	}	
	@Test
	public void testFetch11Child() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Address[100].fetch('cust')";
		
		HLDQueryStatement hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t1.cid,t1.x,t1.addr FROM Address as t0 LEFT JOIN Customer as t1 ON t0.id=t1.addr WHERE t0.id=?", "100");
	}	
	
	//TODO Customer.addr1 and .addr2 and try fetching one or both
	//two addr
	@Test
	public void testFKS11Parent2Addr() {
		use11TwoAddr = true;
		String src = "let x = Customer[55].fks()";
		
		HLDQueryStatement hld = buildFromSrc(src, 0); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t0.addr1,t0.addr2 FROM Customer as t0 WHERE t0.cid=?", "55");
	}	
	@Test
	public void testFKS11Child2Addr() {
		use11TwoAddr = true;
		String src = "let x = Address[100].fks()";
		
		HLDQueryStatement hld = buildFromSrc(src, 2); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t1.cid,t2.cid FROM Address as t0 LEFT JOIN Customer as t1 ON t0.id=t1.addr1 LEFT JOIN Customer as t2 ON t0.id=t2.addr2 WHERE t0.id=?", "100");
	}	
	
	@Test
	public void testFetch11Parent2Addr() {
		use11TwoAddr = true;
		String src = "let x = Customer[55].fetch('addr1')";
		
		HLDQueryStatement hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t0.addr1,t0.addr2,t1.id,t1.y FROM Customer as t0 LEFT JOIN Address as t1 ON t0.addr1=t1.id WHERE t0.cid=?", "55");
	}
	@Test
	public void testFetch11Parent2AddrB() {
		use11TwoAddr = true;
		String src = "let x = Customer[55].fetch('addr1', 'addr2')";
		
		HLDQueryStatement hld = buildFromSrc(src, 2); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t0.addr1,t0.addr2,t1.id,t1.y,t2.id,t2.y FROM Customer as t0 LEFT JOIN Address as t1 ON t0.addr1=t1.id LEFT JOIN Address as t2 ON t0.addr2=t2.id WHERE t0.cid=?", "55");
	}
	
	@Test
	public void testFetch11Child2Addr() {
		use11TwoAddr = true;
		String src = "let x = Address[100].fetch('cust1')";
		
		HLDQueryStatement hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t1.cid,t1.x,t1.addr1,t1.addr2 FROM Address as t0 LEFT JOIN Customer as t1 ON t0.id=t1.addr1 WHERE t0.id=?", "100");
	}	

	//implicit fetch
	@Test
	public void testImplicitOrderBy() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Customer[55].orderBy('addr')";
		
		HLDQueryStatement hld = buildFromSrc(src, 0); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t0.addr FROM Customer as t0 WHERE t0.cid=? ORDER BY t0.addr", "55");
	}
	@Test
	public void testImplicitFilter() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Customer[addr.y == 55]";
		
		HLDQueryStatement hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t0.addr FROM Customer as t0 LEFT JOIN Address as t1 ON t0.addr=t1.id WHERE t1.y = ?", "55");
	}
	//then do let x = Customer[addr.y == 55].orderBy('addr') and ensure not two joins!
	@Test
	public void testImplicitFilter2() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Customer[addr.y == 55].orderBy('addr')";
		
		HLDQueryStatement hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t0.addr FROM Customer as t0 LEFT JOIN Address as t1 ON t0.addr=t1.id WHERE t1.y = ? ORDER BY t0.addr", "55");
	}
	
	//-------------------------
	private boolean use11TwoAddr;
	
	@Override
	protected String buildSrc() {
		if (use11TwoAddr) {
			String src = " type Customer struct {cid int unique, x int, relation addr1 Address 'a1' one optional , relation addr2 Address 'a2' one optional  } end";
			src += "\n type Address struct {id int unique, y int, relation cust1 Customer 'a1' one optional parent, relation cust2 Customer 'a2' one optional parent } end";
			return src;
		} else {
			return super.buildSrc();
		}
	}


}
