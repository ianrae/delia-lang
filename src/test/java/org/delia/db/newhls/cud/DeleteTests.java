package org.delia.db.newhls.cud;


import static org.junit.Assert.assertEquals;

import org.delia.db.newhls.NewHLSTestBase;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.junit.Test;

/**
 * TODO: when delete also delete DAT table, and non-optional child
 * @author Ian Rae
 *
 */
public class DeleteTests extends NewHLSTestBase {
	
	// --- filter tests ---
	@Test
	public void test1() {
		useCustomer11Src = true;
		String src = "delete Customer[55]";
		
		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		
		chkDeleteSql(stmgrp, 0, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.cust = ?", null, "55");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.cid=?", "55");
	}
	@Test
	public void test1MandatoryChild() {
		useCustomer11MandatoryChildSrc = true;
		String src = "delete Customer[55]";
		
		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t1 WHERE t1.cust = ?", "55");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.cid=?", "55");
	}
	
	@Test
	public void test2() {
		useCustomer11Src = true;
		String src = "delete Address[100]";
		
		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 1);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}
	@Test
	public void test2Mandatory() {
		useCustomer11MandatoryChildSrc = true;
		String src = "delete Address[100]";
		
		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		//DELETE from customer WHERE cid IN (SELECT t2.cust FROM Address as t2 WHERE t2.id = ?) 100
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}
	@Test
	public void test3() {
		useCustomer11Src = true;
		String src = "delete Customer[x > 10]";
		
		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.cust IN (SELECT t2.cid FROM Customer as t2 WHERE t2.x > ?)", null, "10");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.x > ?", "10");
	}
	
	
	//TODO: ****************** all remaining tests need to be done
	// --- 1:1 ---
	@Test
	public void test11a() {
		useCustomer11Src = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[1] {y: 45, cust:55}");
		src = addSrc(src, "delete Address[1]");
		
		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 1);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t0 WHERE t0.id=?", "1");
	}
	@Test
	public void test11aM() {
		useCustomer11MandatoryChildSrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[1] {y: 45, cust:55}");
		src = addSrc(src, "delete Address[1]");
		
		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 1);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t0 WHERE t0.id=?", "1");
	}
	@Test
	public void test11b() {
		useCustomer11Src = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[1] {y: 45, cust:55}");
		src = addSrc(src, "delete Customer[55]");
		
		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 1);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "UPDATE Address as t0 SET t0.cust = null WHERE t0.cust=?", "55");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t1 WHERE t1.id=?", "55");
	}
	//TODO make test when child is not optional
	
//	// --- 1:N ---
//	@Test
//	public void test1N() {
//		useCustomer1NSrc = true;
//		String src = "update Customer[1] {x: 45}";
//		
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		chkDeleteSql(hlddelete, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "45", "1");
//	}
//	@Test
//	public void test1N2() {
//		useCustomer1NSrc = true;
//		String src = "update Address[100] {y: 45}";
//		
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		chkDeleteSql(hlddelete, 1, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "100");
//	}
//	@Test
//	public void test1N2a() {
//		useCustomer1NSrc = true;
//		String src0 = "insert Customer {cid: 55, x: 45}";
//		String src = addSrc(src0, "update Address[100] {y: 45, cust:55}");
//		
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		SqlStatementGroup stmgrp = genUpdateSql(hlddelete, 1);
//		dumpGrp(stmgrp);
//		
//		chkDeleteSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ?, t0.cust = ? WHERE t0.id=?", "45", "55", "100");
//	}
//	@Test
//	public void test1NInsertParent() {
//		//adapted from t0-insert-parent.txt: add workers
//		useCustomer1NSrc = true;
//		String src0 = "insert Customer {cid: 55, x: 45}";
//		String src = addSrc(src0, "insert Address {id: 100, y: 45}");
//		src = addSrc(src, "insert Address {id: '101', y:46 }");
//		src = addSrc(src, "update Customer[56] {x:66, addr: ['100','101'] }");
//		
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		SqlStatementGroup stmgrp = genUpdateSql(hlddelete, 3);
//		dumpGrp(stmgrp);
//		chkDeleteSql(stmgrp, 0, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "66", "56");
//		chkDeleteSql(stmgrp, 1, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.id=?", "56", "100");
//		chkDeleteSql(stmgrp, 2, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.id=?", "56", "101");
//	}
//	
//	// --- M:N ---
//	@Test
//	public void testMN() {
//		useCustomerManyToManySrc = true;
//		String src = "update Customer[1] { x: 45}";
//		
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		chkDeleteSql(hlddelete, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "45", "1");
//	}
//	
//	@Test
//	public void testMN2() {
//		useCustomerManyToManySrc = true;
//		String src = "update Address[1] { y: 45}";
//		
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		chkDeleteSql(hlddelete, 1, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "1");
//	}
//	@Test
//	public void testMNScenario1() {
//		useCustomerManyToManySrc = true;
//		String src0 = "insert Customer {cid: 55, x: 45}";
//		String src = addSrc(src0, "update Address[100] {y: 45, cust:55}");
//		//update by pk
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		SqlStatementGroup stmgrp = genUpdateSql(hlddelete, 3);
//		dumpGrp(stmgrp);
//		chkDeleteSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "100");
//		chkDeleteSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.rightv = ? AND t1.leftv <> ?", "100", "55");
//		chkDeleteSql(stmgrp, 2, "MERGE INTO CustomerAddressDat1 as t1 KEY(rightv) VALUES ?, ?", "55", "100");
//	}
//	@Test
//	public void testMNScenario2() {
//		useCustomerManyToManySrc = true;
//		String src0 = "insert Customer {cid: 55, x: 45}";
//		String src = addSrc(src0, "update Address[true] {y: 45, cust:55}");
//		//update all
//		
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		SqlStatementGroup stmgrp = genUpdateSql(hlddelete, 3);
//		dumpGrp(stmgrp);
//		chkDeleteSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ?", "45");
//		chkDeleteSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1");
//		String s = "MERGE INTO CustomerAddressDat1 as t1 USING (SELECT cid FROM Customer) AS S ON t1.rightv = s.cid WHEN MATCHED THEN UPDATE SET t1.leftv = ? WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.cid, ?)";
//		chkDeleteSql(stmgrp, 2, s, "55", "55");
//	}
//	@Test
//	public void testMNScenario3() {
//		useCustomerManyToManySrc = true;
//		String src0 = "insert Customer {cid: 55, x: 45}";
//		String src = addSrc(src0, "update Address[y > 10] {y: 45, cust:55}");
//		//update filter
//		
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		SqlStatementGroup stmgrp = genUpdateSql(hlddelete, 3);
//		dumpGrp(stmgrp);
//		chkDeleteSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ? WHERE t0.y > ?", "45", "10");
//		chkDeleteSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv <> ? AND t1.rightv IN (SELECT cid FROM Customer as a WHERE a.y > ?)", "55", "10");
//		String s = "WITH cte1 AS (SELECT ? as leftv, id as rightv FROM CustomerINSERT INTO CustomerAddressDat1 as t1 SELECT * from cte1";
//		chkDeleteSql(stmgrp, 2, s, "55");
//	}
//	
//	
//	@Test
//	public void testMNUpdateParent() {
//		//adapted from t0-insert-parent.txt: add workers
//		useCustomerManyToManySrc = true;
//		String src0 = "insert Customer {cid: 55, x: 45}";
//		String src = addSrc(src0, "insert Address {id: 100, y: 45}");
//		src = addSrc(src, "insert Address {id: '101', y:46 }");
//		src = addSrc(src, "update Customer[56] { x:66, addr: ['100','101'] }");
//		
//		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
//		SqlStatementGroup stmgrp = genUpdateSql(hlddelete, 5);
//		dumpGrp(stmgrp);
//		chkDeleteSql(stmgrp, 0, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "66", "56");
//		chkDeleteSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv = ? AND t1.rightv <> ?", "56", "100");
//		chkDeleteSql(stmgrp, 2, "MERGE INTO CustomerAddressDat1 as t1 KEY(leftv) VALUES ?, ?", "56", "100"); 
//		chkDeleteSql(stmgrp, 3, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv = ? AND t1.rightv <> ?", "56", "101");
//		chkDeleteSql(stmgrp, 4, "MERGE INTO CustomerAddressDat1 as t1 KEY(leftv) VALUES ?, ?", "56", "101");
//		//TODO: the above is correct but not efficient. only need a single:
//		//DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv = 56
//	}
	
	
	//-------------------------
	private boolean useCustomer11MandatoryChildSrc;
	
	@Override
	protected String buildSrc() {
		if (useCustomer11MandatoryChildSrc) {
			return buildCustomer11MandatoryChildSrc();
		} else {
			return super.buildSrc();
		}
	}

	
	private String buildCustomer11MandatoryChildSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional parent  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one  } end";
		return src;
	}
	
	
	protected SqlStatementGroup genDeleteSql(HLDDeleteStatement hldupdate, int numStatements) {
		SqlStatementGroup stmgrp = mgr.generateSql(hldupdate);
		assertEquals(numStatements, stmgrp.statementL.size());
		return stmgrp;
	}
	protected void chkDeleteSql(HLDDeleteStatement hlddel, String expected, String...args) {
		SqlStatementGroup stmgrp = mgr.generateSql(hlddel);
		SqlStatement stm = stmgrp.statementL.get(0);
		chkStm(stm, expected, args);
	}
	protected void chkDeleteSql(SqlStatementGroup stmgrp, int index, String expected, String...args) {
		SqlStatement stm = stmgrp.statementL.get(index);
		chkStm(stm, expected, args);
	}
}
