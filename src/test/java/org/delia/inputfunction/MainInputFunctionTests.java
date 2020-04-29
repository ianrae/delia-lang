package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
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
import org.delia.compiler.ast.inputfunction.IdentPairExp;
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
import org.delia.inputfunction.InputFunctionTests.InputFunctionRunner;
import org.delia.inputfunction.InputFunctionTests.LineObj;
import org.delia.runner.DValueIterator;
import org.delia.runner.ResultValue;
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
	
	public static class ProgramSpec {
		public TLangProgram prog;
		public IdentPairExp outputField;
	}
	
	public static class ProgramSet {
		public Map<String,ProgramSpec> map = new ConcurrentHashMap<>();
		public HdrInfo hdr;
	}
	
	public class LineObjIterator implements Iterator<LineObj> {
		private List<LineObj> list;
		private int index;

		public LineObjIterator(List<LineObj> list) {
			this.list = list;
			this.index = 0;
		}
		
		@Override
		public boolean hasNext() {
			return index < list.size();
		}

		@Override
		public LineObj next() {
			LineObj con = list.get(index++);
			return con;
		}
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
				
				ProgramSpec spec = new ProgramSpec();
				spec.outputField = mappingExp.outputField;
				spec.prog = program;
				progset.map.put(infield, spec);
			}
			
			progset.hdr = this.createHdrFrom(infnExp);
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
		private HdrInfo createHdrFrom(InputFunctionDefStatementExp inFnExp) {
			HdrInfo hdr = new HdrInfo();
			int index = 0;
			for(Exp exp: inFnExp.bodyExp.statementL) {
				InputFuncMappingExp mapping = (InputFuncMappingExp) exp;
				hdr.map.put(index, mapping.inputField.name());
				index++;
			}
			return hdr;
		}
		
		
		public List<DValue> process(ProgramSet progset, LineObjIterator lineObjIter, Delia delia, DeliaSession session, List<DeliaError> totalErrorL) {
			
			while(lineObjIter.hasNext()) {
				LineObj lineObj = lineObjIter.next();
				
				InputFunctionRunner inFuncRunner = new InputFunctionRunner(factorySvc, session.getExecutionContext().registry);
				
				HdrInfo hdr = progset.hdr;
				inFuncRunner.setProgramSet(progset);
				List<DeliaError> errL = new ArrayList<>();
				List<DValue> dvals = inFuncRunner.process(hdr, lineObj, errL);
				if (! errL.isEmpty()) {
					log.logError("failed!");
				} else {
					//hmm. or do we do insert Customer {....}
					//i think we can do insert Customer {} with empty dson and somehow
					//pass in the already build dval runner.setAlreadyBuiltDVal()
					//TODO dvals may be Customer,Address, ... fix this code here
					DValueIterator iter = new DValueIterator(dvals);
					delia.getOptions().insertPrebuiltValueIterator = iter;
					String s = String.format("insert Customer {}");
					ResultValue res = delia.continueExecution(s, session);
					if (! res.ok) {
						//err
					}
					delia.getOptions().insertPrebuiltValueIterator = null;
				}
			}
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
