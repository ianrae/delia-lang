package org.delia.db.newhls.cud;


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
	
	//TODO test inserting refs
	
	//-------------------------
	protected HLDInsert buildFromSrcInsert(String src, int expectedJoins) {
		InsertStatementExp insertExp = compileToInsertStatement(src);
		log.log(src);
		
		mgr = createManager(); 
		HLDInsert hldins = mgr.fullBuildInsert(insertExp);
		log.log(hldins.toString());
//		assertEquals(expectedJoins, hldins.hld.joinL.size());
		return hldins;
	}

	protected InsertStatementExp compileToInsertStatement(String src) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		for(Exp exp: sessimpl.mostRecentContinueExpL) {
			if (exp instanceof InsertStatementExp) {
				return (InsertStatementExp) exp;
			}
		}
		return null;
	}
	
	protected void chkInsertSql(HLDInsert hldins, int numStatements, String expected, String...args) {
		SqlStatementGroup stmgrp = mgr.generateSql(hldins);
		SqlStatement stm = stmgrp.statementL.get(0);
		chkStm(stm, expected, args);
		assertEquals(numStatements, stmgrp.statementL.size());
	}

	private void assertEquals(int numStatements, int size) {
		// TODO Auto-generated method stub
		
	}
}
