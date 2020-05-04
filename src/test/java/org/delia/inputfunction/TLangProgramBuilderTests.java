package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionBodyExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.compiler.parser.InputFunctionParser;
import org.delia.compiler.parser.NameAndFuncParser;
import org.delia.compiler.parser.TerminalParser;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.DeliaException;
import org.delia.tlang.TLangProgramBuilder;
import org.delia.tlang.runner.TLangProgram;
import org.delia.tlang.runner.TLangStatement;
import org.delia.tlang.runner.TLangVarEvaluator;
import org.delia.tlang.statement.EndIfStatement;
import org.delia.tlang.statement.IfStatement;
import org.delia.tlang.statement.ToUpperStatement;
import org.delia.tlang.statement.ValueStatement;
import org.delia.tlang.statement.VariableStatement;
import org.delia.type.DTypeRegistry;
import org.junit.Before;
import org.junit.Test;

public class TLangProgramBuilderTests  extends NewBDDBase {
	
	@Test
	public void test1() {
		ValueStatement statement = buildTLang("35", 1, ValueStatement.class);
		assertEquals(35, statement.getDVal().asInt());
	}
	@Test
	public void testVar() {
		VariableStatement statement = buildTLang("zz", 1, VariableStatement.class);
		assertEquals("zz", statement.getVarName());
	}
	@Test
	public void testIf() {
		IfStatement statement = buildTLang("if true then zz,endif", 3, IfStatement.class);
		assertEquals("if", statement.getName());
		
		EndIfStatement stat2 = (EndIfStatement) recentProgram.statements.get(2);
		assertEquals("endif", stat2.getName());
	}
	@Test(expected=DeliaException.class)
	public void testIfMissingEndif() {
		IfStatement statement = buildTLang("if true then zz", 2, IfStatement.class);
		assertEquals("if", statement.getName());
		
		EndIfStatement stat2 = (EndIfStatement) recentProgram.statements.get(1);
		assertEquals("endif", stat2.getName());
	}
	@Test
	public void testIfOp() {
		IfStatement statement = buildTLang("if x > 5 then zz,endif", 3, IfStatement.class);
		assertEquals("if", statement.getName());
		
		EndIfStatement stat2 = (EndIfStatement) recentProgram.statements.get(2);
		assertEquals("endif", stat2.getName());
	}
	
	
	@Test
	public void testToUpperCase() {
		ToUpperStatement statement = buildTLang("toUpperCase()", 1, ToUpperStatement.class);
		assertEquals("toUpperCase", statement.getName());
	}
	
	@Test(expected=DeliaException.class)
	public void testBadFn() {
		IfStatement statement = buildTLang("x.zzzzz()", 2, IfStatement.class);
	}


	// --
//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private DTypeRegistry registry;
	private TLangProgram recentProgram;

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
	private <T extends TLangStatement> T buildTLang(String tlang, int expectedSize, Class<T> clazz) {
		InputFunctionDefStatementExp infnExp = doTLang(tlang);
		TLangProgramBuilder programBuilder = new TLangProgramBuilder(delia.getFactoryService(), registry);

		InputFunctionBodyExp body = infnExp.bodyExp;
		InputFuncMappingExp mappingExp = (InputFuncMappingExp) body.statementL.get(0);
		TLangProgram program = programBuilder.build(mappingExp);
		recentProgram = program;
		assertEquals(expectedSize, program.statements.size());
		return (T)program.statements.get(0);
	}
	private InputFunctionDefStatementExp doTLang(String tlang) {
		String src = String.format("input function foo(Customer c, Address a) { field -> c.firstName using { %s }}", tlang);
		InputFunctionDefStatementExp infnExp = parse(src);
		return infnExp;
	}
	
}
