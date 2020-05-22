package org.delia.scope.scopetest.relation;

import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaFactory;
import org.delia.api.DeliaSession;
import org.delia.base.UnitTestLog;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.DeliaError;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class RelationTests { //extends IntegerTestBase {
	
	@Test
	public void testExec() {
		ResultValue res = delia.execute("let x = 5");
		assertEquals(true, res.ok);
		assertEquals(5, res.getAsDValue().asInt());
		
		//again is ok because creates new compiler and runner
		res = delia.execute("let x = 15");
		assertEquals(true, res.ok);
		assertEquals(15, res.getAsDValue().asInt());
	}
	
	@Test
	public void testExecContinue() {
		DeliaSession dbsess = delia.beginSession("let x = 5");
		assertEquals(true, dbsess.ok());
		assertEquals(5, dbsess.getFinalResult().getAsDValue().asInt());
		
		//again is ok because creates new compiler and runner
		ResultValue res = delia.continueExecution("let y = x", dbsess);
		assertEquals(true, res.ok);
		assertEquals(5, res.getAsDValue().asInt());
	}
	
	@Test
	public void test4Struct1() {
		createTypeFail("", "Customer", "relation field1 int", "relation-wrong-type");
	}
	@Test
	public void test4Struct2() {
		String src = createTypeSrc("Address", "");
		createTypeFail(src, "Customer", "relation addr Address", "relation-missing-one-or-many");
	}
	@Test
	public void test4Struct3() {
		String src = createTypeSrc("Address", "");
		createTypeFail(src, "Customer", "relation addr Address many parent", "relation-parent-not-allowed");
	}
	@Test
	public void testOneOneWay() {
		String src = createTypeSrc("Address", "relation cust Customer one");
		src += createTypeSrc("Customer", "");
		execTypeStatement(src);
		
		execStatement("insert Customer { id:44 }");
		ResultValue res = execStatement("let x1 = Customer[44]");
		chkInt(res, "id", 44);
		chkNullField(res, "addr");
		
		execStatement("insert Address { id:100, cust:44 }");
		res = execStatement("let x2 = Customer[44]");
		chkInt(res, "id", 44);

		res = execStatement("let x3 = Address[100]");
		chkInt(res, "id", 100);
		
		chkRelation(res, "cust", 44, "Customer", false);
		res = execStatement("let x4 = Customer[44]");
		chkRelationNull(res, "addr");
	}
	@Test
	public void testOneTwoWay() {
		String src = createTypeSrc("Address", "relation cust Customer one");
		src += createTypeSrc("Customer", "relation addr Address one optional");
		execTypeStatement(src);
		
		execStatement("insert Customer { id:44 }");
		ResultValue res = execStatement("let x1 = Customer[44]");
		chkInt(res, "id", 44);
		chkNullField(res, "addr");
		
		execStatement("insert Address { id:100, cust:44 }");
		res = execStatement("let x2 = Customer[44]");
		chkInt(res, "id", 44);

		res = execStatement("let x3 = Address[100]");
		chkInt(res, "id", 100);
		
		chkRelation(res, "cust", 44, "Customer", false);
		res = execStatement("let x4 = Customer[44]");
	}

	@Test
	public void testOneTwoWay2() {
		String src = createTypeSrc("Address", "relation cust Customer one");
		src += createTypeSrc("Customer", "relation addr Address one optional");
		execTypeStatement(src);
		execStatement("configure loadFKs=true");
		execStatement("insert Customer { id:44 }");
		execStatement("insert Customer { id:45 }");
		chkEntityNoRel("Customer", 44, "addr");
		chkEntityNoRel("Customer", 45, "addr");
		
		execStatement("insert Address { id:100, cust:44 }");
		execStatement("insert Address { id:101, cust:45 }");
		ResultValue res = chkEntity("Address", 100);
		chkRelation(res, "cust", 44, "Customer", false);
		
		res = chkEntity("Address", 101);
		chkRelation(res, "cust", 45, "Customer", false);
		res = chkEntity("Customer", 44);
		
		//we auto-create the other side of a two-way
		chkRelation(res, "addr", 100, "Address", false);
	}
	
	@Test
	public void testOneTwoWay2Fail() {
		String src = createTypeSrc("Address", "relation cust Customer one"); //FK here
		src += createTypeSrc("Customer", "relation addr Address one optional");
		execTypeStatement(src);
		
		execStatement("configure loadFKs=true");
		execStatement("insert Customer { id:44 }");
		execStatement("insert Customer { id:45 }");
		chkEntityNoRel("Customer", 44, "addr");
		chkEntityNoRel("Customer", 45, "addr");
		
		execStatement("insert Address { id:100, cust:44 }");
		execStatementFail("insert Address { id:101, cust:44 }", "rule-relationOne"); //44 already used
	}
	
//	@Test
//	public void testManyTwoWay2() {
//		String src = createTypeSrc("Address", "relation cust Customer one");
//		src += createTypeSrc("Customer", "relation addr Address many");
//		execTypeStatement(src);
//		
//		execStatement("insert Customer { id:44 }");
//		execStatement("insert Customer { id:45 }");
//		chkEntityNoRel("Customer", 44, "addr");
//		chkEntityNoRel("Customer", 45, "addr");
//		
//		execStatement("insert Address { id:100, cust:44 }");
//		execStatement("insert Address { id:101, cust:45 }");
//		ResultValue res = chkEntity("Address", 100);
//		chkRelation(res, "cust", 44, "Customer", false);
//		
//		res = chkEntity("Address", 101);
//		chkRelation(res, "cust", 45, "Customer", false);
//		res = chkEntity("Customer", 44);
//		
//		//we auto-create the other side of a two-way
//		chkRelation(res, "addr", 100, "Address", false);
//		
//		//now add 2nd relation
//		execStatement("insert Customer { id:46, addr:100 }");
//		res = chkEntity("Customer", 46);
//		chkRelation(res, "addr", 100, "Address", false);
//	}
//	@Test
//	public void testManyTwoWay0() {
//		String src = createTypeSrc("Address", "relation cust Customer one");
//		src += createTypeSrc("Customer", "relation addr Address many");
//		execTypeStatement(src);
//		
//		execStatement("insert Customer { id:44 }");
//		execStatement("insert Customer { id:45 }");
//		chkEntityNoRel("Customer", 44, "addr");
//		chkEntityNoRel("Customer", 45, "addr");
//		
//		//not allowed to insert Address w/o a cust value
//		execStatementFail("insert Address { id:100 }", "rule:relationOne");
//	}
	@Test
	public void testManyTwoWayOptionalEasy() {
		//Cust -> Address
		// 1 : N
		// M - O is easy
		String src = createTypeSrc("Customer", "relation addr Address many optional");
		src += createTypeSrc("Address", "relation cust Customer one"); //FK here
		execTypeStatement(src);
		
		execStatement("insert Customer { id:44 }");
		execStatement("insert Customer { id:45 }");
		chkEntityNoRel("Customer", 44, "addr");
		chkEntityNoRel("Customer", 45, "addr");
		
		execStatement("insert Address { id:100, cust:44 }");
	}
	@Test
	public void testManyTwoWayOptionalHard() {
		//Cust -> Address
		// 1 : N
		// O - M is easy
		//need FK in assoc. tbl
		String src = createTypeSrc("Customer", "relation addr Address many");
		src += createTypeSrc("Address", "relation cust Customer one optional"); 
		execTypeStatement(src);
		execStatement("insert Address { id:100}");
		
		execStatement("insert Customer { id:44, addr:100 }"); 
//		execStatement("insert Customer { id:45 }");
//		chkEntityNoRel("Customer", 44, "addr");
//		chkEntityNoRel("Customer", 45, "addr");
		
	}
	
	// --
	private Delia delia;
	private DeliaSession sess = null;
	private boolean addIdFlag;
	private ZDBInterfaceFactory dbInterface ;
	private int nextVarNumn = 1;
	
	@Before
	public void init() {
		addIdFlag = true;
		Log log = new UnitTestLog();
		FactoryService factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
		dbInterface = new MemZDBInterfaceFactory(factorySvc);
		delia = DeliaFactory.create(dbInterface, log, factorySvc);
//		DBHelper.createTable(dbInterface, "Address"); //!! fake schema
//		DBHelper.createTable(dbInterface, "Customer"); //!! fake schema
	}
	
	protected void createType(String type, String relField) {
		String src = createTypeSrc(type, relField);
		ResultValue res = execTypeStatement(src);
		chkResOK(res);
	}
	protected String createTypeSrc(String type, String relField) {
		String sid = addIdFlag ? String.format(" id int unique") : "";
		relField = sid.isEmpty() ? relField : ", " + relField;
		String src = String.format("type %s struct { %s %s} end", type, sid, relField);
		src += "\n";
		return src;
	}
	protected ResultValue execTypeStatement(String src) {
		if (sess != null) {
			log("rebuilding..");
		}
		sess = delia.beginSession(src);
		
		ResultValue res = sess.getFinalResult();
		chkResOK(res);
		return res;
	}
	protected void createTypeFail(String initialSrc, String type, String rel, String errId) {
		String sid = addIdFlag ? String.format(" id int unique") : "";
		String src = String.format("type %s struct { %s %s }  end", type, sid, rel);
		execTypeStatementFail(initialSrc + src, errId);
	}
	protected void execTypeStatementFail(String src, String errId) {
		boolean pass = false;
		try {
			execTypeStatement(src);
			pass = true;
		} catch (DeliaException e) {
			DeliaError err = e.getLastError();
			assertEquals(true, err.getId().contains(errId));
		}
		assertEquals(false, pass);
	}
	protected ResultValue execStatement(String src) {
		assertEquals(true, sess != null);
		ResultValue res = delia.continueExecution(src, sess);
		chkResOK(res);
		return res;
	}
	protected void execStatementFail(String src, String errId) {
		assertEquals(true, sess != null);
		boolean pass = false;
		try {
			delia.continueExecution(src, sess);
		} catch (DeliaException e) {
			DeliaError err = e.getLastError();
			assertEquals(true, err.getId().equals(errId));
			pass = true;
		}
		assertEquals(true, pass);
	}
	
	private void chkResFail(ResultValue res, String errId) {
		assertEquals(false, res.ok);
		DeliaError err = res.getLastError();
		assertEquals(true, err.getId().equals(errId));
	}

	public void chkResOK(ResultValue res) {
		assertEquals(true, res.ok);
		assertEquals(true, res.errors.isEmpty());
	}
	private void log(String s) {
		System.out.println(s);
	}
	private void chkRelation(ResultValue res, String fieldName, int expected, String typeName, boolean b) {
		DValue dval = res.getAsDValue();
		DRelation drel = dval.asStruct().getField(fieldName).asRelation();
		assertEquals(expected, drel.getForeignKey().asInt());
		assertEquals(typeName, drel.getTypeName());
	}
	private void chkRelationNull(ResultValue res, String fieldName) {
		DValue dval = res.getAsDValue();
		DValue inner = dval.asStruct().getField(fieldName);
		assertEquals(null, inner);
	}

	private void chkInt(ResultValue res, String fieldName, int expected) {
		DValue dval = res.getAsDValue();
		assertEquals(expected, dval.asStruct().getField(fieldName).asInt());
	}
	private void chkEntityNoRel(String typeName, int id, String nullField) {
		String src = String.format("%s%s[%d]", buildLet(), typeName, id);
		ResultValue res = execStatement(src);
		chkInt(res, "id", id);
		chkNullField(res, nullField);
	}
	private ResultValue chkEntity(String typeName, int id) {
		String src = String.format("%s%s[%d]", buildLet(), typeName, id);
		ResultValue res = execStatement(src);
		chkInt(res, "id", id);
		return res;
	}

	private String buildLet() {
		return String.format("let x%d = ", nextVarNumn++);
	}

	private void chkNullField(ResultValue res, String fieldName) {
		DValue dval = res.getAsDValue();
		assertEquals(null, dval.asStruct().getField(fieldName));
	}


	
}
