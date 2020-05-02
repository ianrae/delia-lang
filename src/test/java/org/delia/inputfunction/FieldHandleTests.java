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
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.InputFunctionService;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.LineObjIteratorImpl;
import org.delia.runner.inputfunction.ProgramSet;
import org.delia.runner.inputfunction.ProgramSpec;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.junit.Before;
import org.junit.Test;

public class FieldHandleTests  extends NewBDDBase {

	public static class InputFieldHandle {
		public String columnName;
		public int columnIndex; //element of LineObj.   SYNTH + id for synthetic
	}
	public static class OutputFieldHandle {
		public static final int NUM_METRICS = 5;
		public static final int INDEX_N = 0;
		public static final int INDEX_M = 1;
		public static final int INDEX_I = 2;
		public static final int INDEX_D = 3;
		public static final int INDEX_R = 4;

		public DStructType structType;
		public String fieldName;
		public int fieldIndex; //index of field in structType
		public int ifhIndex;
		//TODO: add list additional ifh indexes for combine(FIRSTNAME,'',LASTNAME)
		public int[] arMetrics; //for NMIDR error counters
	}

	public static class ImportSpec {
		public DStructType structType;
		public List<InputFieldHandle> ifhList = new ArrayList<>();
		public List<OutputFieldHandle> ofhList = new ArrayList<>();
	}

	public interface ImportMetricObserver {
		void onRowStart(ImportSpec ispec, int rowNum);
		void onRowEnd(ImportSpec ispec, int rowNum, boolean success);

		void onNoMappingError(ImportSpec ispec, OutputFieldHandle ofh);
		void onMissingError(ImportSpec ispec, OutputFieldHandle ofh);
		void onInvalidError(ImportSpec ispec, OutputFieldHandle ofh);
		void onDuplicateError(ImportSpec ispec, OutputFieldHandle ofh);
		void onRelationError(ImportSpec ispec, OutputFieldHandle ofh);
	}

	public static class ImportSpecBuilder {

		public ImportSpec buildSpecFor(ProgramSet progset, DStructType structType) {
			ImportSpec ispec = new ImportSpec();
			ispec.structType = structType;

			String alias = findAlias(progset, structType);

			int index = 0;
			for(TypePair pair: structType.getAllFields()) {
				ProgramSpec pspec = findField(progset, alias, pair.name);
				if (pspec == null) {
					//not being imported
				} else {
					OutputFieldHandle ofh = new OutputFieldHandle();
					ofh.structType = structType;
					ofh.fieldIndex = index;
					ofh.fieldName = pair.name;
					ofh.arMetrics = new int[OutputFieldHandle.NUM_METRICS];
					ispec.ofhList.add(ofh);
				}
				index++;
			}

			return ispec;
		}

		private String findAlias(ProgramSet progset, DStructType structType) {
			int i = progset.outputTypes.indexOf(structType);
			if (i >= 0) {
				return progset.outputAliases.get(i);
			}
			return null;
		}

		private ProgramSpec findField(ProgramSet progset, String alias, String outputFieldName) {
			for(String inputField: progset.fieldMap.keySet()) {
				ProgramSpec pspec = progset.fieldMap.get(inputField);
				if (pspec.outputField.val1.equals(alias) && pspec.outputField.val2.equals(outputFieldName)) {
					return pspec;
				}
			}
			return null;
		}

		public void addInputColumn(ImportSpec ispec, String columnName, int colIndex, String outputFieldName) {
			InputFieldHandle ifh = new InputFieldHandle();
			ifh.columnIndex = colIndex;
			ifh.columnName = columnName;
			ispec.ifhList.add(ifh);

			for(OutputFieldHandle ofh: ispec.ofhList) {
				if (ofh.fieldName.equals(outputFieldName)) {
					ofh.ifhIndex = ispec.ifhList.size() - 1;
				}
			}

		}
	}
	
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
		DStructType structType = progset.outputTypes.get(0);
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
