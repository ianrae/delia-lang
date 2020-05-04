package org.delia.bdd.old;

import static org.junit.Assert.assertEquals;

import org.delia.compiler.ast.LetStatementExp;
import org.delia.runner.CompilerHelper;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.junit.Before;
import org.junit.Test;


public class LetBBTests extends BDDTestBase {

	@Test
	public void testString()  {
		chkString("'abc'", "abc");
		chkString("\"abc\"", "abc");
	}
	
	@Test
	public void testInt()  {
		chkInt("34", 34);
		chkInt("-34", -34);
	}
	
	@Test
	public void testBoolean()  {
		chkBoolean("true", true);
		chkBoolean("false", false);
	}
	@Test
	public void testBooleanDollarDollar()  {
		chkBoolean("true", true);
		ResultValue res = doDollarDollar(Shape.BOOLEAN);
		Boolean b = (Boolean) res.getAsDValue().asBoolean();
		assertEquals(true, b);
	}

	//TODO: fix this
	@Test
	public void testNull()  {
		chkNull("null");
	}
	
	@Test
	public void testQueryFileOne()  {
		String expr = "Actor[10].firstName";
		String dang = String.format("let a = %s", expr);
		ResultValue res = compileAndRun(dang);
		QueryResponse qresp = helper.chkRawResQuery(res, 1);
		assertEquals("name0", qresp.dvalList.get(0).asString());
	}
	@Test
	public void testQueryFileMany()  {
		String expr = "Actor[id > 10].firstName";
		String dang = String.format("let a = %s", expr);
		ResultValue res = compileAndRun(dang);
		QueryResponse qresp = helper.chkRawResQuery(res, 3);
		assertEquals("name1", qresp.dvalList.get(0).asString());
		assertEquals("name2", qresp.dvalList.get(1).asString());
		assertEquals("name3", qresp.dvalList.get(2).asString());
		
		res = doDollarDollar(null);
		qresp = helper.chkRawResQuery(res, 3);
		assertEquals("name1", qresp.dvalList.get(0).asString());
		assertEquals("name2", qresp.dvalList.get(1).asString());
		assertEquals("name3", qresp.dvalList.get(2).asString());
	}
	//test fn min()
	@Test
	public void testQueryFileManyFuncMin()  {
		String expr = "Actor[id > 10].id.min()";
		String dang = String.format("let a = %s", expr);
		ResultValue res = compileAndRun(dang);
		QueryResponse qresp = helper.chkRawResQuery(res, 1);
		assertEquals(11, qresp.dvalList.get(0).asInt());
	}
	//test fn max()
	@Test
	public void testQueryFileManyFuncMax()  {
		String expr = "Actor[id > 10].id.max()";
		String dang = String.format("let a = %s", expr);
		ResultValue res = compileAndRun(dang);
		QueryResponse qresp = helper.chkRawResQuery(res, 1);
		assertEquals(13, qresp.dvalList.get(0).asInt());
	}
	
	
	//WORKING ONE...
	@Test
	public void testWIP()  {
//		chkString("'abc'", "abc");
	}
	
	//--
	private int nextVarNum = 1;
	
	@Before
	public void init() {
		super.init();
	}
	
	public void chkString(String expr, String expected) {
		ResultValue res = doLet(expr, "string");
		assertEquals(Shape.STRING, res.shape);
		DValue dval = res.getAsDValue();
		assertEquals(expected, dval.asString());
	}
	public void chkInt(String expr, int expected) {
		ResultValue res = doLet(expr, "int");
		assertEquals(Shape.INTEGER, res.shape);
		Integer n = (Integer) res.getAsDValue().asInt();
		assertEquals(expected, n.intValue());
	}
	public void chkBoolean(String expr, boolean expected) {
		ResultValue res = doLet(expr, "boolean");
		assertEquals(Shape.BOOLEAN, res.shape);
		Boolean n = (Boolean) res.getAsDValue().asBoolean();
		assertEquals(expected, n.booleanValue());
	}
	public void chkNull(String expr) {
		ResultValue res = doLet(expr, null);
		assertEquals(null, res.shape);
		assertEquals(null, res.val);
	}
	
	private ResultValue doLet(String expr, String typeName) {
		String dang = String.format("let a%d = %s", nextVarNum++, expr);
		CompilerHelper chelper = new CompilerHelper(null);
		LetStatementExp exp = (LetStatementExp) chelper.parseOne(dang);
		assertEquals(typeName, exp.typeName);
		
		ResultValue res = runner.executeOneStatement(exp);
		assertEquals(true, res.ok);
		return res;
	}
	private ResultValue doDollarDollar(Shape shape) {
		String dang = String.format("$$");
		CompilerHelper chelper = new CompilerHelper(null);
		LetStatementExp exp = (LetStatementExp) chelper.parseOne(dang);
		assertEquals("queryResponse", exp.typeName);
		
		ResultValue res = runner.executeOneStatement(exp);
		assertEquals(true, res.ok);
		assertEquals(shape, res.shape);
		return res;
	}
	private ResultValue compileAndRun(String dang) {
		CompilerHelper chelper = new CompilerHelper(null);
		LetStatementExp exp = (LetStatementExp) chelper.parseOne(dang);
		ResultValue res = runner.executeOneStatement(exp);
		return res;
	}
	
}
