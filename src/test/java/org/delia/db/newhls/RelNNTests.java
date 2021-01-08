package org.delia.db.newhls;


import org.junit.Test;

/**
 * DAT: Customer.addr -> datId: 1 (table: CustomerAddressDat1) left:Customer.addr, right:Address.cust)
 *  this means:
 *   CustomerAddressDat1 left side holds Customer is, right side is Address ids
 *   left:Customer.addr means we put customer id here
 *   left:Address.cust means we put address id here
 * @author Ian Rae
 *
 */
public class RelNNTests extends NewHLSTestBase {
	//one addr
	@Test
	public void testFKS11Parent() {
		useCustomerManyToManySrc = true;
		String src = "let x = Customer[55].fks()";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.rightv FROM Customer as t0 JOIN CustomerAddressDat1 as t1 ON t0.cid=t1.leftv WHERE t0.cid=?", "55");
	}
	@Test
	public void testFKS11Child() {
		useCustomerManyToManySrc = true;
		String src = "let x = Address[100].fks()";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t1.leftv FROM Address as t0 JOIN CustomerAddressDat1 as t1 ON t0.id=t1.rightv WHERE t0.id=?", "100");
	}	
	
	@Test
	public void testFetch11Parent() {
		useCustomerManyToManySrc = true;
		String src = "let x = Customer[55].fetch('addr')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		// t1.rightv as addr -- is 'as addr' needed?
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.rightv,t2.id,t2.y,t1.leftv FROM Customer as t0 JOIN CustomerAddressDat1 as t1 ON t0.cid=t1.leftv JOIN Address as t2 ON t1.rightv=t2.id WHERE t0.cid=?", "55");
	}	
	@Test
	public void testFetch11Child() {
		useCustomerManyToManySrc = true;
		String src = "let x = Address[100].fetch('cust')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t1.leftv,t2.cid,t2.x,t1.rightv FROM Address as t0 JOIN CustomerAddressDat1 as t1 ON t0.id=t1.rightv JOIN Customer as t2 ON t1.leftv=t2.cid WHERE t0.id=?", "100");
	}	
	
	//TODO Customer.addr1 and .addr2 and try fetching one or both
	//two addr
	@Test
	public void testFKS11Parent2Addr() {
		use11TwoAddr = true;
		String src = "let x = Customer[55].fks()";
		
		HLDQuery hld = buildFromSrc(src, 2); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.rightv,t2.rightv FROM Customer as t0 JOIN CustomerAddressDat1 as t1 ON t0.cid=t1.leftv JOIN CustomerAddressDat2 as t2 ON t0.cid=t2.leftv WHERE t0.cid=?", "55");
	}	
	@Test
	public void testFKS11Child2Addr() {
		use11TwoAddr = true;
		String src = "let x = Address[100].fks()";
		
		HLDQuery hld = buildFromSrc(src, 2); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t1.leftv,t2.leftv FROM Address as t0 JOIN CustomerAddressDat1 as t1 ON t0.id=t1.rightv JOIN CustomerAddressDat2 as t2 ON t0.id=t2.rightv WHERE t0.id=?", "100");
	}	
	
	@Test
	public void testFetch11Parent2Addr() {
		use11TwoAddr = true;
		String src = "let x = Customer[55].fetch('addr1')";
		//here
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.rightv,t2.id,t2.y,t1.leftv FROM Customer as t0 JOIN CustomerAddressDat1 as t1 ON t0.cid=t1.leftv JOIN Address as t2 ON t1.rightv=t2.id WHERE t0.cid=?", "55");
	}
	@Test
	public void testFetch11Parent2AddrB() {
		use11TwoAddr = true;
		String src = "let x = Customer[55].fetch('addr1', 'addr2')";
		
		HLDQuery hld = buildFromSrc(src, 2); 
		chkFullSql(hld, "SELECT t0.cid,t0.x,t1.rightv,t3.rightv,t2.id,t2.y,t1.leftv,t1.leftv,t4.id,t4.y,t3.leftv,t3.leftv FROM Customer as t0 JOIN CustomerAddressDat1 as t1 ON t0.cid=t1.leftv JOIN Address as t2 ON t1.rightv=t2.id JOIN CustomerAddressDat2 as t3 ON t0.cid=t3.leftv JOIN Address as t4 ON t3.rightv=t4.id WHERE t0.cid=?", "55");
	}
	
	@Test
	public void testFetch11Child2Addr() {
		use11TwoAddr = true;
		String src = "let x = Address[100].fetch('cust1')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.id,t0.y,t1.leftv,t2.cid,t2.x,t1.rightv FROM Address as t0 JOIN CustomerAddressDat1 as t1 ON t0.id=t1.rightv JOIN Customer as t2 ON t1.leftv=t2.cid WHERE t0.id=?", "100");
	}	

	//implicit fetch
	@Test
	public void testImplicitOrderBy() {
		useCustomerManyToManySrc = true;
		String src = "let x = Customer[55].orderBy('addr')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x FROM Customer as t0 JOIN CustomerAddressDat1 as t1 ON t0.cid=t1.leftv WHERE t0.cid=? ORDER BY t1.rightv", "55");
	}
	@Test
	public void testImplicitFilter() {
		useCustomerManyToManySrc = true;
		String src = "let x = Customer[addr.y == 55]";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x FROM Customer as t0 JOIN CustomerAddressDat1 as t1 ON t0.cid=t1.leftv JOIN Address as t2 ON t1.rightv=t2.id WHERE t2.y == ?", "55");
	}
	@Test
	public void testImplicitFilter2() {
		useCustomerManyToManySrc = true;
		String src = "let x = Customer[addr.y == 55].orderBy('addr')";
		
		HLDQuery hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t0.cid,t0.x FROM Customer as t0 JOIN CustomerAddressDat1 as t1 ON t0.cid=t1.leftv JOIN Address as t2 ON t1.rightv=t2.id WHERE t2.y == ? ORDER BY t1.rightv", "55");
	}
	
	//-------------------------
	private boolean use11TwoAddr;
	
	@Override
	protected String buildSrc() {
		if (use11TwoAddr) {
			String src = " type Customer struct {cid int unique, x int, relation addr1 Address 'a1' many optional, relation addr2 Address 'a2' many optional} end";
			src += "\n type Address struct {id int unique, y int, relation cust1 Customer 'a1' many optional, relation cust2 Customer 'a2' many optional } end";
			return src;
		} else {
			return super.buildSrc();
		}
	}


}
