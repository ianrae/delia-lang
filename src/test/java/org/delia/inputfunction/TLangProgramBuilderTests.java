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
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.inputfunction.EndIfStatementExp;
import org.delia.compiler.ast.inputfunction.IfStatementExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionBodyExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
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
import org.delia.db.memdb.filter.OP;
import org.delia.error.SimpleErrorTracker;
import org.delia.runner.DeliaException;
import org.delia.tlang.runner.BasicCondition;
import org.delia.tlang.runner.Condition;
import org.delia.tlang.runner.DValueOpEvaluator;
import org.delia.tlang.runner.OpCondition;
import org.delia.tlang.runner.StatementOpEvaluator;
import org.delia.tlang.runner.TLangProgram;
import org.delia.tlang.runner.TLangStatement;
import org.delia.tlang.runner.TLangVarEvaluator;
import org.delia.tlang.statement.ElseIfStatement;
import org.delia.tlang.statement.EndIfStatement;
import org.delia.tlang.statement.IfStatement;
import org.delia.tlang.statement.ToUpperStatement;
import org.delia.tlang.statement.TrimStatement;
import org.delia.tlang.statement.ValueStatement;
import org.delia.tlang.statement.VariableStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
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
			
			validateProgram(program);
			return program;
		}

		private void validateProgram(TLangProgram program) {
			boolean inIf = false;
			for(TLangStatement statement: program.statements) {
				if (statement instanceof IfStatement) {
					if (inIf) {
						DeliaExceptionHelper.throwError("tlang-nested-if-not-allowed", "Nested if statements not allowed in TLANG");
					} 
					inIf = true;
				} else if (statement instanceof ElseIfStatement) {
					if (!inIf) {
						DeliaExceptionHelper.throwError("tlang-missing-if", "elseif without a preceeding if statement.");
					}
				} else if (statement instanceof EndIfStatement) {
					if (!inIf) {
						DeliaExceptionHelper.throwError("tlang-missing-if", "endif without a preceeding if statement.");
					}
					inIf = false;
				}
			}
			
			if (inIf) {
				DeliaExceptionHelper.throwError("tlang-missing-endif", "if without an endif statement.");
			}
		}

		private TLangStatement parseStatement(Exp exp) {
			if (exp instanceof XNAFMultiExp) {
				XNAFMultiExp multiExp = (XNAFMultiExp) exp;
				if (multiExp.qfeL.size() > 2) {
					//err
					return null;
				} else if (multiExp.qfeL.size() == 2) {
					XNAFSingleExp field = multiExp.qfeL.get(0);
					XNAFSingleExp fnn = multiExp.qfeL.get(1);
					DeliaExceptionHelper.throwError("tlang-x-y-not-supported", "tlang x.y not supported");
					return null;
				} else {
					XNAFSingleExp fieldOrFn = multiExp.qfeL.get(0);
					if (fieldOrFn.isRuleFn) {
						return buildFnStatement(fieldOrFn);
					} else {
						//var reference
						return new VariableStatement(fieldOrFn.funcName);
					}
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
				Condition cond = buildCondition(ifexp); //new BasicCondition(true); //!!!
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

		private Condition buildCondition(IfStatementExp ifexp) {
			FilterOpExp filter = (FilterOpExp) ifexp.condition;
			OP op = OP.createFromString(filter.op);
			
			TLangStatement stat1 = this.parseStatement(filter.op1); //** recursion **
			TLangStatement stat2 = this.parseStatement(filter.op2); //** recursion **
			//how set stat1?
			StatementOpEvaluator evaluator = new StatementOpEvaluator(op);
			evaluator.setRightVar(stat2);
			DValueOpEvaluator dvalEval = new DValueOpEvaluator(op);
			evaluator.setDvalEvaluator(dvalEval);
			
			OpCondition cond = new OpCondition(evaluator);
			return cond;
		}

		private TLangStatement buildFnStatement(XNAFSingleExp fieldOrFn) {
			String fnName = fieldOrFn.funcName;
			switch(fnName) {
			case "toUpperCase":
				return new ToUpperStatement();
			case "trim":
				return new TrimStatement();
			default:
				DeliaExceptionHelper.throwError("tlang-unknown-fn", "Unknown function '%s'", fnName);
			}
			return null;
		}
		
	}
	
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
		IfStatement statement = buildTLang("if true then zz,endif", 2, IfStatement.class);
		assertEquals("if", statement.getName());
		
		EndIfStatement stat2 = (EndIfStatement) recentProgram.statements.get(1);
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
		IfStatement statement = buildTLang("if x > 5 then zz,endif", 2, IfStatement.class);
		assertEquals("if", statement.getName());
		
		EndIfStatement stat2 = (EndIfStatement) recentProgram.statements.get(1);
		assertEquals("endif", stat2.getName());
	}
	
	
	@Test
	public void testToUpperCase() {
		IfStatement statement = buildTLang("x.toUpperCase()", 2, IfStatement.class);
		assertEquals("xx", statement.getName());
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
