package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.dataimport.CSVFileLoader;
import org.delia.dataimport.DataImportService;
import org.delia.dataimport.ExternalDataLoaderImpl;
import org.delia.dataimport.ImportGroupBuilder;
import org.delia.dataimport.ImportLevel;
import org.delia.dataimport.ImportTool;
import org.delia.log.LogLevel;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.SimpleImportMetricObserver;
import org.junit.Before;
import org.junit.Test;

public class ImportToolTests extends InputFunctionTestBase {
	
	@Test
	public void testTool1Category() {
		Delia delia = createNewDelia();
		String src = createCategorySrc(false);
		buildSrc(delia, src);
		
		ImportTool tool = new ImportTool(session);
		String path = BASE_DIR + "categories.csv";
		String s = tool.generateInputFunctionSourceCode("Category", path);
		log.log("here:");
		log.log(s);
		
		log.log("add to session..");
		ResultValue res = delia.continueExecution(s, session);
		assertEquals(true, res.ok);
		
		DataImportService importSvc = new DataImportService(session, 10);
		CSVFileLoader loader = new CSVFileLoader();
		loader.init(path);
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		InputFunctionResult result = importSvc.executeImport("category", loader, ImportLevel.ONE);
		importSvc.dumpImportReport(result, observer);
	}
	
	@Test
	public void testTool1ProductSource() {
		Delia delia = createNewDelia();
		String src = createCategorySrc(false);
		buildSrc(delia, src);
		
		ImportTool tool = new ImportTool(session);
		String path = BASE_DIR + "products.csv";
		String s = tool.generateDeliaStructSourceCode("Product", path, false);
		log.log("here:");
		log.log(s);
		log.log("");
	}
	
	@Test
	public void testTool1Product() {
		Delia delia = initDelia(); 
		
		ImportTool tool = new ImportTool(session);
		String path = BASE_DIR + "products.csv";
		String s = tool.generateInputFunctionSourceCode("Product", path);
		log.log("here:");
		log.log(s);
		
		log.log("add to session..");
		ResultValue res = delia.continueExecution(s, session);
		assertEquals(true, res.ok);
		
		DataImportService importSvc = new DataImportService(session, 10);
		CSVFileLoader loader = new CSVFileLoader();
		loader.init(path);
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		InputFunctionResult result = importSvc.executeImport("product", loader, ImportLevel.ONE);
		importSvc.dumpImportReport(result, observer);
	}
	
	private Delia initDelia() {
		Delia delia = createNewDelia();
		String src = createCategorySrc(false);
		src += " " + createProductSrc();
		buildSrc(delia, src);
		return delia;
	}

	@Test
	public void testLevel2() {
		Delia delia = initDelia(); 
		
		ImportTool tool = new ImportTool(session);
		String path = BASE_DIR + "products.csv";
		String prodSrc = tool.generateInputFunctionSourceCode("Product", path);
		log.log("here:");
		log.log(prodSrc);
		
		String path2 = BASE_DIR + "categories.csv";
		String catSrc = tool.generateInputFunctionSourceCode("Category", path2);
		log.log("here:");
		log.log(catSrc);
		
		String newSrc = prodSrc + " " + catSrc;
		
		log.log("add to session..");
		ResultValue res = delia.continueExecution(newSrc, session);
		assertEquals(true, res.ok);
		
		ImportGroupBuilder groupBuilder = new ImportGroupBuilder(delia.getFactoryService());
		CSVFileLoader loader1 = new CSVFileLoader();
		loader1.init(path2);
		CSVFileLoader loader2 = new CSVFileLoader();
		loader2.init(path);
		
		groupBuilder.addImport("category", loader1);
		groupBuilder.addImport("product", loader2);
		
		DataImportService importSvc = new DataImportService(session, 10);
		CSVFileLoader loader = new CSVFileLoader();
		loader.init(path);
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupBuilder.getGroupL(), ImportLevel.TWO);
		for(InputFunctionResult result: resultL) {
			importSvc.dumpImportReport(result, observer);
		}
	}
	
	@Test
	public void testLevel3() {
		Delia delia = initDelia(); 
		
		ImportTool tool = new ImportTool(session);
		String path = BASE_DIR + "products.csv";
		String prodSrc = tool.generateInputFunctionSourceCode("Product", path);
		log.log("here:");
		log.log(prodSrc);
		
		String path2 = BASE_DIR + "categories.csv";
		String catSrc = tool.generateInputFunctionSourceCode("Category", path2);
		log.log("here:");
		log.log(catSrc);
		
		String newSrc = prodSrc + " " + catSrc;
		
		log.log("add to session..");
		ResultValue res = delia.continueExecution(newSrc, session);
		assertEquals(true, res.ok);
		
		ImportGroupBuilder groupBuilder = new ImportGroupBuilder(delia.getFactoryService());
		CSVFileLoader loader1 = new CSVFileLoader();
		loader1.init(path2);
		CSVFileLoader loader2 = new CSVFileLoader();
		loader2.init(path);
		groupBuilder.addImport("category", loader1);
		groupBuilder.addImport("product", loader2);
		
		DataImportService importSvc = new DataImportService(session, 10);
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		
		ExternalDataLoader externalLoader = createExternalLoader();
		importSvc.setExternalDataLoader(externalLoader);
		List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupBuilder.getGroupL(), ImportLevel.THREE);
		for(InputFunctionResult result: resultL) {
			importSvc.dumpImportReport(result, observer);
		}
	}

	private ExternalDataLoader createExternalLoader() {
		Delia externalDelia = createNewDelia();
		
		String src = createCategorySrc(false);
		src += " " + createProductSrc();
		src += "\n" + "insert Category {categoryID:992, categoryName: 'ext1', description:'ext-desc', picture:'p'}";
		DeliaSession externalSession = externalDelia.beginSession(src);
		
		ExternalDataLoader externalLoader = new ExternalDataLoaderImpl(externalDelia.getFactoryService(), externalSession);
		return externalLoader;
	}
	
	
	@Test
	public void testLevel4() {
		Delia delia = initDelia(); 
		
		ImportTool tool = new ImportTool(session);
		String path = BASE_DIR + "products.csv";
		String prodSrc = tool.generateInputFunctionSourceCode("Product", path);
		log.log("here:");
		log.log(prodSrc);
		
		String path2 = BASE_DIR + "categories.csv";
		String catSrc = tool.generateInputFunctionSourceCode("Category", path2);
		log.log("here:");
		log.log(catSrc);
		
		String newSrc = prodSrc + " " + catSrc;
		
		log.log("add to session..");
		ResultValue res = delia.continueExecution(newSrc, session);
		assertEquals(true, res.ok);
		
		ImportGroupBuilder groupBuilder = new ImportGroupBuilder(delia.getFactoryService());
		CSVFileLoader loader1 = new CSVFileLoader();
		loader1.init(path2);
		CSVFileLoader loader2 = new CSVFileLoader();
		loader2.init(path);
		groupBuilder.addImport("category", loader1);
		groupBuilder.addImport("product", loader2);
		
		DataImportService importSvc = new DataImportService(session, 10);
		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		
		List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupBuilder.getGroupL(), ImportLevel.FOUR);
		for(InputFunctionResult result: resultL) {
			importSvc.dumpImportReport(result, observer);
		}
	}

	// --

	@Before
	public void init() {
	}
	private void buildSrc(Delia delia, String src) {
//		String src = createCustomerSrc(which);
		delia.getLog().setLevel(LogLevel.DEBUG);
		delia.getLog().log(src);
		this.session = delia.beginSession(src);
	}
	private String createCategorySrc(boolean inOrder) {
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
	
	String createProductSrc() {
		String src = "type Product struct {    productID int primaryKey,    productName string,    supplierID int, relation categoryID Category optional one,    quantityPerUnit string,    unitPrice string,   ";
		src += "unitsInStock int,    unitsOnOrder int,    reorderLevel int,    discontinued int} end";
		return src;
	}
	
}
