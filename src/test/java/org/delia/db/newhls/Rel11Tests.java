package org.delia.db.newhls;


import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class Rel11Tests extends NewHLSTestBase {
	//one addr
	@Test
	public void testFKS11Parent() {
		useCustomer11Src = true;
		String src = "let x = Customer[55].fks()";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.id FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust WHERE t0.cid=?", "55");
	}
	@Test
	public void testFKS11Child() {
		useCustomer11Src = true;
		String src = "let x = Address[100].fks()";
		
		HLDQuery hld = buildFromSrc(src, 0); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t0.cust FROM Address as t0 WHERE t0.id=?", "100");
	}	
	
	@Test
	public void testFetch11Parent() {
		useCustomer11Src = true;
		String src = "let x = Customer[55].fetch('addr')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.id,t1.id,t1.y,t1.cust FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust WHERE t0.cid=?", "55");
	}	
	@Test
	public void testFetch11Child() {
		useCustomer11Src = true;
		String src = "let x = Address[100].fetch('cust')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t0.cust,t1.cid,t1.x FROM Address as t0 JOIN Customer as t1 ON t0.cust=t1.cid WHERE t0.id=?", "100");
	}	
	
	//TODO Customer.addr1 and .addr2 and try fetching one or both
	//two addr
	@Test
	public void testFKS11Parent2Addr() {
		use11TwoAddr = true;
		String src = "let x = Customer[55].fks()";
		
		HLDQuery hld = buildFromSrc(src, 2); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.id,t2.id FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust1 JOIN Address as t2 ON t0.cid=t2.cust2 WHERE t0.cid=?", "55");
	}	
	@Test
	public void testFKS11Child2Addr() {
		use11TwoAddr = true;
		String src = "let x = Address[100].fks()";
		
		HLDQuery hld = buildFromSrc(src, 0); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t0.cust1,t0.cust2 FROM Address as t0 WHERE t0.id=?", "100");
	}	
	
	@Test
	public void testFetch11Parent2Addr() {
		use11TwoAddr = true;
		String src = "let x = Customer[55].fetch('addr1')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.id,t1.id,t1.y,t1.cust1,t1.cust2 FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust1 WHERE t0.cid=?", "55");
	}
	@Test
	public void testFetch11Parent2AddrB() {
		use11TwoAddr = true;
		String src = "let x = Customer[55].fetch('addr1', 'addr2')";
		
		HLDQuery hld = buildFromSrc(src, 2); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.id,t2.id,t1.id,t1.y,t1.cust1,t1.cust2,t2.id,t2.y,t2.cust1,t2.cust2 FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust1 JOIN Address as t2 ON t0.cid=t2.cust2 WHERE t0.cid=?", "55");
	}
	
	@Test
	public void testFetch11Child2Addr() {
		use11TwoAddr = true;
		String src = "let x = Address[100].fetch('cust1')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t0.cust1,t0.cust2,t1.cid,t1.x FROM Address as t0 JOIN Customer as t1 ON t0.cust1=t1.cid WHERE t0.id=?", "100");
	}	

	//implicit fetch
	@Test
	public void testImplicitOrderBy() {
		useCustomer11Src = true;
		String src = "let x = Customer[55].orderBy('addr')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust WHERE t0.cid=? ORDER BY t1.cust", "55");
	}
	@Test
	public void testImplicitFilter() {
		useCustomer11Src = true;
		String src = "let x = Customer[addr.y == 55]";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust WHERE t1.y == ?", "55");
	}
	//then do let x = Customer[addr.y == 55].orderBy('addr') and ensure not two joins!
	@Test
	public void testImplicitFilter2() {
		useCustomer11Src = true;
		String src = "let x = Customer[addr.y == 55].orderBy('addr')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x FROM Customer as t0 JOIN Address as t1 ON t0.cid=t1.cust WHERE t1.y == ? ORDER BY t1.cust", "55");
	}
	
	//-------------------------
	private boolean use11TwoAddr;
	
	@Override
	protected String buildSrc() {
		if (use11TwoAddr) {
			String src = " type Customer struct {cid int unique, x int, relation addr1 Address 'a1' one optional parent, relation addr2 Address 'a2' one optional parent } end";
			src += "\n type Address struct {id int unique, y int, relation cust1 Customer 'a1' one optional, relation cust2 Customer 'a2' one optional } end";
			return src;
		} else {
			return super.buildSrc();
		}
	}


}
