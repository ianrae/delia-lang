package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.app.NorthwindHelper;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.dataimport.DataImportService;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.log.LogLevel;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.ImportSpec;
import org.delia.runner.inputfunction.ImportSpecBuilder;
import org.delia.runner.inputfunction.InputFunctionRequest;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.InputFunctionService;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.LineObjIteratorImpl;
import org.delia.runner.inputfunction.OutputFieldHandle;
import org.delia.runner.inputfunction.ProgramSet;
import org.delia.runner.inputfunction.SimpleImportMetricObserver;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class FieldHandleTests  extends NewBDDBase {

	@Test
	public void testFieldHandle() {
		createDelia(0);

		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		ProgramSet progset = inputFnSvc.buildProgram("foo", session);
		assertEquals(3, progset.fieldMap.size());
		ProgramSet.OutputSpec ospec = progset.outputSpecs.get(0);
		DStructType structType = ospec.structType;
		ImportSpecBuilder ispecBuilder = new ImportSpecBuilder();

		ImportSpec ispec = ispecBuilder.buildSpecFor(progset, structType);
		assertEquals(3, ispec.ofhList.size());
		OutputFieldHandle ofh = ispec.ofhList.get(0);
		assertEquals(structType, ofh.structType);
		assertEquals(0, ofh.fieldIndex);
		assertEquals("id", ofh.fieldName);

		ofh.ifhIndex = -1;
		ispecBuilder.addInputColumn(ispec, "ID", 11, "id");
		assertEquals(0, ofh.ifhIndex);
	}
	
	@Test
	public void testNError() {
		createDelia(1);
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		ProgramSet progset = buildProgSet(inputFnSvc, observer, 2); 
		
		LineObjIterator lineObjIter = createIter(2);
		InputFunctionResult result = runImport(inputFnSvc, progset, lineObjIter); //inputFnSvc.process(request, lineObjIter);
		chkResult(result, 2, 2, 0);
		
		chkObserver(observer, 2, 0, 2);
		assertEquals(2, observer.currentRowMetrics[OutputFieldHandle.INDEX_N]);
	}
	@Test
	public void testMError() {
		createDelia(0);
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		ProgramSet progset = buildProgSet(inputFnSvc, observer, 3); 
		
		LineObjIterator lineObjIter = createIter(2, null); //no bob
		InputFunctionResult result = runImport(inputFnSvc, progset, lineObjIter); //inputFnSvc.process(request, lineObjIter);
		chkResult(result, 2, 3, 0);
		
		chkObserver(observer, 2, 0, 2);
		assertEquals(2, observer.currentRowMetrics[OutputFieldHandle.INDEX_M]);
	}
	@Test
	public void testMErrorEmptyString() {
		createDelia(0);
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		ProgramSet progset = buildProgSet(inputFnSvc, observer, 3); 
		
		LineObjIterator lineObjIter = createIter(2, ""); //"" should be treated same as null
		InputFunctionResult result = runImport(inputFnSvc, progset, lineObjIter); //inputFnSvc.process(request, lineObjIter);
		chkResult(result, 2, 3, 0);
		
		chkObserver(observer, 2, 0, 2);
		assertEquals(2, observer.currentRowMetrics[OutputFieldHandle.INDEX_M]);
	}
	
	@Test
	public void testIError() {
		createDelia(0);
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		ProgramSet progset = buildProgSet(inputFnSvc, observer, 3); 
		
		LineObjIterator lineObjIter = createIter(2);
		LineObj line1 = currentLineObjL.get(0);
		line1.elements[1] = "notanint";
		InputFunctionResult result = runImport(inputFnSvc, progset, lineObjIter); //inputFnSvc.process(request, lineObjIter);
		chkResult(result, 2, 3, 1);
		
		chkObserver(observer, 2, 1, 1);
		assertEquals(1, observer.currentRowMetrics[OutputFieldHandle.INDEX_I1]);
	}
	@Test
	public void testI2Error() {
		createDelia(2);
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		ProgramSet progset = buildProgSet(inputFnSvc, observer, 3); 
		
		LineObjIterator lineObjIter = createIter(2);
		InputFunctionResult result = runImport(inputFnSvc, progset, lineObjIter); //inputFnSvc.process(request, lineObjIter);
		chkResult(result, 2, 3, 2);
		
		chkObserver(observer, 2, 0, 2);
		assertEquals(0, observer.currentRowMetrics[OutputFieldHandle.INDEX_I1]);
		assertEquals(2, observer.currentRowMetrics[OutputFieldHandle.INDEX_I2]);
	}

	
	private ProgramSet buildProgSet(InputFunctionService inputFnSvc, SimpleImportMetricObserver observer, int expectedSize) {
		inputFnSvc.setMetricsObserver(observer);
		ProgramSet progset = inputFnSvc.buildProgram("foo", session);
		assertEquals(expectedSize, progset.fieldMap.size());
		addImportSpec(progset);
		return progset;
	}

	private void chkObserver(SimpleImportMetricObserver observer, int i, int j, int k) {
		assertEquals(i, observer.rowCounter);
		assertEquals(j, observer.successfulRowCounter);
		assertEquals(k, observer.failedRowCounter);
	}

	private void chkResult(InputFunctionResult result, int i, int j, int k) {
		assertEquals(i, result.numRowsProcessed);
		assertEquals(j, result.numColumnsProcessedPerRow);
		assertEquals(k, result.numRowsInserted);
		assertEquals(false, result.wasHalted);
	}

	private InputFunctionResult runImport(InputFunctionService inputFnSvc, ProgramSet progset, LineObjIterator lineObjIter) {
		InputFunctionRequest request = new InputFunctionRequest();
		request.delia = delia;
		request.progset = progset;
		request.session = session;
		InputFunctionResult result = inputFnSvc.process(request, lineObjIter);
		return result;
	}

	private void addImportSpec(ProgramSet progset) {
		ProgramSet.OutputSpec ospec = progset.outputSpecs.get(0);
		ImportSpecBuilder ispecBuilder = new ImportSpecBuilder();
		ospec.ispec = ispecBuilder.buildSpecFor(progset, ospec.structType);
	}

	// --
	private final String BASE_DIR = NorthwindHelper.BASE_DIR;

	//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private int numExpectedColumnsProcessed;
	private List<LineObj> currentLineObjL;


	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
	}
	private void createDelia(int which) {
		String src = buildCustomerSrc(which);
		this.delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
	}
	private String buildCustomerSrc(int which) {
		String rule = which == 2 ? "name.len() > 4" : "";
		String src = String.format(" type Customer struct {id int primaryKey, wid int, name string } %s end", rule);
		if (which == 0 || which == 2) {
			src += " input function foo(Customer c) { ID -> c.id, WID -> c.wid, ";
			src += " NAME -> c.name using { if missing return null} }";
		} else if (which == 1) {
			src += " input function foo(Customer c) { ID -> c.id, NAME -> c.name}";
		}

		return src;
	}


	private String buildCategorySrc(boolean inOrder) {
		if (inOrder) {
			String src = String.format(" type Category struct { categoryID int primaryKey, categoryName string, description string, picture string} end");
			//categoryID,categoryName,description,picture
			src += String.format(" \ninput function foo(Category c) { categoryID -> c.categoryID, categoryName -> c.categoryName, description -> c.description, picture -> c.picture } ");
			src += String.format(" \nlet var1 = 55");

			return src;
		} else {
			String src = String.format(" type Category struct { categoryID int primaryKey, categoryName string, description string, picture string} end");
			//categoryID,categoryName,description,picture
			src += String.format(" \ninput function foo(Category c) { categoryName -> c.categoryName, description -> c.description, picture -> c.picture, categoryID -> c.categoryID } ");
			src += String.format(" \nlet var1 = 55");

			return src;
		}
	}
	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}
	private InputFunctionResult buildAndRun(int which, LineObjIterator lineObjIter, int expectedNumRows) {
		createDelia(which);
		return buildAndRun(lineObjIter, expectedNumRows);
	}
	private InputFunctionResult buildAndRun(LineObjIterator lineObjIter, int expectedNumRows) {
		DataImportService importSvc = new DataImportService(delia, session, 0);

		InputFunctionResult result = importSvc.importIntoDatabase("foo", lineObjIter);
		assertEquals(0, result.errors.size());
		assertEquals(expectedNumRows, result.numRowsProcessed);
		assertEquals(expectedNumRows, result.numRowsInserted);
		assertEquals(numExpectedColumnsProcessed, result.numColumnsProcessedPerRow);
		return result;
	}
	private InputFunctionResult buildAndRunFail(LineObjIterator lineObjIter) {
		DataImportService importSvc = new DataImportService(delia, session, 0);

		InputFunctionResult result = importSvc.importIntoDatabase("foo", lineObjIter);
		assertEquals(0, result.errors.size());
		assertEquals(1, result.numRowsProcessed);
		assertEquals(0, result.numRowsInserted);
		return result;
	}
	private void chkCustomer(Integer id, String expected) {
		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", id.toString());
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(expected, dval.asStruct().getField("name").asString());
		long n  = dao.count("Customer");
		assertEquals(1L, n);
	}
	private void chkNoCustomer(Integer id) {
		DeliaDao dao = new DeliaDao(delia, session);
		ResultValue res = dao.queryByPrimaryKey("Customer", id.toString());
		assertEquals(true, res.ok);
		assertEquals(0, res.getAsDValueList().size());
	}




	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	private LineObjIterator createIter(int n) {
		return createIter(n, "bob");
	}
	private LineObjIterator createIter(int n, String nameStr) {
		List<LineObj> list = new ArrayList<>();
		currentLineObjL = list;
		for(int i = 0; i < n; i++) {
			list.add(this.createLineObj(i + 1, nameStr));
		}
		return new LineObjIteratorImpl(list);
	}
	private LineObj createLineObj(int id, String nameStr) {
		String[] ar = { "", "33", "bob" };
		ar[0] = String.format("%d", id);
		ar[2] = nameStr;

		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}
}
