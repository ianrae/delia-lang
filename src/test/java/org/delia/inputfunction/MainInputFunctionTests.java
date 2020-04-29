package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.memdb.filter.OP;
import org.delia.error.DeliaError;
import org.delia.inputfunction.InputFunctionTests.HdrInfo;
import org.delia.inputfunction.InputFunctionTests.LineObj;
import org.delia.tlang.runner.BasicCondition;
import org.delia.tlang.runner.DValueOpEvaluator;
import org.delia.tlang.runner.IsMissingCondition;
import org.delia.tlang.runner.OpCondition;
import org.delia.tlang.runner.TLangProgram;
import org.delia.tlang.runner.TLangRunner;
import org.delia.tlang.statement.EndIfStatement;
import org.delia.tlang.statement.IfStatement;
import org.delia.tlang.statement.ToUpperStatement;
import org.delia.tlang.statement.ValueStatement;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

public class MainInputFunctionTests  extends NewBDDBase {
	
	public static class ProgramSet {
		public Map<String,TLangProgram> map = new ConcurrentHashMap<>();
	}
	
	public static class TLangService extends ServiceBase {

		public TLangService(FactoryService factorySvc) {
			super(factorySvc);
		}
		
		public ProgramSet buildProgram(String inputFnName, DeliaSession session) {
			ProgramSet progset = new ProgramSet();
			InputFunctionDefStatementExp infnExp = findFunction(inputFnName, session);
			for(Exp exp: infnExp.bodyExp.statementL) {
				InputFuncMappingExp mappingExp = (InputFuncMappingExp) exp;
				TLangProgram program = new TLangProgram();
				String infield = mappingExp.inputField.name();
				
				progset.map.put(infield, program);
			}
			return progset;
		}
		private InputFunctionDefStatementExp findFunction(String inputFnName, DeliaSession session) {
			DeliaSessionImpl sessionimpl = (DeliaSessionImpl) session;
			for(Exp exp: sessionimpl.expL) {
				if (exp instanceof InputFunctionDefStatementExp) {
					InputFunctionDefStatementExp infnExp = (InputFunctionDefStatementExp) exp;
					if (infnExp.funcName.equals(inputFnName)) {
						return infnExp;
					}
				}
			}
			return null;
		}
		
		public List<DValue> process(HdrInfo hdr, LineObj lineObj, List<DeliaError> totalErrorL) {
			return null;
		}		
	}
	
	
	
	@Test
	public void test1() {
		
		TLangService tlangSvc = new TLangService(delia.getFactoryService());
		
		ProgramSet progset = tlangSvc.buildProgram("foo", session);
		assertEquals(3, progset.map.size());
		
	}


	// --
	//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private DTypeRegistry registry;
	ScalarValueBuilder builder;

	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
		String src = buildSrc();
		this.session = delia.beginSession(src);
		this.registry = session.getExecutionContext().registry;
		this.builder = delia.getFactoryService().createScalarValueBuilder(registry);
	}
	private String buildSrc() {
		String src = " type Customer struct {id int unique, wid int, name string } end";
		src += " input function foo(Customer c) { ID -> c.id, WID -> c.wid, NAME -> c.name}";

		return src;
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}
	private TLangProgram createProgram() {
		TLangProgram prog = new TLangProgram();
		prog.statements.add(new ToUpperStatement());
		prog.statements.add(new TLangTests.AddXStatement());
		return prog;
	}
	private TLangProgram createProgram2(boolean bb) {
		TLangProgram prog = new TLangProgram();
		prog.statements.add(new IfStatement(new BasicCondition(bb)));
		prog.statements.add(new ToUpperStatement());
		prog.statements.add(new EndIfStatement());
		prog.statements.add(new TLangTests.AddXStatement());
		return prog;
	}
	private TLangProgram createProgram3(boolean bb) {
		TLangProgram prog = new TLangProgram();
		prog.statements.add(new IfStatement(new IsMissingCondition()));

		DValue x = builder.buildString("Z");
		prog.statements.add(new ValueStatement(x));
		prog.statements.add(new EndIfStatement());
		if (bb) {
			prog.statements.add(new TLangTests.AddXStatement());
		}
		return prog;
	}
	private TLangProgram createProgram4(boolean bb) {
		TLangProgram prog = new TLangProgram();
		
		DValueOpEvaluator eval = new DValueOpEvaluator(OP.EQ);
		OpCondition cond = new OpCondition(eval);
		DValue xx = builder.buildString("abc");
		eval.setRightVar(xx);
		prog.statements.add(new IfStatement(cond));

		DValue x = builder.buildString("Z");
		prog.statements.add(new ValueStatement(x));
		prog.statements.add(new EndIfStatement());
		if (bb) {
			prog.statements.add(new TLangTests.AddXStatement());
		}
		return prog;
	}

	private TLangRunner createTLangRunner() {
		return new TLangRunner(delia.getFactoryService(), registry);
	}
	private void chkTrail(TLangRunner tlangRunner, String expected) {
		delia.getLog().log(tlangRunner.trail.getTrail());
		assertEquals(expected, tlangRunner.trail.getTrail());
	}


	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}


}
