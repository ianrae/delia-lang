package org.delia.mem;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.base.DBTestHelper;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dataimport.CSVImportService;
import org.delia.dataimport.ImportGroupSpec;
import org.delia.dataimport.ImportToool;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.log.LogLevel;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.util.TextFileReader;
import org.junit.Before;
import org.junit.Test;

public class FilmAndActorTests  extends NewBDDBase {
	
	@Test
	public void testTool() {
		String srcPath = IMPORT_DIR + "film-and-actor.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		Delia delia = createDelia();
		DeliaSession session = delia.beginSession(deliaSrc);
		ImportToool tool = new ImportToool(session);

//		String csvPath = BASE_DIR + "film.csv";
//		String s = tool.generateInputFunctionSourceCode("Film", csvPath);
//		log.log(s);
		
		String csvPath = BASE_DIR + "actor.csv";
		String s = tool.generateInputFunctionSourceCode("Actor", csvPath);
		log.log(s);
	}
	
	@Test
	public void testLevel2() {
		List<ImportGroupSpec> groupList = new ArrayList<>();
		ImportGroupSpec gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "film.csv";
		gspec.inputFnName = "film";
		gspec.typeName = "Film";
		groupList.add(gspec);
		gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "actor.csv";
		gspec.inputFnName = "actor";
		gspec.typeName = "Actor";
		groupList.add(gspec);
		gspec = new ImportGroupSpec();
		gspec.csvPath = BASE_DIR + "actor-film.csv";
		gspec.inputFnName = "actorfilms";
		gspec.typeName = "Actor";
		groupList.add(gspec);
		
		String srcPath = IMPORT_DIR + "film-and-actor.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		CSVImportService.Options options = new CSVImportService.Options();
//		options.numRowsToImport = 3;
		options.logDetails = true;
		List<InputFunctionResult> resultL = csvSvc.dryRunLevel2(groupList, deliaSrc, options);
		csvSvc.dumpReports(resultL);
	}
	
	@Test
	public void testVia() {
		String csvPath = BASE_DIR + "actor-film.csv";
		
		String srcPath = IMPORT_DIR + "film-and-actor.txt";
		TextFileReader reader = new TextFileReader();
		String deliaSrc = reader.readFileAsSingleString(srcPath);
		
		CSVImportService csvSvc = new CSVImportService();
		
		CSVImportService.Options options = new CSVImportService.Options();
//		options.numRowsToImport = 3;
		options.logDetails = true;
//		options.logLevel = LogLevel.DEBUG;
		InputFunctionResult result = csvSvc.dryRunLevel1(csvPath, deliaSrc, "Actor", "actorfilms", options);
		csvSvc.dumpReport(result);
	}
	
	
//	private ExternalDataLoader createExternalLoader() {
//		Delia externalDelia = createDelia();
//		
//		String srcPath = IMPORT_DIR + "product-and-category.txt";
//		TextFileReader reader = new TextFileReader();
//		String deliaSrc = reader.readFileAsSingleString(srcPath);
//		
////		deliaSrc += "\n" + "insert Category {categoryID:992, categoryName: 'ext1', description:'ext-desc', picture:'p'}";
//		deliaSrc += "\n" + "upsert Category[992] {categoryName: 'ext1', description:'ext-desc', picture:'p'}";
//		DeliaSession externalSession = externalDelia.beginSession(deliaSrc);
//		
//		ExternalDataLoader externalLoader = new ExternalDataLoaderImpl(externalDelia.getFactoryService(), externalSession);
//		return externalLoader;
//	}
//	
//	@Test
//	public void testLevel4Preparation() {
//		Delia delia = createDelia();
//		
//		H2TestCleaner cleaner = new H2TestCleaner(DBType.H2);
//		cleaner.deleteKnownTables(delia.getFactoryService(), delia.getDBInterface());
//		cleaner.deleteTables(delia.getFactoryService(), delia.getDBInterface(), "Category,Product");
//	}
//	
//	@Test
//	public void testLevel4() {
//		List<ImportGroupSpec> groupList = new ArrayList<>();
//		ImportGroupSpec gspec = new ImportGroupSpec();
//		gspec.csvPath = BASE_DIR + "categories.csv";
//		gspec.inputFnName = "category";
//		gspec.typeName = "Category";
//		groupList.add(gspec);
//		gspec = new ImportGroupSpec();
//		gspec.csvPath = BASE_DIR + "products.csv";
//		gspec.inputFnName = "product";
//		gspec.typeName = "Product";
//		groupList.add(gspec);
//		
//		String srcPath = IMPORT_DIR + "product-and-category.txt";
//		TextFileReader reader = new TextFileReader();
//		String deliaSrc = reader.readFileAsSingleString(srcPath);
//		
//		CSVImportService csvSvc = new CSVImportService();
//		
//		//h2
//		Delia delia = createDelia();
//		
//		CSVImportService.Options options = new CSVImportService.Options();
////		options.numRowsToImport = 3;
////		options.logDetails = true;
//		options.useInsertStatement = true;
//		List<InputFunctionResult> resultL = csvSvc.importIntoDatabase(groupList, deliaSrc, delia, options);
//		csvSvc.dumpReports(resultL);
//	}
	
	private Delia createDelia() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return delia;
	}
	// --
	private final String BASE_DIR = "src/main/resources/test/film/";
	public final String IMPORT_DIR = "src/main/resources/test/import/";

	@Before
	public void init() {
		//uncomment this to run these tests
		DBTestHelper.throwIfNoSlowTests();
	}
	
	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
}
