package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
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
		chkFnStatement(infnExp, 1, "f2", "c", "z");
	}

	@Test
	public void testTLang() {
		String src = "input function foo(Customer c, Address a) { field -> c.firstName using { x }, f2 -> c.z}";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(2, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
		chkArg(infnExp, 1, "Address", "a");
		
		assertEquals(2, infnExp.bodyExp.statementL.size());
		chkFnStatement(infnExp, 0, "field", "c", "firstName");
		chkFnStatement(infnExp, 1, "f2", "c", "z");
	}

	private void chkFnStatement(InputFunctionDefStatementExp infnExp, int i, String expected, String s2, String s3) {
		Exp z = infnExp.bodyExp.statementL.get(i);
		InputFuncMappingExp stexp = (InputFuncMappingExp) z;
		assertEquals(expected, stexp.inputField.name());
		assertEquals(s2, stexp.outputField.val1);
		assertEquals(s3, stexp.outputField.val2);
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
		NameAndFuncParser.initLazy();
		Exp exp = InputFunctionParser.inputFunction().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return (InputFunctionDefStatementExp) exp;
	}


	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
	
	
}
