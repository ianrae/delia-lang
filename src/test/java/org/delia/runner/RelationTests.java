package org.delia.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.delia.base.DBHelper;
import org.delia.db.sql.NewLegacyRunner;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.type.ValidationState;
import org.junit.Before;
import org.junit.Test;

public class RelationTests extends RunnerTestBase {
	
	@Test
	public void testFetch() {
		createActorType("");
		insertAddress(runner, 33);		
		
		int id = 44;
		DValue dval = insertAndQueryEx(runner, id);		
		chkValid(dval);
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		DRelation drel = dval.asStruct().getField("addr").asRelation();
		assertEquals(false, drel.haveFetched());
		
		QueryResponse qresp = doFetch(runner, null);
		assertEquals(true, qresp.ok);
		DValue dvalActor = qresp.getOne();
		assertEquals(44, dvalActor.asStruct().getField("id").asInt());
		DValue dvalAddress = dvalActor.asStruct().getField("addr").asRelation().getFetchedItems().get(0);
		assertEquals("kingston", dvalAddress.asStruct().getField("city").asString());
	}
	
//TODO: fix let x = a.fetch('addr') -- needs to do fetch
//	@Test
	public void testFetchFromA() {
		createActorType("");
		insertAddress(runner, 33);		
		
		int id = 44;
		DValue dval = insertAndQueryEx(runner, id);		
		chkValid(dval);
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		DRelation drel = dval.asStruct().getField("addr").asRelation();
		assertEquals(false, drel.haveFetched());
		
		QueryResponse qresp = doFetch(runner, "a");
		assertEquals(true, qresp.ok);
		DValue dvalActor = qresp.getOne();
		assertEquals(44, dvalActor.asStruct().getField("id").asInt());
		DValue dvalAddress = dvalActor.asStruct().getField("addr").asRelation().getFetchedItems().get(0);
		assertEquals("kingston", dvalAddress.asStruct().getField("city").asString());
		
		drel = dval.asStruct().getField("addr").asRelation();
		assertEquals(true, drel.haveFetched());
		
		assertEquals(1, drel.getFetchedItems().size());
		DValue dval2 = drel.getFetchedItems().get(0);
		assertSame(dval2, dvalAddress);
//		assertEquals(33, dvalAddr.asStruct().getField("id").asInt());
//		assertEquals("kingston", dvalAddr.asStruct().getField("city").asString());
		
	}
	
	private DValue insertAndQueryEx(NewLegacyRunner runner, int id) {
		QueryResponse qresp= insertAndQuery(runner, id);
		return qresp.getOne();
	}
	private QueryResponse insertAndQuery(NewLegacyRunner runner, int id) {
		String src = String.format("insert Actor {id:%d, firstName:'bob', addr:33 }", id);
//		InsertStatementExp exp = chkInsert(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
		
		//now query it
		src = String.format("let a = Actor[%d]", id);
//		LetStatementExp exp2 = chkQueryLet(src, null);
		res = runner.beginOrContinue(src, true);
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Actor");
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		DRelation drel = dval.asStruct().getField("addr").asRelation();
		assertEquals(33, drel.getForeignKey().asInt());
		return qresp;
	}
	private void insertAddress(NewLegacyRunner runner, int id) {
		String src = String.format("insert Address {id:%d, city:'kingston' }", id);
//		InsertStatementExp exp = chkInsert(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
	}

	private QueryResponse doFetch(NewLegacyRunner runner, String varToFetchFrom) {
		String src;
		if (varToFetchFrom != null) {
			src = String.format("let x = %s.fetch('addr')", varToFetchFrom);
		} else {
			int id = 44;
			src = String.format("let x = Actor[%d].fetch('addr')", id);
		}
//		LetStatementExp exp2 = chkQueryLet(src, null);
		ResultValue res = runner.beginOrContinue(src, true);
		assertEquals(true, res.ok);
		QueryResponse qresp = chkResQuery(res, "Actor");
		return qresp;
	}
	
	// --
	
	@Before
	public void init() {
		initRunner();
	}
	
	private void chkValid(DValue dval) {
		assertEquals(ValidationState.VALID, dval.getValidationState());
	}
	private void chkInvalid(DValue dval) {
		assertEquals(ValidationState.INVALID, dval.getValidationState());
	}
	private NewLegacyRunner createActorType(String rule) {
		String src = String.format("type Address struct {id int unique, city string} end ");
		src += String.format("type Actor struct {id int unique, firstName string, relation addr Address one} end");
//		List<Exp> list = chelper.parseTwo(src);
//		TypeStatementExp exp0 = (TypeStatementExp) list.get(0);
//		TypeStatementExp exp1 = (TypeStatementExp) list.get(1);
		ResultValue res = runner.beginOrContinue(src, true);
		chkResOK(res);
		
		DBHelper.createTable(dbInterface, "Actor"); //!! fake schema
		return runner;
	}



}
