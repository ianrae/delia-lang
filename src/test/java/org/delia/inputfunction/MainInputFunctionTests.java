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
import org.delia.error.DeliaError;
import org.delia.inputfunction.InputFunctionTests.HdrInfo;
import org.delia.inputfunction.InputFunctionTests.InputFunctionRunner;
import org.delia.inputfunction.InputFunctionTests.LineObj;
import org.delia.log.LogLevel;
import org.delia.runner.DValueIterator;
import org.delia.runner.ResultValue;
import org.delia.tlang.runner.TLangProgram;
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
	
	public static class InputFunctionRequest {
		public ProgramSet progset;
		public Delia delia;
		public DeliaSession session;
	}
	public static class InputFunctionResult {
		public List<DeliaError> totalErrorL = new ArrayList<>();
		public int numRowsProcessed;
		public int numDValuesProcessed;
	}
	public static class InputFunctionService extends ServiceBase {
		public InputFunctionService(FactoryService factorySvc) {
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

		public InputFunctionResult process(InputFunctionRequest request, LineObjIterator lineObjIter) {
			InputFunctionResult fnResult = new InputFunctionResult();
			InputFunctionRunner inFuncRunner = new InputFunctionRunner(factorySvc, request.session.getExecutionContext().registry);
			HdrInfo hdr = request.progset.hdr;
			inFuncRunner.setProgramSet(request.progset);
			
			int lineNum = 1;
			while(lineObjIter.hasNext()) {
				log.logDebug("line%d:", lineNum);
				fnResult.numRowsProcessed++;
				LineObj lineObj = lineObjIter.next();

				List<DeliaError> errL = new ArrayList<>();
				List<DValue> dvals = inFuncRunner.process(hdr, lineObj, errL); //one row
				if (! errL.isEmpty()) {
					log.logError("failed!");
					fnResult.totalErrorL.addAll(errL);
				} else {
					for(DValue dval: dvals) {
						log.logDebug("line%d: dval '%s'", lineNum, dval.getType().getName());
						fnResult.numDValuesProcessed++;
						executeInsert(dval, request, fnResult);
					}
				}
				lineNum++;
			}
			
			return fnResult;
		}

		private void executeInsert(DValue dval, InputFunctionRequest request, InputFunctionResult fnResult) {
			DValueIterator iter = new DValueIterator(dval);
			request.delia.getOptions().insertPrebuiltValueIterator = iter;
			String typeName = dval.getType().getName();
			String s = String.format("insert %s {}", typeName);
			ResultValue res = request.delia.continueExecution(s, request.session);
			if (! res.ok) {
				//err
				fnResult.totalErrorL.addAll(res.errors);
			}
			request.delia.getOptions().insertPrebuiltValueIterator = null;
		}		
	}


	@Test
	public void test1() {
		InputFunctionService tlangSvc = new InputFunctionService(delia.getFactoryService());
		ProgramSet progset = tlangSvc.buildProgram("foo", session);
		assertEquals(3, progset.map.size());
		
		LineObjIterator lineObjIter = createIter(1);
		InputFunctionRequest request = new InputFunctionRequest();
		request.delia = delia;
		request.progset = progset;
		request.session = session;
		InputFunctionResult result = tlangSvc.process(request, lineObjIter);
		assertEquals(0, result.totalErrorL.size());
		assertEquals(1, result.numRowsProcessed);

		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("bob", dval.asStruct().getField("name").asString());
		
		long n  = dao.count("Customer");
		assertEquals(1L, n);
	}
	
	@Test
	public void test2() {
		InputFunctionService tlangSvc = new InputFunctionService(delia.getFactoryService());
		ProgramSet progset = tlangSvc.buildProgram("foo", session);
		assertEquals(3, progset.map.size());
		
		LineObjIterator lineObjIter = createIter(2);
		InputFunctionRequest request = new InputFunctionRequest();
		request.delia = delia;
		request.progset = progset;
		request.session = session;
		InputFunctionResult result = tlangSvc.process(request, lineObjIter);
		assertEquals(0, result.totalErrorL.size());
		assertEquals(1, result.numRowsProcessed);

		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("bob", dval.asStruct().getField("name").asString());
		
		long n  = dao.count("Customer");
		assertEquals(2L, n);
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
		this.delia.getLog().setLevel(LogLevel.DEBUG);
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

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
	
	
	private LineObjIterator createIter(int n) {
		List<LineObj> list = new ArrayList<>();
		for(int i = 0; i < n; i++) {
			list.add(this.createLineObj(i));
		}
		return new LineObjIterator(list);
	}
	private LineObj createLineObj(int i) {
		String[] ar = { "", "33","bob" };
		ar[0] = String.format("%d", i);
		
		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}





}
