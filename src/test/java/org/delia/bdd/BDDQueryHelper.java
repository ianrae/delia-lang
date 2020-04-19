package org.delia.bdd;

import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.LetStatementExp;
import org.delia.h2.DeliaInitializer;
import org.delia.runner.CompilerHelper;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.runner.RunnerHelper;
import org.delia.type.DValue;



public class BDDQueryHelper {
	private RunnerHelper helper = new RunnerHelper();
	private CompilerHelper chelper = new CompilerHelper(null);
	private DeliaInitializer initter;
	private Runner runner;
	private int nextVarNum = 1;

	public BDDQueryHelper(DeliaInitializer initter, Runner runner) {
		this.initter = initter;
		this.runner = runner;
	}
	
	public void chkQueryOne(String typeName, int id) {
		String name = String.format("name%d", id - 10);
		String dang = String.format("let a%d = %s[%d]", nextVarNum++, typeName, id);
		LetStatementExp exp2 = chelper.chkQueryLet(dang, null);
		ResultValue res = runner.executeOneStatement(exp2);
		QueryResponse qresp = helper.chkResQuery(res, typeName);
		DValue dval = qresp.getOne();
		assertEquals(name, dval.asStruct().getField("firstName").asString());
	}
	public void chkQueryNotFound(String typeName, int id) {
		String name = String.format("name%d", id);
		String dang = String.format("let a%d = %s[%d]", nextVarNum++, typeName, id);
		LetStatementExp exp2 = chelper.chkQueryLet(dang, null);
		ResultValue res = runner.executeOneStatement(exp2);
		helper.chkResQueryEmpty(res);
	}
	public void chkQueryAll(String typeName, int size) {
		String dang = String.format("let a%d = %s", nextVarNum++, typeName);
		LetStatementExp exp2 = chelper.chkQueryLet(dang, null);
		ResultValue res = runner.executeOneStatement(exp2);
		QueryResponse qresp = helper.chkResQueryMany(res, size);
		if (size > 0) {
			DValue dval = qresp.dvalList.get(0);
			assertEquals("name0", dval.asStruct().getField("firstName").asString());
		}
	}
	public void chkQueryOpLT(String typeName, int val, int size) {
		String dang = String.format("let a%d = %s[id < %d]", nextVarNum++, typeName, val);
		doQuery(dang, size);
	}
	public void chkQueryOpLE(String typeName, int val, int size) {
		String dang = String.format("let a%d = %s[id <= %d]", nextVarNum++, typeName, val);
		doQuery(dang, size);
	}
	public void chkQueryOpGT(String typeName, int val, int size) {
		String dang = String.format("let a%d = %s[id > %d]", nextVarNum++, typeName, val);
		doQuery(dang, size);
	}
	public void chkQueryOpGE(String typeName, int val, int size) {
		String dang = String.format("let a%d = %s[id >= %d]", nextVarNum++, typeName, val);
		doQuery(dang, size);
	}
	private void doQuery(String dang, int size) {
		LetStatementExp exp2 = chelper.chkQueryLet(dang, null);
		ResultValue res = runner.executeOneStatement(exp2);
		QueryResponse qresp = helper.chkResQueryMany(res, size);
//		if (size > 0) {
//			DValue dval = qresp.dvalList.get(0);
//			assertEquals("name0", dval.asStruct().getField("firstName").asString());
//		}
	}
	public void chkQueryOpLTNull(String typeName, int size) {
		String dang = String.format("let a%d = %s[id < null]", nextVarNum++, typeName);
		doQuery(dang, size);
	}

	
	
	private void log(String msg) {
		System.out.println(msg);
	}
}
