package org.delia.inputfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.app.NorthwindHelper;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dataimport.CSVFileLoader;
import org.delia.dataimport.DataImportService;
import org.delia.dataimport.ExternalDataLoaderImpl;
import org.delia.dataimport.ImportLevel;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.GroupPair;
import org.delia.runner.inputfunction.InputFunctionResult;
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

		public List<InputFunctionResult> importLevel3(List<ImportGroupSpec> groupList, String deliaSrc,
				ExternalDataLoader externalLoader) {
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
			importSvc.setExternalDataLoader(externalLoader);
			List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupL, ImportLevel.THREE);
			return resultL;
		}

		public List<InputFunctionResult> importLevel4(List<ImportGroupSpec> groupList, String deliaSrc, Delia delia) {
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
			List<InputFunctionResult> resultL = importSvc.executeImportGroup(groupL, ImportLevel.FOUR);
			return resultL;
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
		
		List<InputFunctionResult> resultL = csvSvc.importLevel2(groupList, deliaSrc);
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
		List<InputFunctionResult> resultL = csvSvc.importLevel3(groupList, deliaSrc, externalLoader);
		csvSvc.dumpReports(resultL);
	}
	
	private ExternalDataLoader createExternalLoader() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia externalDelia = DeliaBuilder.withConnection(info).build();
		
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
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		
		List<InputFunctionResult> resultL = csvSvc.importLevel4(groupList, deliaSrc, delia);
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
