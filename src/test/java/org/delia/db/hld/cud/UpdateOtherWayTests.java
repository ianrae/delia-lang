package org.delia.db.hld.cud;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.hld.NewHLSTestBase;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.runner.DoNothingVarEvaluator;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class UpdateOtherWayTests extends NewHLSTestBase {
	//TODO flip all tests to be other way
	
	// --- 1:1 ---
	@Test
	public void test1() {
		this.useCustomer11OtherWaySrc = true;
		String src = "update Customer[1] {x: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "45", "1");
	}
	@Test
	public void test2() {
		useCustomer11OtherWaySrc = true;
		String src = "update Address[100] { y: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "100");
	}
	@Test
	public void test2a() {
		useCustomer11OtherWaySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[1] {y: 45, cust:55}");
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 2);
		dumpGrp(stmgrp);
		chkUpdateSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "1");
		chkUpdateSql(stmgrp, 1, "UPDATE Customer as t1 SET t1.addr = ? WHERE t1.cid=?", "1", "55");
	}
	
	// --- 1:N ---
	@Test
	public void test1N() {
		useCustomer11OtherWaySrc = true;
		String src = "update Customer[1] {x: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "45", "1");
	}
	@Test
	public void test1N2() {
		useCustomer11OtherWaySrc = true;
		String src = "update Address[100] {y: 45}";
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		chkUpdateSql(hldupdate, 1, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "100");
	}
	@Test
	public void test1N2a() {
		useCustomer11OtherWaySrc = true;
		String src0 = "insert Customer {cid: 55, x: 45}";
		String src = addSrc(src0, "update Address[100] {y: 45, cust:55}");
		
		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 2);
		dumpGrp(stmgrp);
		
		chkUpdateSql(stmgrp, 0, "UPDATE Address as t0 SET t0.y = ? WHERE t0.id=?", "45", "100");
		chkUpdateSql(stmgrp, 1, "UPDATE Customer as t1 SET t1.addr = ? WHERE t1.cid=?", "100", "55");
	}
//TODO:fix	
//	@Test
//	public void test1NInsertParentShouldFail() {
//		//adapted from t0-insert-parent.txt: add workers
//		useCustomer1NOtherWaySrc = true;
//		String src0 = "insert Customer {cid: 55, x: 45}";
//		String src = addSrc(src0, "insert Address {id: 100, y: 45}");
//		src = addSrc(src, "insert Address {id: '101', y:46 }");
//		src = addSrc(src, "update Customer[56] {x:66, addr: ['100','101'] }"); //TODO:this should fail
//		//Customer has one addr so should be an error to use an array
//		
//		HLDUpdateStatement hldupdate = buildFromSrcUpdate(src, 0); 
//		SqlStatementGroup stmgrp = genUpdateSql(hldupdate, 1);
//		dumpGrp(stmgrp);
//		chkUpdateSql(stmgrp, 0, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "66", "56");
//		chkUpdateSql(stmgrp, 1, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.id=?", "56", "100");
//		chkUpdateSql(stmgrp, 2, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.id=?", "56", "101");
//	}
	
	// --- M:N ---
	//no need for 'other way' tests since both sides are symmetrical
	
	
	//-------------------------
	protected HLDUpdateStatement buildFromSrcUpdate(String src, int statementIndex) {
		UpdateStatementExp updateExp = compileToUpdateStatement(src, statementIndex);
		log.log(src);
		
		mgr = createManager(); 
		HLDUpdateStatement hldupdate = mgr.fullBuildUpdate(updateExp, new DoNothingVarEvaluator(), null);
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
}
