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
import org.delia.dataimport.CSVFileLoader;
import org.delia.dataimport.DataImportService;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.log.LogLevel;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.ImportMetricObserver;
import org.delia.runner.inputfunction.ImportSpec;
import org.delia.runner.inputfunction.ImportSpecBuilder;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.InputFunctionService;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.LineObjIteratorImpl;
import org.delia.runner.inputfunction.OutputFieldHandle;
import org.delia.runner.inputfunction.ProgramSet;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class FieldHandleTests  extends NewBDDBase {

	public static class SimpleImportMetricObserver implements ImportMetricObserver {
		public int rowCounter; //num rows attempted
		public int failedRowCounter;
		public int successfulRowCounter;
		public int[] currentRowMetrics = new int[OutputFieldHandle.NUM_METRICS];
//		public int[] totalMetrics = new int[OutputFieldHandle.NUM_METRICS];

		@Override
		public void onRowStart(ImportSpec ispec, int rowNum) {
			rowCounter++;
		}

		@Override
		public void onRowEnd(ImportSpec ispec, int rowNum, boolean success) {
			if (! success) {
				failedRowCounter++;
			} else {
				successfulRowCounter++;
			}
			
			int n = 0;
			for(int i = 0; i < currentRowMetrics.length; i++) {
				n += currentRowMetrics[i];
			}
			//n is number of errors for this row
			
		}

		@Override
		public void onNoMappingError(ImportSpec ispec, OutputFieldHandle ofh) {
			ofh.arMetrics[OutputFieldHandle.INDEX_N]++;
		}

		@Override
		public void onMissingError(ImportSpec ispec, OutputFieldHandle ofh) {
			ofh.arMetrics[OutputFieldHandle.INDEX_M]++;
		}

		@Override
		public void onInvalidError(ImportSpec ispec, OutputFieldHandle ofh) {
			ofh.arMetrics[OutputFieldHandle.INDEX_I]++;
		}

		@Override
		public void onDuplicateError(ImportSpec ispec, OutputFieldHandle ofh) {
			ofh.arMetrics[OutputFieldHandle.INDEX_D]++;
		}

		@Override
		public void onRelationError(ImportSpec ispec, OutputFieldHandle ofh) {
			ofh.arMetrics[OutputFieldHandle.INDEX_R]++;
		}
		
	}

	@Test
	public void test1() {
		String path = BASE_DIR + "categories.csv";
		createDelia(true);
		//		CSVFileLoader fileLoader = new CSVFileLoader(path);
		//		numExpectedColumnsProcessed = 4;
		//		buildAndRun(true, fileLoader, 8);

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

	// --
	private final String BASE_DIR = NorthwindHelper.BASE_DIR;

	//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private int numExpectedColumnsProcessed;

	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
	}
	private void createDelia(boolean inOrder) {
		String src = buildCustomerSrc(false);
		this.delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
	}
	private String buildCustomerSrc(boolean withRules) {
		String rules = withRules ? "name.len() > 4" : "";
		String src = String.format(" type Customer struct {id int primaryKey, wid int, name string } %s end", rules);
		src += " input function foo(Customer c) { ID -> c.id, WID -> c.wid, NAME -> c.name}";

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
	private InputFunctionResult buildAndRun(boolean inOrder, LineObjIterator lineObjIter, int expectedNumRows) {
		createDelia(inOrder);
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
