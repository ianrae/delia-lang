package org.delia.db.hld;


import org.delia.hld.HLDQueryStatement;
import org.junit.Test;

/**
 * @author Ian Rae
 *
 */
public class Rel11OtherWayFinalFieldTests extends NewHLSTestBase {
	@Test
	public void testScalar() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Customer[55].x";
		
		HLDQueryStatement hld = buildFromSrc(src, 0); 
		chkFullSql(hld, "SELECT t0.x FROM Customer as t0 WHERE t0.cid=?", "55");
	}
	@Test
	public void testRelParent() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Customer[55].addr";
		
		HLDQueryStatement hld = buildFromSrc(src, 0); 
		chkFullSql(hld, "SELECT t0.addr FROM Customer as t0 WHERE t0.cid=?", "55");
	}
	@Test
	public void testRelChild() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Address[100].cust";
		
		HLDQueryStatement hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t1.addr FROM Address as t0 LEFT JOIN Customer as t1 ON t0.id=t1.addr WHERE t0.id=?", "100");
	}
	
	//-- through chain
	@Test
	public void testTCScalar() {
		useCustomer11OtherWaySrc = true;
		String src = "let x = Customer[55].addr.y";
		
		HLDQueryStatement hld = buildFromSrc(src, 1); 
		chkFullSql(hld, "SELECT t1.y FROM Customer as t0 LEFT JOIN Address as t1 ON t0.addr=t1.id WHERE t0.cid=?", "55");
	}
//	@Test
//	public void testTCRelParent() {
//		useCustomer11OtherWaySrc = true;
//		String src = "let x = Customer[55].addr";
//		
//		HLDQuery hld = buildFromSrc(src, 1); 
//		chkFullSql(hld, "SELECT t1.cust FROM Customer as t0 LEFT JOIN Address as t1 ON t0.cid=t1.cust WHERE t0.cid=?", "55");
//	}
//	@Test
//	public void testTCRelChild() {
//		useCustomer11OtherWaySrc = true;
//		String src = "let x = Address[100].cust";
//		
//		HLDQuery hld = buildFromSrc(src, 0); 
//		chkFullSql(hld, "SELECT t0.cust FROM Address as t0 WHERE t0.id=?", "100");
//	}
	
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
