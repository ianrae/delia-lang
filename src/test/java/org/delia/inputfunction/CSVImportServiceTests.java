package org.delia.inputfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dataimport.CSVImportService;
import org.delia.dataimport.ExternalDataLoaderImpl;
import org.delia.dataimport.ImportGroupSpec;
import org.delia.db.DBType;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.util.TextFileReader;
import org.junit.Before;
import org.junit.Test;

public class CSVImportServiceTests extends InputFunctionTestBase {
	
	@Test
	public void testLevel1Category() {
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		String csvPath = BASE_DIR + "categories.csv";
		InputFunctionResult result = csvSvc.dryRunLevel1(csvPath, deliaSrc, "Category", "category");
		csvSvc.dumpReport(result);
	}
	
	@Test
	public void testLevel1Product() {
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		String csvPath = BASE_DIR + "products.csv";
		InputFunctionResult result = csvSvc.dryRunLevel1(csvPath, deliaSrc, "Product", "product");
		csvSvc.dumpReport(result);
	}
	
	@Test
	public void testLevel2() {
		List<ImportGroupSpec> groupList = new ArrayList<>();
		ImportGroupSpec gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "categories.csv";
		gspec.inputFnName = "category";
		gspec.typeName = "Category";
		groupList.add(gspec);
		gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "products.csv";
		gspec.inputFnName = "product";
		gspec.typeName = "Product";
		groupList.add(gspec);
		
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		List<InputFunctionResult> resultL = csvSvc.dryRunLevel2(groupList, deliaSrc);
		csvSvc.dumpReports(resultL);
	}
	
	@Test
	public void testLevel3() {
		List<ImportGroupSpec> groupList = new ArrayList<>();
		ImportGroupSpec gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "categories.csv";
		gspec.inputFnName = "category";
		gspec.typeName = "Category";
		groupList.add(gspec);
		gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "products.csv";
		gspec.inputFnName = "product";
		gspec.typeName = "Product";
		groupList.add(gspec);
		
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		ExternalDataLoader externalLoader = createExternalLoader();
		List<InputFunctionResult> resultL = csvSvc.dryRunLevel3(groupList, deliaSrc, externalLoader);
		csvSvc.dumpReports(resultL);
	}
	
	private ExternalDataLoader createExternalLoader() {
		Delia externalDelia = createNewDelia();
		
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		deliaSrc += "\n" + "insert Category {categoryID:992, categoryName: 'ext1', description:'ext-desc', picture:'p'}";
		DeliaSession externalSession = externalDelia.beginSession(deliaSrc);
		
		ExternalDataLoader externalLoader = new ExternalDataLoaderImpl(externalDelia.getFactoryService(), externalSession);
		return externalLoader;
	}
	
	@Test
	public void testLevel4() {
		List<ImportGroupSpec> groupList = new ArrayList<>();
		ImportGroupSpec gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "categories.csv";
		gspec.inputFnName = "category";
		gspec.typeName = "Category";
		groupList.add(gspec);
		gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "products.csv";
		gspec.inputFnName = "product";
		gspec.typeName = "Product";
		groupList.add(gspec);
		
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		//mem in this test but would normally be a real database
		Delia delia = createNewDelia();
		
		List<InputFunctionResult> resultL = csvSvc.importIntoDatabase(groupList, deliaSrc, delia);
		csvSvc.dumpReports(resultL);
	}

	// --
	public final String IMPORT_DIR = "src/test/resources/test/import/";

	@Before
	public void init() {
	}
}
