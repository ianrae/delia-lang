package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.compiler.ast.inputfunction.TLangBodyExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.compiler.parser.InputFunctionParser;
import org.delia.compiler.parser.NameAndFuncParser;
import org.delia.compiler.parser.TerminalParser;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.type.DTypeRegistry;
import org.junit.Before;
import org.junit.Test;

public class InputFunctionParserTests  extends NewBDDBase {
	
	
	@Test
	public void test() {
		String src = "input function foo(Customer c) {}";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(1, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
	}
	
	@Test
	public void test2() {
		String src = "input function foo(Customer c, Address a) { field -> c.firstName, f2 -> c.z}";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(2, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
		chkArg(infnExp, 1, "Address", "a");
		
		assertEquals(2, infnExp.bodyExp.statementL.size());
		chkFnStatement(infnExp, 0, "field", "c", "firstName");
		TLangBodyExp texp = chkFnStatement(infnExp, 1, "f2", "c", "z").tlangBody;
		assertEquals(null, texp);
	}

	@Test
	public void testTLang() {
//		String src = "input function foo(Customer c, Address a) { field -> c.firstName using { x.y }, f2 -> c.z}";
		String src = "input function foo(Customer c, Address a) { field -> c.firstName using { x.y }}";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(2, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
		chkArg(infnExp, 1, "Address", "a");
		
		TLangBodyExp texp = chkFnStatement(infnExp, 0, "field", "c", "firstName").tlangBody;
		
		IdentExp x1 = (IdentExp) texp.statementL.get(0);
		XNAFMultiExp x2 = (XNAFMultiExp) texp.statementL.get(1);
		assertEquals("x", x1.name());
		assertEquals("y", x2.qfeL.get(0).funcName);
	}
	
	@Test
	public void testTLang2() {
		chkTLangInt("35", 35);
		Long bign = getLongNum();
		chkTLangLong(bign.toString(), getLongNum());
		Double dd = -1045.678;
		chkTLangDouble(dd.toString(), dd);
		
		chkTLangBoolean("true", true);
		chkTLangString("'abc'", "abc");
	}
	
	@Test
	public void testTLangIdent() {
		chkTLangIdent("x", "x");
		chkTLangIdent("x.y(5)", "x", "5");
		chkTLangIdent("x(5)", "x", "5");
	}

	private void chkTLangIdent(String tlang, String expected, String...args) {
		TLangBodyExp texp = doTLang(tlang);
		
		XNAFMultiExp x1 = (XNAFMultiExp) texp.statementL.get(0);
		assertEquals(expected, x1.qfeL.get(0).funcName);
		if (args.length == 0) {
			chkStatementSize(texp, 1);
		} else {
			chkStatementSize(texp, 1);
			XNAFMultiExp x2 = (XNAFMultiExp) texp.statementL.get(0);
			for(String arg: args) {
				XNAFSingleExp sexp = x2.qfeL.get(0);
				assertEquals(arg, sexp.argL.get(0).strValue());
			}
		}
		
		
	}
	private void chkTLangString(String tlang, String expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		StringExp x1 = (StringExp) texp.statementL.get(0);
		assertEquals(expected, x1.val);
		chkStatementSize(texp, 1);
	}
	private void chkTLangBoolean(String tlang, boolean expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		BooleanExp x1 = (BooleanExp) texp.statementL.get(0);
		assertEquals(expected, x1.val);
		chkStatementSize(texp, 1);
	}
	private void chkTLangInt(String tlang, int expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		IntegerExp x1 = (IntegerExp) texp.statementL.get(0);
		assertEquals(expected, x1.val.intValue());
		chkStatementSize(texp, 1);
	}
	private void chkTLangLong(String tlang, long expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		LongExp x1 = (LongExp) texp.statementL.get(0);
		assertEquals(expected, x1.val.longValue());
		chkStatementSize(texp, 1);
	}
	private void chkTLangDouble(String tlang, double expected) {
		TLangBodyExp texp = doTLang(tlang);
		
		NumberExp x1 = (NumberExp) texp.statementL.get(0);
		assertEquals(expected, x1.val.doubleValue(), 0.0001);
		chkStatementSize(texp, 1);
	}
	private TLangBodyExp doTLang(String tlang) {
		String src = String.format("input function foo(Customer c, Address a) { field -> c.firstName using { %s }}", tlang);
		InputFunctionDefStatementExp infnExp = parse(src);
		TLangBodyExp texp = chkFnStatement(infnExp, 0, "field", "c", "firstName").tlangBody;
		return texp;
	}

	private void chkStatementSize(TLangBodyExp texp, int n) {
		assertEquals(n, texp.statementL.size());
	}

	private InputFuncMappingExp chkFnStatement(InputFunctionDefStatementExp infnExp, int i, String expected, String s2, String s3) {
		Exp z = infnExp.bodyExp.statementL.get(i);
		InputFuncMappingExp stexp = (InputFuncMappingExp) z;
		assertEquals(expected, stexp.inputField.name());
		assertEquals(s2, stexp.outputField.val1);
		assertEquals(s3, stexp.outputField.val2);
		return stexp;
	}

	private void chkArg(InputFunctionDefStatementExp infnExp, int i, String expected, String expected2) {
		IdentPairExp pairExp = infnExp.argsL.get(i);
		assertEquals(expected, pairExp.typeName());
		assertEquals(expected2, pairExp.argName());
	}



	// --
//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private DTypeRegistry registry;

	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
		String src = buildSrc();
		this.session = delia.beginSession(src);
		this.registry = session.getExecutionContext().registry;
	}
	private String buildSrc() {
		String src = " type Customer struct {id int unique, wid int, name string } end";
		return src;
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}
	
	
	private InputFunctionDefStatementExp parse(String src) {
		log.log(src);
		NameAndFuncParser.initLazy();
		Exp exp = InputFunctionParser.inputFunction().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return (InputFunctionDefStatementExp) exp;
	}
	
	private Long getLongNum() {
		int max = Integer.MAX_VALUE;
		long bigId = Long.valueOf((long)max) + 10; //2147483647
		return bigId;
	}


	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
	
	
}
