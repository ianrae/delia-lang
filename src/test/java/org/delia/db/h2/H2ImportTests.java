package org.delia.db.h2;

import java.util.ArrayList;
import java.util.List;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.app.NorthwindHelper;
import org.delia.base.DBTestHelper;
import org.delia.bdd.BDDBase;
import org.delia.builder.DeliaBuilder;
import org.delia.dataimport.CSVImportService;
import org.delia.dataimport.ExternalDataLoaderImpl;
import org.delia.dataimport.ImportGroupSpec;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.util.TextFileReader;
import org.delia.zdb.CollectingObserverFactory;
import org.delia.zdb.DBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class H2ImportTests  extends BDDBase {
	
	
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
		dumpObserver();
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
		
		H2TestCleaner cleaner = new H2TestCleaner(DBType.H2);
		cleaner.deleteKnownTables(delia.getFactoryService(), delia.getDBInterface());
		cleaner.deleteTables(delia.getFactoryService(), delia.getDBInterface(), "Category,Product");
	}
	
//	@Test
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
		
		CSVImportService.Options options = new CSVImportService.Options();
//		options.numRowsToImport = 3;
//		options.logDetails = true;
		options.useInsertStatement = true;
		List<InputFunctionResult> resultL = csvSvc.importIntoDatabase(groupList, deliaSrc, delia, options);
		csvSvc.dumpReports(resultL);
	}
	
	private Delia createDelia() {
		ConnectionDefinition connStr = H2ConnectionHelper.getTestDB();
		Delia delia = DeliaBuilder.withConnection(connStr).build();
		
//		ConnectionInfo info = ConnectionBuilder.dbType(DBType.H2).connectionString(H2ConnectionHelper.getTestDB();
//		Delia delia = DeliaBuilder.withConnection(info).build();
		this.observerFactory = new CollectingObserverFactory();
		delia.getOptions().dbObserverFactory = observerFactory;
		delia.getOptions().observeHLDSQLOnly = false;

		H2TestCleaner cleaner = new H2TestCleaner(DBType.H2);
		cleaner.deleteKnownTables(delia.getFactoryService(), delia.getDBInterface());
		cleaner.deleteContraintsForTable("CATEGORY");
		return delia;
	}
	// --
	private final String BASE_DIR = NorthwindHelper.BASE_DIR;
	public final String IMPORT_DIR = "src/test/resources/test/import/";
	private CollectingObserverFactory observerFactory;

	@Before
	public void init() {
		//uncomment this to run these tests
		DBTestHelper.throwIfNoSlowTests();
	}
	
	@Override
	public DBInterfaceFactory createForTest() {
		DBInterfaceFactory db = DBTestHelper.createMEMDb(createFactorySvc());
		return db;
	}
	
	private void dumpObserver() {
		for(SqlStatement stm: observerFactory.getObserver().getStatementList()) {
			log.log("stm: " + stm.sql);
		}
	}

	
}
