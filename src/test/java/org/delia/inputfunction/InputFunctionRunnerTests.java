package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.LogLevel;
import org.delia.runner.DValueIterator;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.HdrInfo;
import org.delia.runner.inputfunction.ImportRunnerInitializer;
import org.delia.runner.inputfunction.ImportSpec;
import org.delia.runner.inputfunction.ImportSpecBuilder;
import org.delia.runner.inputfunction.InputFunctionRunner;
import org.delia.runner.inputfunction.InputFunctionServiceOptions;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.ProgramSet;
import org.delia.runner.inputfunction.ProgramSpec;
import org.delia.runner.inputfunction.SimpleImportMetricObserver;
import org.delia.runner.inputfunction.ViaLineInfo;
import org.delia.runner.inputfunction.ViaService;
import org.delia.tlang.TLangProgramBuilder;
import org.delia.tlang.runner.TLangVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class InputFunctionRunnerTests extends InputFunctionTestBase {
	
	@Test
	public void test1() {
		String src = buildSrc();
		this.session = delia.beginSession(src);
		this.registry = session.getExecutionContext().registry;

		InputFunctionRunner inFuncRunner = createXConv();
		
		delia.getLog().setLevel(LogLevel.DEBUG);
		InputFunctionDefStatementExp inFnExp = findInputFn(session, "foo");
		
		HdrInfo hdr = createHdrFrom(inFnExp);
		LineObj lineObj = createLineObj();
		ProgramSet progset = createProgramSet(hdr, inFnExp);
		progset.inFnExp = inFnExp;
		DStructType structType = (DStructType) registry.getType("Customer");
		ProgramSet.OutputSpec ospec = new ProgramSet.OutputSpec();
		ospec.alias = "c";
		ospec.structType = structType;
		progset.outputSpecs.add(ospec);
		addImportSpec(progset);
		
		inFuncRunner.setProgramSet(progset);
		List<DeliaError> lineErrL = new ArrayList<>();
		List<DValue> dvals = inFuncRunner.process(hdr, lineObj, lineErrL, new ViaLineInfo());
		chkNoErrors(lineErrL);
		chkNoErrors(localET.getErrors());
		assertEquals(1, dvals.size());
		
		//hmm. or do we do insert Customer {....}
		//i think we can do insert Customer {} with empty dson and somehow
		//pass in the already build dval runner.setAlreadyBuiltDVal()
		
		DValueIterator iter = new DValueIterator(dvals);
		InputFunctionServiceOptions options = new InputFunctionServiceOptions();
		ImportSpec ispec = progset.outputSpecs.get(0).ispec;
		ImportRunnerInitializer initializer = new ImportRunnerInitializer(delia.getFactoryService(), iter, session, options, 
						ispec, new SimpleImportMetricObserver());
		session.setRunnerIntiliazer(initializer);
		String s = String.format("insert Customer {}");
		ResultValue res = delia.continueExecution(s, session);
		assertEquals(true, res.ok);
		session.setRunnerIntiliazer(null);
		
		DeliaGenericDao dao = new DeliaGenericDao(delia, session);
		res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("bob", dval.asStruct().getField("name").asString());
	}
	
	@Test
	public void test2() {
		String src = buildSrc2();
		this.session = delia.beginSession(src);
		this.registry = session.getExecutionContext().registry;
		InputFunctionRunner inFuncRunner = createXConv();
		
		delia.getLog().setLevel(LogLevel.DEBUG);
		InputFunctionDefStatementExp inFnExp = findInputFn(session, "foo");
		
		HdrInfo hdr = createHdrFrom(inFnExp);
		LineObj lineObj = createLineObj();
		ProgramSet progset = createProgramSet(hdr, inFnExp);
		progset.inFnExp = inFnExp;
		DStructType structType = (DStructType) registry.getType("Customer");
		ProgramSet.OutputSpec ospec = new ProgramSet.OutputSpec();
		ospec.alias = "c";
		ospec.structType = structType;
		progset.outputSpecs.add(ospec);
		addImportSpec(progset);
		
		inFuncRunner.setProgramSet(progset);
		List<DeliaError> lineErrL = new ArrayList<>();
		List<DValue> dvals = inFuncRunner.process(hdr, lineObj, lineErrL, new ViaLineInfo());
		chkNoErrors(lineErrL);
		chkNoErrors(localET.getErrors());
		assertEquals(1, dvals.size());
		
		DValue dval = dvals.get(0);
		assertEquals(101, dval.asStruct().getField("id").asInt());
		assertEquals("bob", dval.asStruct().getField("name").asString());
	}
	

	private ImportSpec addImportSpec(ProgramSet progset) {
		ProgramSet.OutputSpec ospec = progset.outputSpecs.get(0);
		ImportSpecBuilder ispecBuilder = new ImportSpecBuilder();
		ospec.ispec = ispecBuilder.buildSpecFor(progset, ospec.structType);
		
		ispecBuilder.addInputColumn(ospec.ispec, "ID", 0, "id");
		ispecBuilder.addInputColumn(ospec.ispec, "WID", 1, "wid");
		ispecBuilder.addInputColumn(ospec.ispec, "NAME", 2, "name");
		
		return ospec.ispec;
	}
	private ProgramSet createProgramSet(HdrInfo hdr, InputFunctionDefStatementExp inFnExp) {
		ProgramSet progset = new ProgramSet();
		progset.hdr = hdr;
		buildProgset(progset, inFnExp);
		return progset;
	}
	
	private void buildProgset(ProgramSet progset, InputFunctionDefStatementExp inFnExp) {
		TLangProgramBuilder progBuilder = new TLangProgramBuilder(delia.getFactoryService(), registry);
		for(Exp exp: inFnExp.bodyExp.statementL) {
			InputFuncMappingExp mappingExp = (InputFuncMappingExp) exp;
			ProgramSpec spec = new ProgramSpec();
			spec.prog = progBuilder.build(mappingExp);
			spec.inputField = mappingExp.getInputField();
			spec.outputField = mappingExp.outputField;
			progset.fieldMap.put(mappingExp.getInputField(), spec);
		}
	}

	private void chkNoErrors(List<DeliaError> totalErrorL) {
		for(DeliaError err: totalErrorL) {
			delia.getLog().log("err: %s", err.toString());
		}
		assertEquals(0, totalErrorL.size());
	}

	private HdrInfo createHdrFrom(InputFunctionDefStatementExp inFnExp) {
		HdrInfo hdr = new HdrInfo();
		int index = 0;
		for(Exp exp: inFnExp.bodyExp.statementL) {
			InputFuncMappingExp mapping = (InputFuncMappingExp) exp;
			hdr.map.put(index, mapping.getInputField());
			index++;
		}
		return hdr;
	}

	private InputFunctionDefStatementExp findInputFn(DeliaSession session2, String fnName) {
		InputFunctionDefStatementExp infnExp = session2.getExecutionContext().inputFnMap.get(fnName);
		return infnExp;
	}

	private LineObj createLineObj() {
		String[] ar = { "1", "33","bob" };
		LineObj lineObj = new LineObj(ar, 100);
		return lineObj;
	}


	private HdrInfo createHdr() {
		HdrInfo hdr = new HdrInfo();
		hdr.map.put(0, "ID");
		hdr.map.put(1, "WID");
		hdr.map.put(2, "NAME");
		return hdr;
	}


	private InputFunctionRunner createXConv() {
		localET = new SimpleErrorTracker(delia.getLog());
		TLangVarEvaluator varEvaluator = new TLangVarEvaluator(session.getExecutionContext());
		ViaService viaSvc = new ViaService(delia.getFactoryService());
		return new InputFunctionRunner(delia.getFactoryService(), registry, localET, varEvaluator, viaSvc);
	}


	// --
	private DTypeRegistry registry;
	private ErrorTracker localET;
	private boolean useSrc2;

	@Before
	public void init() {
		DeliaGenericDao dao = this.createDao();
		this.delia = dao.getDelia();
	}
	private String buildSrc() {
		String src = " type Customer struct {id int unique, wid int, name string } end";
		src += " input function foo(Customer c) { ID -> c.id, WID -> c.wid, NAME -> c.name}";
		return src;
	}
	private String buildSrc2() {
		String src = " type Customer struct {id int unique, wid int, name string } end";
		src += " input function foo(Customer c) { ID -> c.id using { LINENUM }, WID -> c.wid, NAME -> c.name}";
		
		return src;
	}

	private DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaGenericDao(delia);
	}
}
