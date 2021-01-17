package org.delia.db.newhls.cud;

import static org.junit.Assert.assertEquals;

import org.delia.db.newhls.NewHLSTestBase;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class UpdateTests extends NewHLSTestBase {
	
	// --- 1:1 ---
	@Test
	public void test1() {
		useCustomer11Src = true;
		String src = "update Customer[1] {x: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "45", "1");
	}
	@Test
	public void test2() {
		useCustomer11Src = true;
		String src = "update Address[100] { y: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "100");
	}
	@Test
	public void test2a() {
		useCustomer11Src = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[1] {y: 45, cust:55}");
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 1);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ?, t0.cust = ? WHERE t0.id=?", "45", "55", "1");
	}
	
	// --- 1:N ---
	@Test
	public void test1N() {
		useCustomer1NSrc = true;
		String src = "update Customer[1] {x: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "45", "1");
	}
	@Test
	public void test1N2() {
		useCustomer1NSrc = true;
		String src = "update Address[100] {y: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "100");
	}
	@Test
	public void test1N2a() {
		useCustomer1NSrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[100] {y: 45, cust:55}");
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 1);
		dumpGrp(stmgrp);
		
		chkUpdateSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ?, t0.cust = ? WHERE t0.id=?", "45", "55", "100");
	}
	@Test
	public void test1NInsertParent() {
		//adapted from t0-insert-parent.txt: add workers
		useCustomer1NSrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 100, y: 45}");
		src = addSrc(src, "insert Address {id: '101', y:46 }");
		src = addSrc(src, "update Customer[56] {x:66, addr: ['100','101'] }");
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 3);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "66", "56");
		chkUpdateSql(stmgrp, 1, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.id=?", "56", "100");
		chkUpdateSql(stmgrp, 2, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.id=?", "56", "101");
	}
	
	// --- M:N ---
	@Test
	public void testMN() {
		useCustomerManyToManySrc = true;
		String src = "update Customer[1] { x: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "45", "1");
	}
	
	@Test
	public void testMN2() {
		useCustomerManyToManySrc = true;
		String src = "update Address[1] { y: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "1");
	}
	@Test
	public void testMNScenario1() {
		useCustomerManyToManySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[100] {y: 45, cust:55}");
		//update by pk
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 3);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "100");
		chkUpdateSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.rightv = ? AND t1.leftv <> ?", "100", "55");
		chkUpdateSql(stmgrp, 2, "MERGE INTO CustomerAddressDat1 as t1 KEY(rightv) VALUES ?, ?", "55", "100");
	}
	@Test
	public void testMNScenario2() {
		useCustomerManyToManySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[true] {y: 45, cust:55}");
		//update all
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 3);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ?", "45");
		chkUpdateSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1");
		String s = "MERGE INTO CustomerAddressDat1 as t1 USING (SELECT cid FROM Customer) AS S ON t1.rightv = s.cid WHEN MATCHED THEN UPDATE SET t1.leftv = ? WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.cid, ?)";
		chkUpdateSql(stmgrp, 2, s, "55", "55");
	}
	@Test
	public void testMNScenario3() {
		useCustomerManyToManySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[y > 10] {y: 45, cust:55}");
		//update filter
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 3);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ? WHERE t0.y > ?", "45", "10");
		chkUpdateSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv <> ? AND t1.rightv IN (SELECT cid FROM Customer as a WHERE a.y > ?)", "55", "10");
		String s = "WITH cte1 AS (SELECT ? as leftv, id as rightv FROM CustomerINSERT INTO CustomerAddressDat1 as t1 SELECT * from cte1";
		chkUpdateSql(stmgrp, 2, s, "55");
	}
	
	
	@Test
	public void testMNUpdateParent() {
		//adapted from t0-insert-parent.txt: add workers
		useCustomerManyToManySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 100, y: 45}");
		src = addSrc(src, "insert Address {id: '101', y:46 }");
		src = addSrc(src, "update Customer[56] { x:66, addr: ['100','101'] }");
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 5);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "66", "56");
		chkUpdateSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv = ? AND t1.rightv <> ?", "56", "100");
		chkUpdateSql(stmgrp, 2, "MERGE INTO CustomerAddressDat1 as t1 KEY(leftv) VALUES ?, ?", "56", "100"); 
		chkUpdateSql(stmgrp, 3, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv = ? AND t1.rightv <> ?", "56", "101");
		chkUpdateSql(stmgrp, 4, "MERGE INTO CustomerAddressDat1 as t1 KEY(leftv) VALUES ?, ?", "56", "101");
		//TODO: the above is correct but not efficient. only need a single:
		//DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv = 56
	}
	
	
	
	//-------------------------
	
	protected void chkUpdateSql(HLDUpdateStatement hldupdate, int numStatements, String expected, String...args) {
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, numStatements);
		SqlStatement stm = stmgrp.statementL.get(0);
		chkStm(stm, expected, args);
	}

	protected SqlStatementGroup genUpdateSql(HLDUpdateStatement hldupdate, int numStatements) {
		SqlStatementGroup stmgrp = mgr.generateSql(hldupdate);
		assertEquals(numStatements, stmgrp.statementL.size());
		return stmgrp;
	}
	protected void chkUpdateSql(SqlStatementGroup stmgrp, int index, String expected, String...args) {
		SqlStatement stm = stmgrp.statementL.get(index);
		chkStm(stm, expected, args);
	}
	
}
