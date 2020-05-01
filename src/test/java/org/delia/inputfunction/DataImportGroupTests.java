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
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.dataimport.CSVFileLoader;
import org.delia.dataimport.DataImportService;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.log.LogLevel;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.LineObjIterator;
import org.junit.Before;
import org.junit.Test;

public class DataImportGroupTests  extends NewBDDBase {
	
	public static class ImportGroupService extends ServiceBase {
		
		public static class GroupPair {
			public String inputFnName;
			public LineObjIterator iter;
		}
		
		private List<GroupPair> groupL = new ArrayList<>();

		public ImportGroupService(FactoryService factorySvc) {
			super(factorySvc);
		}
		
		public void addImport(String inputFunctionName, LineObjIterator iter) {
			GroupPair pair = new GroupPair();
			pair.inputFnName = inputFunctionName;
			pair.iter = iter;
			this.groupL.add(pair);
		}
		
		List<InputFunctionResult> run(Delia delia, DeliaSession session) {
			List<InputFunctionResult> resultL = new ArrayList<>();
			
			for(GroupPair pair: groupL) {
				DataImportService importSvc = new DataImportService(delia, session);

				InputFunctionResult result = importSvc.importIntoDatabase(pair.inputFnName, pair.iter);
				resultL.add(result);
			}
			
			return resultL;
		}
		
	}

	@Test
	public void testCategories() {
		String path = BASE_DIR + "categories.csv";
		CSVFileLoader fileLoader = new CSVFileLoader(path);
		numExpectedColumnsProcessed = 4;
		buildAndRun(1, fileLoader, 8);
	}
	@Test
	public void testProducts() {
		String path = BASE_DIR + "products.csv";
		CSVFileLoader fileLoader = new CSVFileLoader(path);
		numExpectedColumnsProcessed = 4;
		buildAndRun(2, fileLoader, 8);
	}
	
	
	// --
	//	private DeliaDao dao;
	private final String BASE_DIR = NorthwindHelper.BASE_DIR;
	
	private Delia delia;
	private DeliaSession session;
	private int numExpectedColumnsProcessed;

	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
	}

	private void createDelia(int which) {
		String src;
		if (which == 1) {
			src = buildSrc(true);
		} else {
			src = buildSrcProduct();
		}
		this.delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
	}
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
	private String buildSrcProduct() {
		String src = " type Product struct { productID int unique, productName string, category Category, quantityPerUnit string, ";
		src += "\n unitPrice string, unitsInStock int, unitsOnOrder int, reorderLevel int, discontinued int } end";
		src += "\n  input function supplier1(Product p) { ";
		src += "\n  productID -> p.productID, productName -> p.productName, categoryID -> p.category, quantityPerUnit -> p.quantityPerUnit";
		src += " }";

		return src;
	}
	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}
	private InputFunctionResult buildAndRun(int which, LineObjIterator lineObjIter, int expectedRows) {
		createDelia(which);
		ImportGroupService groupSvc = new ImportGroupService(delia.getFactoryService());
		groupSvc.addImport("foo", lineObjIter);
		
		List<InputFunctionResult> resultL = groupSvc.run(delia, session);
		assertEquals(1, resultL.size());
		InputFunctionResult result = resultL.get(0);
		assertEquals(0, result.errors.size());
		assertEquals(expectedRows, result.numRowsProcessed);
		assertEquals(expectedRows, result.numRowsInserted);
		return result;
	}

	
//	private String buildSrc(boolean inOrder) {
//		if (inOrder) {
//			String src = String.format(" type Category struct { categoryID int primaryKey, categoryName string, description string, picture string} end");
//			//categoryID,categoryName,description,picture
//			src += String.format(" \ninput function foo(Category c) { categoryID -> c.categoryID, categoryName -> c.categoryName, description -> c.description, picture -> c.picture } ");
//			src += String.format(" \nlet var1 = 55");
//
//			return src;
//		} else {
//			String src = String.format(" type Category struct { categoryID int primaryKey, categoryName string, description string, picture string} end");
//			//categoryID,categoryName,description,picture
//			src += String.format(" \ninput function foo(Category c) { categoryName -> c.categoryName, description -> c.description, picture -> c.picture, categoryID -> c.categoryID } ");
//			src += String.format(" \nlet var1 = 55");
//
//			return src;
//		}
//	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

}
