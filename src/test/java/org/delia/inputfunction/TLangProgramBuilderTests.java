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
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.inputfunction.EndIfStatementExp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.IfStatementExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionBodyExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.compiler.ast.inputfunction.TLangBodyExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.compiler.parser.InputFunctionParser;
import org.delia.compiler.parser.NameAndFuncParser;
import org.delia.compiler.parser.TerminalParser;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.error.SimpleErrorTracker;
import org.delia.tlang.runner.BasicCondition;
import org.delia.tlang.runner.Condition;
import org.delia.tlang.runner.TLangProgram;
import org.delia.tlang.runner.TLangStatement;
import org.delia.tlang.statement.ElseIfStatement;
import org.delia.tlang.statement.EndIfStatement;
import org.delia.tlang.statement.IfStatement;
import org.delia.tlang.statement.ToUpperStatement;
import org.delia.tlang.statement.ValueStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

public class TLangProgramBuilderTests  extends NewBDDBase {
	
	public static class TLangProgramBuilder extends ServiceBase {

		private DTypeRegistry registry;
		private ScalarValueBuilder builder;

		public TLangProgramBuilder(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.et = new SimpleErrorTracker(log); //local et
			this.registry = registry;
			this.builder = factorySvc.createScalarValueBuilder(registry);
		}
		
		TLangProgram build(InputFuncMappingExp mappingExp) {
			if (mappingExp.tlangBody.statementL.isEmpty()) {
				return null; //no tlang
			}
			
			TLangProgram program = new TLangProgram();
			
			for(Exp exp: mappingExp.tlangBody.statementL) {
				TLangStatement statement = parseStatement(exp);
				if (statement != null) {
					program.statements.add(statement);
				}
			}
			return program;
		}

		private TLangStatement parseStatement(Exp exp) {
			if (exp instanceof XNAFMultiExp) {
				XNAFMultiExp multiExp = (XNAFMultiExp) exp;
				if (multiExp.qfeL.size() > 1) {
					//err
					return null;
				}
				XNAFSingleExp fieldOrFn = multiExp.qfeL.get(0);
				if (fieldOrFn.isRuleFn) {
					//fn
					return new ToUpperStatement(); //fix!!
				} else {
					//var reference
					return null;
				}
			} else if (exp instanceof IntegerExp) {
				DValue dval = builder.buildInt(((IntegerExp) exp).val);
				return new ValueStatement(dval);
			} else if (exp instanceof LongExp) {
				DValue dval = builder.buildLong(((LongExp) exp).val);
				return new ValueStatement(dval);
			} else if (exp instanceof NumberExp) {
				DValue dval = builder.buildNumber(((NumberExp) exp).val);
				return new ValueStatement(dval);
			} else if (exp instanceof BooleanExp) {
				DValue dval = builder.buildBoolean(((BooleanExp) exp).val);
				return new ValueStatement(dval);
			} else if (exp instanceof StringExp) {
				DValue dval = builder.buildString(((StringExp) exp).val);
				//TODO date later!1
				return new ValueStatement(dval);
			} else if (exp instanceof IfStatementExp) {
				IfStatementExp ifexp = (IfStatementExp) exp;
				Condition cond = new BasicCondition(true); //!!!
				if (ifexp.isIf) {
					return new IfStatement(cond);
				} else {
					return new ElseIfStatement(cond);
				}
			} else if (exp instanceof EndIfStatementExp) {
				return new EndIfStatement();
			} else {
				//err
				return null;
			}
		}
		
	}
	
	@Test
	public void testTLang2() {
		InputFunctionDefStatementExp infnExp = doTLang("35");
		TLangProgramBuilder programBuilder = new TLangProgramBuilder(delia.getFactoryService(), registry);

		InputFunctionBodyExp body = infnExp.bodyExp;
		InputFuncMappingExp mappingExp = (InputFuncMappingExp) body.statementL.get(0);
		TLangProgram program = programBuilder.build(mappingExp);
		assertEquals(1, program.statements.size());
		ValueStatement statement = (ValueStatement) program.statements.get(0);
		assertEquals(35, statement.getDVal().asInt());
	}
		
		
	private InputFunctionDefStatementExp doTLang(String tlang) {
		String src = String.format("input function foo(Customer c, Address a) { field -> c.firstName using { %s }}", tlang);
		InputFunctionDefStatementExp infnExp = parse(src);
		return infnExp;
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
