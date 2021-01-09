package org.delia.db.newhls.cud;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.db.newhls.NewHLSTestBase;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class InsertTests extends NewHLSTestBase {
	
	// --- 1:1 ---
	@Test
	public void test1() {
		useCustomer11Src = true;
		String src = "insert Customer {cid: 1, x: 45}";
		
		HLDInsert hldins = buildFromSrcInsert(src, 0); 
		chkInsertSql(hldins, 1, "INSERT INTO Customer (cid, x) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void test2() {
		useCustomer11Src = true;
		String src = "insert Address {id: 1, y: 45}";
		
		HLDInsert hldins = buildFromSrcInsert(src, 0); 
		chkInsertSql(hldins, 1, "INSERT INTO Address (id, y) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void test2a() {
		useCustomer11Src = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 1, y: 45, cust:55}");
		
		HLDInsert hldins = buildFromSrcInsert(src, 1); 
		chkInsertSql(hldins, 1, "INSERT INTO Address (id, y, cust) VALUES(?, ?, ?)", "1", "45", "55");
	}
	
	// --- 1:N ---
	@Test
	public void test1N() {
		useCustomer1NSrc = true;
		String src = "insert Customer {cid: 1, x: 45}";
		
		HLDInsert hldins = buildFromSrcInsert(src, 0); 
		chkInsertSql(hldins, 1, "INSERT INTO Customer (cid, x) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void test1N2() {
		useCustomer1NSrc = true;
		String src = "insert Address {id: 1, y: 45}";
		
		HLDInsert hldins = buildFromSrcInsert(src, 0); 
		chkInsertSql(hldins, 1, "INSERT INTO Address (id, y) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void test1N2a() {
		useCustomer1NSrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 1, y: 45, cust:55}");
		
		HLDInsert hldins = buildFromSrcInsert(src, 1); 
		chkInsertSql(hldins, 1, "INSERT INTO Address (id, y, cust) VALUES(?, ?, ?)", "1", "45", "55");
	}
	@Test
	public void test1NInsertParent() {
		//adapted from t0-insert-parent.txt: add workers
		useCustomer1NSrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 100, y: 45}");
		src = addSrc(src, "insert Address {id: '101', y:46 }");
		src = addSrc(src, "insert Customer {cid: 56, x:66, addr: ['100','101'] }");
		
		HLDInsert hldins = buildFromSrcInsert(src, 3); 
		SqlStatementGroup stmgrp = genInsertSql(hldins, 3);
		dumpGrp(stmgrp);
		chkInsertSql(stmgrp, 0, "INSERT INTO Customer (cid, x) VALUES(?, ?)", "56", "66");
		chkInsertSql(stmgrp, 1, "UPDATE Address SET cust = ? WHERE id = ?", "56", "100");
		chkInsertSql(stmgrp, 2, "UPDATE Address SET cust = ? WHERE id = ?", "56", "101");
	}
	
	// --- M:N ---
	@Test
	public void testMN() {
		useCustomerManyToManySrc = true;
		String src = "insert Customer {cid: 1, x: 45}";
		
		HLDInsert hldins = buildFromSrcInsert(src, 0); 
		chkInsertSql(hldins, 1, "INSERT INTO Customer (cid, x) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void testMN2() {
		useCustomerManyToManySrc = true;
		String src = "insert Address {id: 1, y: 45}";
		
		HLDInsert hldins = buildFromSrcInsert(src, 0); 
		chkInsertSql(hldins, 1, "INSERT INTO Address (id, y) VALUES(?, ?)", "1", "45");
	}
	@Test
	public void testMN2a() {
		useCustomerManyToManySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 100, y: 45, cust:55}");
		
		HLDInsert hldins = buildFromSrcInsert(src, 1); 
		SqlStatementGroup stmgrp = genInsertSql(hldins, 2);
		chkInsertSql(stmgrp, 0, "INSERT INTO Address (id, y) VALUES(?, ?)", "100", "45");
		chkInsertSql(stmgrp, 1, "INSERT INTO CustomerAddressDat1 (leftv, rightv) VALUES(?, ?)", "55", "100");
	}
	
	@Test
	public void testMNInsertParent() {
		//adapted from t0-insert-parent.txt: add workers
		useCustomerManyToManySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "insert Address {id: 100, y: 45}");
		src = addSrc(src, "insert Address {id: '101', y:46 }");
		src = addSrc(src, "insert Customer {cid: 56, x:66, addr: ['100','101'] }");
		
		HLDInsert hldins = buildFromSrcInsert(src, 3); 
		SqlStatementGroup stmgrp = genInsertSql(hldins, 3);
		dumpGrp(stmgrp);
		chkInsertSql(stmgrp, 0, "INSERT INTO Customer (cid, x) VALUES(?, ?)", "56", "66");
		chkInsertSql(stmgrp, 1, "INSERT INTO CustomerAddressDat1 (leftv, rightv) VALUES(?, ?)", "56", "100");
		chkInsertSql(stmgrp, 2, "INSERT INTO CustomerAddressDat1 (leftv, rightv) VALUES(?, ?)", "56", "101");
	}
	
	
	private void dumpGrp(SqlStatementGroup stmgrp) {
		log.log("grp: %s", stmgrp.statementL.size());
		for(SqlStatement stm: stmgrp.statementL) {
			log.log(stm.sql);
		}
	}
	
	
	//-------------------------
	protected HLDInsert buildFromSrcInsert(String src, int statementIndex) {
		InsertStatementExp insertExp = compileToInsertStatement(src, statementIndex);
		log.log(src);
		
		mgr = createManager(); 
		HLDInsert hldins = mgr.fullBuildInsert(insertExp);
		log.log(hldins.toString());
		return hldins;
	}

	protected InsertStatementExp compileToInsertStatement(String src, int statementIndex) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		List<Exp> list = sessimpl.mostRecentContinueExpL.stream().filter(exp -> exp instanceof InsertStatementExp).collect(Collectors.toList());
		Exp exp = list.get(statementIndex);
		return (InsertStatementExp) exp;
	}
	
	protected void chkInsertSql(HLDInsert hldins, int numStatements, String expected, String...args) {
		SqlStatementGroup stmgrp = genInsertSql(hldins, numStatements);
		SqlStatement stm = stmgrp.statementL.get(0);
		chkStm(stm, expected, args);
	}

	protected SqlStatementGroup genInsertSql(HLDInsert hldins, int numStatements) {
		SqlStatementGroup stmgrp = mgr.generateSql(hldins);
		assertEquals(numStatements, stmgrp.statementL.size());
		return stmgrp;
	}
	protected void chkInsertSql(SqlStatementGroup stmgrp, int index, String expected, String...args) {
		SqlStatement stm = stmgrp.statementL.get(index);
		chkStm(stm, expected, args);
	}
	
	private String addSrc(String src0, String src) {
		return src0 + "\n" + src;
	}
}
