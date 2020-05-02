package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.LogLevel;
import org.delia.runner.DValueIterator;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.HdrInfo;
import org.delia.runner.inputfunction.InputFunctionRunner;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.ProgramSet;
import org.delia.runner.inputfunction.ProgramSpec;
import org.delia.tlang.runner.TLangProgram;
import org.delia.tlang.runner.TLangVarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class InputFunctionRunnerTests  extends NewBDDBase {
	
	@Test
	public void test2() {
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
		inFuncRunner.setProgramSet(progset);
		List<DeliaError> lineErrL = new ArrayList<>();
		List<DValue> dvals = inFuncRunner.process(hdr, lineObj, lineErrL);
		chkNoErrors(lineErrL);
		chkNoErrors(localET.getErrors());
		assertEquals(1, dvals.size());
		
		//hmm. or do we do insert Customer {....}
		//i think we can do insert Customer {} with empty dson and somehow
		//pass in the already build dval runner.setAlreadyBuiltDVal()
		
		DValueIterator iter = new DValueIterator(dvals);
		session.setInsertPrebuiltValueIterator(iter);
		String s = String.format("insert Customer {}");
		ResultValue res = delia.continueExecution(s, session);
		assertEquals(true, res.ok);
		session.setInsertPrebuiltValueIterator(null);
		
		DeliaDao dao = new DeliaDao(delia, session);
		res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("bob", dval.asStruct().getField("name").asString());
	}

	private ProgramSet createProgramSet(HdrInfo hdr, InputFunctionDefStatementExp inFnExp) {
		ProgramSet progset = new ProgramSet();
		progset.hdr = hdr;
		buildProgset(progset, inFnExp);
		return progset;
	}
	
	private void buildProgset(ProgramSet progset, InputFunctionDefStatementExp inFnExp) {
		for(Exp exp: inFnExp.bodyExp.statementL) {
			InputFuncMappingExp mappingExp = (InputFuncMappingExp) exp;
			ProgramSpec spec = new ProgramSpec();
			spec.prog = new TLangProgram();
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
		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}


	private HdrInfo createHdr() {
		HdrInfo hdr = new HdrInfo();
		hdr.map.put(0, "id");
		hdr.map.put(1, "name");
		hdr.map.put(2, "wid");
		return hdr;
	}


	private InputFunctionRunner createXConv() {
		localET = new SimpleErrorTracker(delia.getLog());
		TLangVarEvaluator varEvaluator = new TLangVarEvaluator(session.getExecutionContext());
		return new InputFunctionRunner(delia.getFactoryService(), registry, localET, varEvaluator);
	}


	// --
//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private DTypeRegistry registry;
	private ErrorTracker localET;

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
}
