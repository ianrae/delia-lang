package org.delia.runner;

import static org.junit.Assert.assertEquals;

import org.delia.type.DValue;
import org.delia.type.ValidationState;
import org.junit.Before;
import org.junit.Test;


public class DeliaStatementRunnerTests extends RunnerTestBase {
	
	@Test
	public void testStr() {
		runner.begin("");
		ResultValue res = runner.beginOrContinue("let a = 'bob'", true);
		assertEquals(true, res.ok);
		chkResStr(res, "bob");
		
		assertEquals(true, runner.exists("a"));
		DValue dval = (DValue) runner.getVar("a").val;
		assertEquals("bob", dval.asString());
	}
	@Test
	public void testBool() {
		runner.begin("");

		ResultValue res = runner.beginOrContinue("let a = true", true);
		helper.chkResBoolean(res, true);
		
		assertEquals(true, runner.exists("a"));
		DValue dval = (DValue) runner.getVar("a").val;
		assertEquals(true, dval.asBoolean());
	}
	
	@Test
	public void testStr2() {
		runner.begin("");
		xchkString("let a = 'bob'");
		ResultValue res = xchkString("let b = 'sue'");
		
		chkResStr(res, "sue");
		assertEquals(true, runner.exists("b"));
		
		assertEquals(true, runner.exists("a"));
		DValue dval = (DValue) runner.getVar("a").val;
		assertEquals("bob", dval.asString());
		
		assertEquals(true, runner.exists("b"));
	}
	
	private ResultValue xchkString(String src) {
		ResultValue res = runner.beginOrContinue(src, true);
		assertEquals(true, res.ok);
		return res;
	}
	@Test
	public void testInsertFail() {
		runner.begin("");
		ResultValue res = doExecCatchFail("insert Airline { field1:'bob' }", false);
		
		chkResFail2(res, "can't find type");
		assertEquals(false, runner.exists("a"));
	}
	@Test
	public void testInsertOK() {
		runner.begin("type Customer struct { id int unique, firstName string, flag boolean } end");
		ResultValue res = xchkString("insert Customer {id:44, firstName:'bob',flag:true }");
		
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
		
		//now query it
		res = xchkString("let a = Customer[44]");
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Customer");
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		
		//now query an id that doesn't exist
		res = xchkString("let a2 = Customer[45]");
		assertEquals(true, res.ok);
		helper.chkResQueryEmpty(res);
	}
	@Test
	public void testInsertDupFail() {
		runner.begin("type Customer struct { id int unique, firstName string } end");
		ResultValue res = xchkString("insert Customer {id:44, firstName:'bob' }");
		
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
		res = doExecCatchFail("insert Customer {id:44, firstName:'sue' }", false);
		chkResFail2(res, "row with unique field");
	}
	
	@Test
	public void testDeleteFail() {
		runner.begin("type Customer struct { id int unique, firstName string } end");
		ResultValue res = doExecCatchFail("delete Customer[44]", true);
		
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
	}
	@Test
	public void testDeleteOK() {
		runner.begin("type Customer struct { id int unique, firstName string } end");
		ResultValue res = xchkString("insert Customer {id:44, firstName:'bob' }");
		
		chkResOK(res);
		assertEquals(false, runner.exists("a"));

		//now delete it
		res = xchkString("delete Customer[44]");
		
		chkResOK(res);
		
		//now query it
		res = xchkString("let a = Customer[44]");
		assertEquals(true, res.ok);
		helper.chkResQueryEmpty(res);
	}
	
	@Test
	public void testTypeAndInsert() {
		ResultValue res = xchkString("type Actor struct {id int unique, firstName string, flag boolean} end");
		res = xchkString("insert Actor {id:44, firstName:'bob', flag:true }");
		chkResOK(res);
		
		assertEquals(false, runner.exists("a"));
		
		//now query it
		res = xchkString("let a = Actor[44]");
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Actor");
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		assertEquals(true, dval.asStruct().getField("flag").asBoolean());
	}
	
	@Test
	public void testUpdateOK() {
		runner.begin("type Customer struct { id int unique, firstName string, flag boolean optional } end");
		ResultValue res = xchkString("insert Customer {id:44, firstName:'bob',flag:true }");
		
		chkResOK(res);
		assertEquals(false, runner.exists("a"));
		
		res = xchkString("update Customer {id:44, firstName:'bobby',flag:false}");
		chkResOK(res);
		Integer numRowsAffected = (Integer) res.val;
		assertEquals(1, numRowsAffected.intValue());
		
		//now query it
		res = xchkString("let a = Customer[44]");
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
		runner.begin("type Customer struct { id int unique, firstName string } end");
		ResultValue res = xchkString("insert Customer {id:44, firstName:'bob' }");
		chkResOK(res);
		
		//now query it
		res = xchkString("let a = Customer");
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
