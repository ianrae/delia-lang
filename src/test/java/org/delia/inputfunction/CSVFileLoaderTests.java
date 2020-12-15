package org.delia.inputfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.dataimport.CSVFileLoader;
import org.delia.db.DBType;
import org.delia.log.LogLevel;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.LineObjIteratorImpl;
import org.junit.Before;
import org.junit.Test;

public class CSVFileLoaderTests extends InputFunctionTestBase {

	@Test
	public void test1() {
		String path = BASE_DIR + "categories.csv";
		CSVFileLoader fileLoader = new CSVFileLoader();
		fileLoader.init(path);
		numExpectedColumnsProcessed = 4;
		buildAndRun(true, fileLoader, 8);
	}

	@Test
	public void testOutOfOrder() {
		String path = BASE_DIR + "categories.csv";
		CSVFileLoader fileLoader = new CSVFileLoader();
		fileLoader.init(path);
		numExpectedColumnsProcessed = 4;
		buildAndRun(false, fileLoader, 8);
	}
	
	// --

	@Before
	public void init() {
		DeliaGenericDao dao = this.createDao();
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
	private DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaGenericDao(delia);
	}
	private InputFunctionResult buildAndRun(boolean inOrder, LineObjIterator lineObjIter, int expectedNumRows) {
		createDelia(inOrder);
		return buildAndRun(lineObjIter, expectedNumRows);
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
