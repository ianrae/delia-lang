package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.error.DeliaError;
import org.delia.log.LogLevel;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.ImportSpecBuilder;
import org.delia.runner.inputfunction.InputFunctionRequest;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.InputFunctionService;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.LineObjIteratorImpl;
import org.delia.runner.inputfunction.ProgramSet;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class InputFunctionServiceTests  extends NewBDDBase {

	@Test
	public void test1() {
		createDelia(false);
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		ProgramSet progset = inputFnSvc.buildProgram("foo", session);
		assertEquals(3, progset.fieldMap.size());
		addImportSpec(progset);
		
		LineObjIterator lineObjIter = createIter(1, true);
		InputFunctionRequest request = new InputFunctionRequest();
		request.delia = delia;
		request.progset = progset;
		request.session = session;
		InputFunctionResult result = inputFnSvc.process(request, lineObjIter);
		assertEquals(0, result.errors.size());
		assertEquals(1, result.numRowsProcessed);
		assertEquals(1, result.numRowsInserted);

		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("bob", dval.asStruct().getField("name").asString());
		
		long n  = dao.count("Customer");
		assertEquals(1L, n);
	}
	
	private void addImportSpec(ProgramSet progset) {
		ProgramSet.OutputSpec ospec = progset.outputSpecs.get(0);
		ImportSpecBuilder ispecBuilder = new ImportSpecBuilder();
		ospec.ispec = ispecBuilder.buildSpecFor(progset, ospec.structType);
	}

	@Test
	public void test2() {
		createDelia(false);
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		ProgramSet progset = inputFnSvc.buildProgram("foo", session);
		assertEquals(3, progset.fieldMap.size());
		addImportSpec(progset);

		LineObjIterator lineObjIter = createIter(2, true);
		InputFunctionRequest request = new InputFunctionRequest();
		request.delia = delia;
		request.progset = progset;
		request.session = session;
		InputFunctionResult result = inputFnSvc.process(request, lineObjIter);
		assertEquals(0, result.errors.size());
		assertEquals(2, result.numRowsProcessed);
		assertEquals(2, result.numRowsInserted);

		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals("bob", dval.asStruct().getField("name").asString());
		
		long n  = dao.count("Customer");
		assertEquals(2L, n);
		
		res = dao.queryByPrimaryKey("Customer", "2");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals("bob", dval.asStruct().getField("name").asString());
	}

	@Test
	public void test3Rule() {
		createDelia(true);
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		ProgramSet progset = inputFnSvc.buildProgram("foo", session);
		assertEquals(3, progset.fieldMap.size());
		addImportSpec(progset);

		LineObjIterator lineObjIter = createIter(2,true);
		InputFunctionRequest request = new InputFunctionRequest();
		request.delia = delia;
		request.progset = progset;
		request.session = session;
		InputFunctionResult result = inputFnSvc.process(request, lineObjIter);
		assertEquals(2, result.errors.size());
		assertEquals(2, result.numRowsProcessed);
		assertEquals(2, result.numRowsInserted);
		
		DeliaError err = result.errors.get(0);
		assertEquals(1, err.getLineNum());

		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(null, dval);
		
		long n  = dao.count("Customer");
		assertEquals(0L, n);
	}
	@Test
	public void test4BadLineObj() {
		createDelia(true);
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		ProgramSet progset = inputFnSvc.buildProgram("foo", session);
		assertEquals(3, progset.fieldMap.size());
		addImportSpec(progset);

		LineObjIterator lineObjIter = createIter(2,false);
		InputFunctionRequest request = new InputFunctionRequest();
		request.delia = delia;
		request.progset = progset;
		request.session = session;
		InputFunctionResult result = inputFnSvc.process(request, lineObjIter);
		assertEquals(2, result.errors.size());
		assertEquals(2, result.numRowsProcessed);
		assertEquals(0, result.numRowsInserted);
		
		DeliaError err = result.errors.get(0);
		assertEquals(1, err.getLineNum());

		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", "1");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(null, dval);
		
		long n  = dao.count("Customer");
		assertEquals(0L, n);
	}
	
	@Test(expected=DeliaException.class)
	public void test4BadInputFunc() {
//		createDelia(true);
		String src = buildSrcBadInputFunc(false);
		this.session = delia.beginSession(src);
	}

	// --
	//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;

	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
	}
	private void createDelia(boolean withRules) {
		String src = buildSrc(withRules);
		this.delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
	}
	private String buildSrc(boolean withRules) {
		String rules = withRules ? "name.len() > 4" : "";
		String src = String.format(" type Customer struct {id int primaryKey, wid int, name string } %s end", rules);
		src += " input function foo(Customer c) { ID -> c.id, WID -> c.wid, NAME -> c.name}";

		return src;
	}
	private String buildSrcBadInputFunc(boolean withRules) {
		String rules = withRules ? "name.len() > 4" : "";
		String src = String.format(" type Customer struct {id int primaryKey, wid int, name string } %s end", rules);
		src += " input function foo(Customer c) { ID -> c.xid, WID -> c.wid, NAME -> c.name}";

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
	
	private LineObjIterator createIter(int n, boolean goodObj) {
		List<LineObj> list = new ArrayList<>();
		for(int i = 0; i < n; i++) {
			list.add(this.createLineObj(i + 1, goodObj));
		}
		return new LineObjIteratorImpl(list);
	}
	private LineObj createLineObj(int id, boolean goodObj) {
		String[] ar = { "", "33","bob" };
		ar[0] = String.format("%d", id);
		if (! goodObj) {
			ar[1] = "bbb"; //not an int
		}
		
		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}
}
