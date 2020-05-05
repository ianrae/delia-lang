package org.delia.h2;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.app.NorthwindHelper;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dataimport.CSVImportService;
import org.delia.dataimport.ExternalDataLoaderImpl;
import org.delia.dataimport.ImportGroupSpec;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.util.TextFileReader;
import org.junit.Before;
import org.junit.Test;

public class H2ImportTests  extends NewBDDBase {
	
	
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
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.H2).connectionString(H2ConnectionHelper.getTestDB()).build();
		Delia externalDelia = DeliaBuilder.withConnection(info).build();
		
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
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.H2).connectionString(H2ConnectionHelper.getTestDB()).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		
		List<InputFunctionResult> resultL = csvSvc.importIntoDatabase(groupList, deliaSrc, delia);
		csvSvc.dumpReports(resultL);
	}

	// --
	private final String BASE_DIR = NorthwindHelper.BASE_DIR;
	public final String IMPORT_DIR = "src/main/resources/test/import/";

	@Before
	public void init() {
	}
	
	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
}
