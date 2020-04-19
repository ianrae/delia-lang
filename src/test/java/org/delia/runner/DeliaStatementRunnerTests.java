package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.base.DBHelper;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.delia.type.ValidationState;
import org.junit.Before;
import org.junit.Test;


public class DeliaStatementRunnerTests extends RunnerTestBase {
	
	@Test
	public void testStr() {
		LetStatementExp exp = chkString("let a = 'bob'", "let a = 'bob'");
		
		assertEquals(false, runner.exists("a"));
		
		ResultValue res = runner.executeOneStatement(exp);
		assertEquals(true, res.ok);
		chkResStr(res, "bob");
		
		assertEquals(true, runner.exists("a"));
		DValue dval = (DValue) runner.getVar("a").val;
		assertEquals("bob", dval.asString());
	}
	@Test
	public void testBool() {
		LetStatementExp exp = chelper.chkBoolean("let a = true", "let a = true");
		
		assertEquals(false, runner.exists("a"));
		
		ResultValue res = runner.executeOneStatement(exp);
		helper.chkResBoolean(res, true);
		
		assertEquals(true, runner.exists("a"));
		DValue dval = (DValue) runner.getVar("a").val;
		assertEquals(true, dval.asBoolean());
	}
	
	@Test
	public void testStr2() {
		LetStatementExp exp1 = chkString("let a = 'bob'", "let a = 'bob'");
		LetStatementExp exp2 = chkString("let b = 'sue'", "let b = 'sue'");
		
		ResultValue res = runner.executeOneStatement(exp1);
		assertEquals(true, res.ok);
		chkResStr(res, "bob");
		assertEquals(false, runner.exists("b"));
		
		assertEquals(true, runner.exists("a"));
		DValue dval = (DValue) runner.getVar("a").val;
		assertEquals("bob", dval.asString());
		
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		chkResStr(res, "sue");
		assertEquals(true, runner.exists("b"));
	}
	
	@Test
	public void testInsertFail() {
		InsertStatementExp exp = chkInsert("insert Airline { field1:'bob' }", "insert Airline {field1: 'bob' }");
		
		ResultValue res = runner.executeOneStatement(exp);
		chkResFail2(res, "can't find type");
		assertEquals(false, runner.exists("a"));
	}
	@Test
	public void testInsertOK() {
		InsertStatementExp exp = chkInsert("insert Customer {id:44, firstName:'bob',flag:true }", "insert Customer {id: 44,firstName: 'bob',flag: true }");
		
		ResultValue res = runner.executeOneStatement(exp);
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
		
		//now query it
		LetStatementExp exp2 = chkQueryLet("let a = Customer[44]", "let a = Customer[44]");
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Customer");
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		
		//now query an id that doesn't exist
		exp2 = chkQueryLet("let a2 = Customer[45]", "let a2 = Customer[45]");
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		helper.chkResQueryEmpty(res);
	}
	@Test
	public void testInsertDupFail() {
		InsertStatementExp exp = chkInsert("insert Customer {id:44, firstName:'bob' }", "insert Customer {id: 44,firstName: 'bob' }");
		
		ResultValue res = runner.executeOneStatement(exp);
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
		
		exp = chkInsert("insert Customer {id:44, firstName:'sue' }", "insert Customer {id: 44,firstName: 'sue' }");
		res = runner.executeOneStatement(exp);
		chkResFail2(res, "row with unique field");
	}
	
	@Test
	public void testDeleteFail() {
		DeleteStatementExp exp = chkDelete("delete Customer[44]", "delete Customer[44]");
		
		ResultValue res = runner.executeOneStatement(exp);
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
	}
	@Test
	public void testDeleteOK() {
		InsertStatementExp exp = chkInsert("insert Customer {id:44, firstName:'bob' }", "insert Customer {id: 44,firstName: 'bob' }");
		
		ResultValue res = runner.executeOneStatement(exp);
		chkResOK(res);
		assertEquals(false, runner.exists("a"));

		//now delete it
		DeleteStatementExp exp1 = chkDelete("delete Customer[44]", "delete Customer[44]");
		
		res = runner.executeOneStatement(exp1);
		chkResOK(res);
		
		//now query it
		LetStatementExp exp2 = chkQueryLet("let a = Customer[44]", "let a = Customer[44]");
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		helper.chkResQueryEmpty(res);
	}
	
	@Test
	public void testTypeAndInsert() {
		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		InsertStatementExp exp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
		
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema

		res = runner.executeOneStatement(exp);
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
		
		//now query it
		LetStatementExp exp2 = chkQueryLet("let a = Actor[44]", "let a = Actor[44]");
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Actor");
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		assertEquals(true, dval.asStruct().getField("flag").asBoolean());
	}
	
	@Test
	public void testUpdateOK() {
		InsertStatementExp exp = chkInsert("insert Customer {id:44, firstName:'bob',flag:true }", "insert Customer {id: 44,firstName: 'bob',flag: true }");
		
		ResultValue res = runner.executeOneStatement(exp);
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
		
		UpdateStatementExp expu = chkUpdate("update Customer {id:44, firstName:'bobby',flag:false}", null);
		res = runner.executeOneStatement(expu);
		chkResOK(res);
		Integer numRowsAffected = (Integer) res.val;
		assertEquals(1, numRowsAffected.intValue());
		
		//now query it
		LetStatementExp exp2 = chkQueryLet("let a = Customer[44]", "let a = Customer[44]");
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Customer");
		DValue dval = qresp.getOne();
		assertEquals("bobby", dval.asStruct().getField("firstName").asString());
		assertEquals(44, dval.asStruct().getField("id").asInt());
		assertEquals(false, dval.asStruct().getField("flag").asBoolean());
		assertEquals(ValidationState.VALID, dval.getValidationState());
	}
	
	
	@Test
	public void testQueryAll() {
		InsertStatementExp exp = chkInsert("insert Customer {id:44, firstName:'bob' }", "insert Customer {id: 44,firstName: 'bob' }");
		
		ResultValue res = runner.executeOneStatement(exp);
		chkResOK(res);
		
		//now query it
		LetStatementExp exp2 = chkQueryLet("let a = Customer", "let a = Customer");
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Customer");
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
	}
	
	// --
	
	@Before
	public void init() {
		runner = initRunner();
	}
	
}
