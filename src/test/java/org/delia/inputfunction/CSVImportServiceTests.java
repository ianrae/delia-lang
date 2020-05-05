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
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dataimport.CSVFileLoader;
import org.delia.dataimport.DataImportService;
import org.delia.dataimport.ExternalDataLoaderImpl;
import org.delia.dataimport.ImportGroupBuilder;
import org.delia.dataimport.ImportLevel;
import org.delia.dataimport.ImportToool;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.log.LogLevel;
import org.delia.runner.ResultValue;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.GroupPair;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.SimpleImportMetricObserver;
import org.delia.util.TextFileReader;
import org.junit.Before;
import org.junit.Test;

public class CSVImportServiceTests  extends NewBDDBase {
	
	public static class ImportGroupSpec {
		public String csvPath;
		public String typeName;
		public String inputFnName;
	}
	
	public static class CSVImportService  {

		private DeliaSession session;
		private SimpleImportMetricObserver observer;
		private DataImportService importSvc;

		public CSVImportService() {
		}
		
		public InputFunctionResult importLevel1(String csvPath, String deliaSrc, String typeName, String inputFunctionName) {
			ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
			Delia delia = DeliaBuilder.withConnection(info).build();
			this.session = delia.beginSession(deliaSrc);
			
			importSvc = new DataImportService(session, 10);
			CSVFileLoader loader = new CSVFileLoader(csvPath);
			this.observer = new SimpleImportMetricObserver();
			importSvc.setMetricsObserver(observer);
			InputFunctionResult result = importSvc.executeImport(inputFunctionName, loader, ImportLevel.ONE);
			return result;
		}
		
		public List<InputFunctionResult> importLevel2(List<ImportGroupSpec> groupList, String deliaSrc) {
			ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
			Delia delia = DeliaBuilder.withConnection(info).build();
			this.session = delia.beginSession(deliaSrc);
			
			importSvc = new DataImportService(session, 10);
			List<GroupPair> groupL = new ArrayList<>();
			for(ImportGroupSpec spec: groupList) {
				CSVFileLoader loader = new CSVFileLoader(spec.csvPath);
				GroupPair pair = new GroupPair();
				pair.inputFnName = spec.inputFnName;
				pair.iter = loader;
				groupL.add(pair);
			}
			this.observer = new SimpleImportMetricObserver();
			importSvc.setMetricsObserver(observer);
			List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupL, ImportLevel.TWO);
			return resultL;
		}
		
		public void dumpReport(InputFunctionResult result) {
			importSvc.dumpImportReport(result, observer);
		}
		public void dumpReports(List<InputFunctionResult> resultL) {
			for(InputFunctionResult result: resultL) {
				importSvc.dumpImportReport(result, observer);
			}
		}
		
	}
	
	@Test
	public void testLevel1Category() {
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		String csvPath = BASE_DIR + "categories.csv";
		InputFunctionResult result = csvSvc.importLevel1(csvPath, deliaSrc, "Category", "category");
		csvSvc.dumpReport(result);
	}
	
	@Test
	public void testLevel1Product() {
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		String csvPath = BASE_DIR + "products.csv";
		InputFunctionResult result = csvSvc.importLevel1(csvPath, deliaSrc, "Product", "product");
		csvSvc.dumpReport(result);
	}
	
	@Test
	public void testLevel2() {
		String path = BASE_DIR + "products.csv";
		String path2 = BASE_DIR + "categories.csv";
		
		List<ImportGroupSpec> groupList = new ArrayList<>();
		ImportGroupSpec gspec = new ImportGroupSpec();
		gspec.csvPath = path;
		gspec.inputFnName = "category";
		gspec.typeName = "Category";
		groupList.add(gspec);
		gspec = new ImportGroupSpec();
		gspec.csvPath = path2;
		gspec.inputFnName = "product";
		gspec.typeName = "Product";
		groupList.add(gspec);
		
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		List<InputFunctionResult> resultL = csvSvc.importLevel2(groupList, deliaSrc);
		csvSvc.dumpReports(resultL);
	}
	
//	@Test
//	public void testLevel3() {
//		Delia delia = initDelia(); 
//		
//		ImportToool tool = new ImportToool(session);
//		String path = BASE_DIR + "products.csv";
//		String prodSrc = tool.generateInputFunctionSourceCode("Product", path);
//		log.log("here:");
//		log.log(prodSrc);
//		
//		String path2 = BASE_DIR + "categories.csv";
//		String catSrc = tool.generateInputFunctionSourceCode("Category", path2);
//		log.log("here:");
//		log.log(catSrc);
//		
//		String newSrc = prodSrc + " " + catSrc;
//		
//		log.log("add to session..");
//		ResultValue res = delia.continueExecution(newSrc, session);
//		assertEquals(true, res.ok);
//		
//		ImportGroupBuilder groupBuilder = new ImportGroupBuilder(delia.getFactoryService());
//		groupBuilder.addImport("category", new CSVFileLoader(path2));
//		groupBuilder.addImport("product", new CSVFileLoader(path));
//		
//		DataImportService importSvc = new DataImportService(session, 10);
//		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
//		importSvc.setMetricsObserver(observer);
//		
//		ExternalDataLoader externalLoader = createExternalLoader();
//		importSvc.setExternalDataLoader(externalLoader);
//		List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupBuilder.getGroupL(), ImportLevel.THREE);
//		for(InputFunctionResult result: resultL) {
//			importSvc.dumpImportReport(result, observer);
//		}
//	}
//
//	private ExternalDataLoader createExternalLoader() {
//		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
//		Delia externalDelia = DeliaBuilder.withConnection(info).build();
//		
//		String src = createCategorySrc(false);
//		src += " " + createProductSrc();
//		src += "\n" + "insert Category {categoryID:992, categoryName: 'ext1', description:'ext-desc', picture:'p'}";
//		DeliaSession externalSession = externalDelia.beginSession(src);
//		
//		ExternalDataLoader externalLoader = new ExternalDataLoaderImpl(externalDelia.getFactoryService(), externalSession);
//		return externalLoader;
//	}
//	
//	
//	@Test
//	public void testLevel4() {
//		Delia delia = initDelia(); 
//		
//		ImportToool tool = new ImportToool(session);
//		String path = BASE_DIR + "products.csv";
//		String prodSrc = tool.generateInputFunctionSourceCode("Product", path);
//		log.log("here:");
//		log.log(prodSrc);
//		
//		String path2 = BASE_DIR + "categories.csv";
//		String catSrc = tool.generateInputFunctionSourceCode("Category", path2);
//		log.log("here:");
//		log.log(catSrc);
//		
//		String newSrc = prodSrc + " " + catSrc;
//		
//		log.log("add to session..");
//		ResultValue res = delia.continueExecution(newSrc, session);
//		assertEquals(true, res.ok);
//		
//		ImportGroupBuilder groupBuilder = new ImportGroupBuilder(delia.getFactoryService());
//		groupBuilder.addImport("category", new CSVFileLoader(path2));
//		groupBuilder.addImport("product", new CSVFileLoader(path));
//		
//		DataImportService importSvc = new DataImportService(session, 10);
//		SimpleImportMetricObserver observer = new SimpleImportMetricObserver();
//		importSvc.setMetricsObserver(observer);
//		
//		List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupBuilder.getGroupL(), ImportLevel.FOUR);
//		for(InputFunctionResult result: resultL) {
//			importSvc.dumpImportReport(result, observer);
//		}
//	}

	// --
	private final String BASE_DIR = NorthwindHelper.BASE_DIR;
	public final String IMPORT_DIR = "src/main/resources/test/import/";

	private DeliaSession session;


	@Before
	public void init() {
	}
	private void buildSrc(Delia delia, String src) {
//		String src = createCustomerSrc(which);
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
	
	
	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	private LineObj createLineObj(int id, String nameStr) {
		String[] ar = { "", "33", "bob" };
		ar[0] = String.format("%d", id);
		ar[2] = nameStr;

		LineObj lineObj = new LineObj(ar, 1);
		return lineObj;
	}
}
