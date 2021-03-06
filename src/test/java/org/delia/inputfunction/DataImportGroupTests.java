package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaGenericDao;
import org.delia.dataimport.CSVFileLoader;
import org.delia.dataimport.DataImportService;
import org.delia.dataimport.ImportLevel;
import org.delia.error.DeliaError;
import org.delia.log.LogLevel;
import org.delia.runner.inputfunction.GroupPair;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.LineObjIterator;
import org.junit.Before;
import org.junit.Test;

public class DataImportGroupTests extends InputFunctionTestBase {
	
	public static class ImportGroupService extends ServiceBase {
		
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
		
		List<InputFunctionResult> run(Delia delia, DeliaSession session, int stopAfterErrorThreshold) {
			List<InputFunctionResult> resultL = new ArrayList<>();
			
			for(GroupPair pair: groupL) {
				DataImportService importSvc = new DataImportService(session, stopAfterErrorThreshold);

				InputFunctionResult result = importSvc.executeImport(pair.inputFnName, pair.iter, ImportLevel.ONE);
				resultL.add(result);
			}
			
			return resultL;
		}
		
	}

	@Test
	public void testCategories() {
		String path = BASE_DIR + "categories.csv";
		CSVFileLoader fileLoader = new CSVFileLoader();
		fileLoader.init(path);
		numExpectedColumnsProcessed = 4;
		buildAndRun(1, "foo", fileLoader, 8);
	}
	@Test
	public void testProducts() {
		String path = BASE_DIR + "products.csv";
		CSVFileLoader fileLoader = new CSVFileLoader();
		fileLoader.init(path);
		log.log(path);
		numExpectedColumnsProcessed = 4;
		stopAfterErrorThreshold = 10;
		buildAndRun(2, "prod1", fileLoader, 77);
	}
	
	
	// --
	private int stopAfterErrorThreshold;

	@Before
	public void init() {
		DeliaGenericDao dao = this.createDao();
		this.delia = dao.getDelia();
	}

	private void createDelia(int which) {
		String src;
		if (which == 1) {
			src = buildSrc(true);
		} else {
			src = buildSrcProduct();
		}
		delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		delia.getOptions().logSourceBeforeCompile = true;
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
		String src = buildSrc(true);
		src += "\n type Product struct { productID int unique, productName string, relation category Category one optional, quantityPerUnit string, ";
		src += "\n unitPrice string optional, unitsInStock int optional, unitsOnOrder int optional, reorderLevel int optional, discontinued int  optional} end";
		src += "\n  input function prod1(Product p) { ";
		src += "\n  productID -> p.productID, productName -> p.productName, categoryID -> p.category, quantityPerUnit -> p.quantityPerUnit";
		src += " }";

		return src;
	}
	private InputFunctionResult buildAndRun(int which, String inFnName, LineObjIterator lineObjIter, int expectedRows) {
		createDelia(which);
		ImportGroupService groupSvc = new ImportGroupService(delia.getFactoryService());
		groupSvc.addImport(inFnName, lineObjIter);
		
		List<InputFunctionResult> resultL = groupSvc.run(delia, session, stopAfterErrorThreshold);
		assertEquals(1, resultL.size());
		InputFunctionResult result = resultL.get(0);
		dumpErrors(result.errors);
		assertEquals(0, result.errors.size());
		assertEquals(expectedRows, result.numRowsProcessed);
		assertEquals(expectedRows, result.numRowsInserted);
		return result;
	}
	private void dumpErrors(List<DeliaError> errors) {
		log.log("%d errors", errors.size());
		for(DeliaError err: errors) {
			log.log(" %s", err.toString());
		}
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

}
