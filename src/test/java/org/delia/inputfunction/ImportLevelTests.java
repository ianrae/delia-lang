package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.Delia;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dataimport.DataImportService;
import org.delia.db.DBType;
import org.delia.log.LogLevel;
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
import org.junit.Before;
import org.junit.Test;

public class ImportLevelTests extends InputFunctionTestBase {

	@Test
	public void testLevel1() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		buildSrc(delia, 0);

		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		ProgramSet progset = buildProgSet(inputFnSvc, observer, 3); 

		LineObjIterator lineObjIter = createIter(2);
		LineObj line1 = currentLineObjL.get(0);
		line1.elements[1] = "notanint";
		InputFunctionResult result = runImport(delia, inputFnSvc, progset, lineObjIter); //inputFnSvc.process(request, lineObjIter);
		chkResult(result, 2, 3, 1);

		chkObserver(observer, 2, 1, 1);
		assertEquals(1, observer.currentRowMetrics[OutputFieldHandle.INDEX_M]);
		assertEquals(1, observer.currentRowMetrics[OutputFieldHandle.INDEX_I1]);
		dumpImportReport(delia, result, observer);
	}
	
	// --
	private List<LineObj> currentLineObjL;
	
	@Before
	public void init() {
	}
	private void buildSrc(Delia delia, int which) {
		String src = createCustomerSrc(which);
		delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
	}
	private String createCustomerSrc(int which) {

		String rule = which == 2 ? "name.len() > 4" : "";
		String src = "";
		if (which == 3) {
			src = String.format(" type Customer struct {id long primaryKey, wid int unique, name string } %s end", rule);
		} else 
		{
			src = String.format(" type Customer struct {id long primaryKey, wid int, name string } %s end", rule);
		}

		if (which == 1) {
			src += " input function foo(Customer c) { ID -> c.id, NAME -> c.name}";
		} else {
			src += " input function foo(Customer c) { ID -> c.id, WID -> c.wid, ";
			src += " NAME -> c.name using { if missing return null} }";
		}

		return src;
	}
	private void dumpImportReport(Delia delia, InputFunctionResult result, SimpleImportMetricObserver observer) {
		DataImportService dataImportSvc = new DataImportService(session, 999);
		dataImportSvc.dumpImportReport(result, observer);
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

	private InputFunctionResult runImport(Delia delia, InputFunctionService inputFnSvc, ProgramSet progset, LineObjIterator lineObjIter) {
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
