package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.app.NorthwindHelper;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.dataimport.CSVFileLoader;
import org.delia.dataimport.DataImportService;
import org.delia.dataimport.ImportLevel;
import org.delia.db.DBType;
import org.delia.log.LogLevel;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.LineObjIteratorImpl;
import org.delia.type.DValue;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class CSVFileLoaderTests  extends NewBDDBase {

	@Test
	public void test1() {
		String path = BASE_DIR + "categories.csv";
		CSVFileLoader fileLoader = new CSVFileLoader(path);
		numExpectedColumnsProcessed = 4;
		buildAndRun(true, fileLoader, 8);
	}

	@Test
	public void testOutOfOrder() {
		String path = BASE_DIR + "categories.csv";
		CSVFileLoader fileLoader = new CSVFileLoader(path);
		numExpectedColumnsProcessed = 4;
		buildAndRun(false, fileLoader, 8);
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
		String src = buildSrc(inOrder);
		this.delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
	}
//	private String buildSrc(String tlang) {
//		String src = String.format(" type Customer struct {id int primaryKey, wid int, name string } end");
//		src += String.format(" input function foo(Customer c) { ID -> c.id, WID -> c.wid, NAME -> c.name using { %s }}", tlang);
//		src += String.format(" let var1 = 55");
//
//		return src;
//	}
	private String buildSrc(boolean inOrder) {
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
		DataImportService importSvc = new DataImportService(session, 0);

		InputFunctionResult result = importSvc.executeImport("foo", lineObjIter, ImportLevel.ONE);
		assertEquals(0, result.errors.size());
		assertEquals(expectedNumRows, result.numRowsProcessed);
		assertEquals(expectedNumRows, result.numRowsInserted);
		assertEquals(numExpectedColumnsProcessed, result.numColumnsProcessedPerRow);
		return result;
	}
	private InputFunctionResult buildAndRunFail(LineObjIterator lineObjIter) {
		DataImportService importSvc = new DataImportService(session, 0);

		InputFunctionResult result = importSvc.executeImport("foo", lineObjIter, ImportLevel.ONE);
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
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
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
