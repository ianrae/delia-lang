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
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "1", "45");
	}
	@Test
	public void test2() {
		useCustomer11Src = true;
		String src = "insert Address {id: 1, y: 45}";
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "INSERT INTO Address (id, y) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void test2a() {
		useCustomer11Src = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 1, y: 45, cust:55}");
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 1); 
		chkUpdateSql(hldupdate, 1, "INSERT INTO Address (id, y, cust) VALUES(?, ?, ?)", "1", "45", "55");
	}
	
	// --- 1:N ---
	@Test
	public void test1N() {
		useCustomer1NSrc = true;
		String src = "insert Customer {cid: 1, x: 45}";
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "INSERT INTO Customer (cid, x) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void test1N2() {
		useCustomer1NSrc = true;
		String src = "insert Address {id: 1, y: 45}";
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "INSERT INTO Address (id, y) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void test1N2a() {
		useCustomer1NSrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 1, y: 45, cust:55}");
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 1); 
		chkUpdateSql(hldupdate, 1, "INSERT INTO Address (id, y, cust) VALUES(?, ?, ?)", "1", "45", "55");
	}
	@Test
	public void test1NInsertParent() {
		//adapted from t0-insert-parent.txt: add workers
		useCustomer1NSrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 100, y: 45}");
		src = addSrc(src, "insert Address {id: '101', y:46 }");
		src = addSrc(src, "insert Customer {cid: 56, x:66, addr: ['100','101'] }");
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 3); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 3);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "INSERT INTO Customer as t0 (t0.cid, t0.x) VALUES(?, ?)", "56", "66");
		chkUpdateSql(stmgrp, 1, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.id=?", "56", "100");
		chkUpdateSql(stmgrp, 2, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.id=?", "56", "101");
	}
	
	// --- M:N ---
	@Test
	public void testMN() {
		useCustomerManyToManySrc = true;
		String src = "insert Customer {cid: 1, x: 45}";
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "INSERT INTO Customer (cid, x) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void testMN2() {
		useCustomerManyToManySrc = true;
		String src = "insert Address {id: 1, y: 45}";
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "INSERT INTO Address (id, y) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void testMN2a() {
		useCustomerManyToManySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 100, y: 45, cust:55}");
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 1); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 2);
		chkUpdateSql(stmgrp, 0, "INSERT INTO Address (id, y) VALUES(?, ?)", "100", "45");
		chkUpdateSql(stmgrp, 1, "INSERT INTO CustomerAddressDat1 (leftv, rightv) VALUES(?, ?)", "55", "100");
	}
	
	@Test
	public void testMNInsertParent() {
		//adapted from t0-insert-parent.txt: add workers
		useCustomerManyToManySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 100, y: 45}");
		src = addSrc(src, "insert Address {id: '101', y:46 }");
		src = addSrc(src, "insert Customer {cid: 56, x:66, addr: ['100','101'] }");
		
		HLDUpdate hldupdate = buildFromSrcUpdate(src, 3); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 3);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "INSERT INTO Customer (cid, x) VALUES(?, ?)", "56", "66");
		chkUpdateSql(stmgrp, 1, "INSERT INTO CustomerAddressDat1 (leftv, rightv) VALUES(?, ?)", "56", "100");
		chkUpdateSql(stmgrp, 2, "INSERT INTO CustomerAddressDat1 (leftv, rightv) VALUES(?, ?)", "56", "101");
	}
	
	
	private void dumpGrp(SqlStatementGroup stmgrp) {
		log.log("grp: %s", stmgrp.statementL.size());
		for(SqlStatement stm: stmgrp.statementL) {
			log.log(stm.sql);
		}
	}
	
	
	//-------------------------
	protected HLDUpdate buildFromSrcUpdate(String src, int statementIndex) {
		UpdateStatementExp updateExp = compileToUpdateStatement(src, statementIndex);
		log.log(src);
		
		mgr = createManager(); 
		HLDUpdate hldupdate = mgr.fullBuildUpdate(updateExp);
		log.log(hldupdate.toString());
		return hldupdate;
	}

	protected UpdateStatementExp compileToUpdateStatement(String src, int statementIndex) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		List<Exp> list = sessimpl.mostRecentContinueExpL.stream().filter(exp -> exp instanceof UpdateStatementExp).collect(Collectors.toList());
		Exp exp = list.get(statementIndex);
		return (UpdateStatementExp) exp;
	}
	
	protected void chkUpdateSql(HLDUpdate hldupdate, int numStatements, String expected, String...args) {
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, numStatements);
		SqlStatement stm = stmgrp.statementL.get(0);
		chkStm(stm, expected, args);
	}

	protected SqlStatementGroup genUpdateSql(HLDUpdate hldupdate, int numStatements) {
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
