package org.delia.db.newhls.cud;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.UpdateStatementExp;
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
	public void testMN2a() {
		useCustomerManyToManySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[100] {y: 45, cust:55}");
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 3);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "100");
		//TODO: fix chkUpdateSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 WHERE leftv = ? and rightv <> ?", "55", "100");
		//fixchkUpdateSql(stmgrp, 2, "MERGE INTO CustomerAddressDat1 KEY(leftv) VALUES(?,?)", "55", "100");
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
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 3);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "66", "56");
		chkUpdateSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 WHERE leftv = ? and rightv <> ?", "56", "100");
		chkUpdateSql(stmgrp, 2, "MERGE INTO CustomerAddressDat1 KEY(leftv) VALUES(?,?)", "56", "100"); //wrong. should be 101 i think
	}
	
	
	
	//-------------------------
	protected HLDUpdateStatement buildFromSrcUpdate(String src, int statementIndex) {
		UpdateStatementExp updateExp = compileToUpdateStatement(src, statementIndex);
		log.log(src);
		
		mgr = createManager(); 
		HLDUpdateStatement hldupdate = mgr.fullBuildUpdate(updateExp);
		log.log(hldupdate.toString());
		return hldupdate;
	}

	protected UpdateStatementExp compileToUpdateStatement(String src, int statementIndex) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		List<Exp> list = sessimpl.mostRecentContinueExpL.stream().filter(exp -> exp instanceof UpdateStatementExp).collect(Collectors.toList());
		Exp exp = list.get(statementIndex);
		return (UpdateStatementExp) exp;
	}
	
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
	
	private String addSrc(String src0, String src) {
		return src0 + "\n" + src;
	}
}
