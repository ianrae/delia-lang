package org.delia.postgres;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.app.NorthwindHelper;
import org.delia.base.DBTestHelper;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dataimport.CSVImportService;
import org.delia.dataimport.ExternalDataLoaderImpl;
import org.delia.dataimport.ImportGroupSpec;
import org.delia.db.DBType;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.util.TextFileReader;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class PostgresImportTests  extends NewBDDBase {
	
	
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
		CSVImportService.Options options = new CSVImportService.Options();
//		options.numRowsToImport = 3;
		options.logDetails = true;
		List<InputFunctionResult> resultL = csvSvc.dryRunLevel3(groupList, deliaSrc, externalLoader, options);
		csvSvc.dumpReports(resultL);
	}
	
	private ExternalDataLoader createExternalLoader() {
		Delia externalDelia = createDelia();
		
		String srcPath = IMPORT_DIR + "product-and-category.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
//		deliaSrc += "\n" + "insert Category {categoryID:992, categoryName: 'ext1', description:'ext-desc', picture:'p'}";
		deliaSrc += "\n" + "upsert Category[992] {categoryName: 'ext1', description:'ext-desc', picture:'p'}";
		DeliaSession externalSession = externalDelia.beginSession(deliaSrc);
		
		ExternalDataLoader externalLoader = new ExternalDataLoaderImpl(externalDelia.getFactoryService(), externalSession);
		return externalLoader;
	}
	
	@Test
	public void testLevel4Preparation() {
		Delia delia = createDelia();
		
		H2TestCleaner cleaner = new H2TestCleaner(DBType.POSTGRES);
		cleaner.deleteKnownTables(delia.getFactoryService(), delia.getDBInterface());
		cleaner.deleteTables(delia.getFactoryService(), delia.getDBInterface(), "Category,Product");
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
		
		//h2
		Delia delia = createDelia();
		
		List<InputFunctionResult> resultL = csvSvc.importIntoDatabase(groupList, deliaSrc, delia);
		csvSvc.dumpReports(resultL);
	}
	
	
	private Delia createDelia() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.POSTGRES).connectionString(PostgresConnectionHelper.getTestDB()).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return delia;
	}

	// --
	private final String BASE_DIR = NorthwindHelper.BASE_DIR;
	public final String IMPORT_DIR = "src/main/resources/test/import/";

	@Before
	public void init() {
		//uncomment this to run these tests
		DBTestHelper.throwIfNoSlowTests();
	}
	
	@Override
	public ZDBInterfaceFactory createForTest() {
		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
		return db;
	}
}
