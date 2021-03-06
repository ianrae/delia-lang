package org.delia.dataimport;

import java.util.ArrayList;
import java.util.List;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.base.DBTestHelper;
import org.delia.log.LogLevel;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.GroupPair;
import org.delia.runner.inputfunction.ImportedValueListener;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.SimpleImportMetricObserver;
import org.delia.zdb.mem.MemDBInterfaceFactory;


/**
 * Single use class
 * @author ian
 *
 */
public class CSVImportService  {
	
	public static class Options {
		public int numRowsToImport = Integer.MAX_VALUE;
		public boolean logDetails = false;
		public boolean useInsertStatement;
		public LogLevel logLevel;
		public ImportedValueListener importedValueListener;
		public InputFileLoaderFactory inputFileLoaderFactory = new CSVInputFileLoaderFactory();
	}

	private DeliaSession session;
	private SimpleImportMetricObserver observer;
	private DataImportService importSvc;
	private Options options;
	
	public CSVImportService() {
		this.options = new Options();
	}

	public InputFunctionResult dryRunLevel1(String csvPath, String deliaSrc, String typeName, String inputFunctionName) {
		return dryRunLevel1(csvPath, deliaSrc, typeName, inputFunctionName, options);
	}
	public List<InputFunctionResult> dryRunLevel2(List<ImportGroupSpec> groupList, String deliaSrc) {
		return this.dryRunLevel2(groupList, deliaSrc, options);
	}
	public List<InputFunctionResult> dryRunLevel3(List<ImportGroupSpec> groupList, String deliaSrc,
			ExternalDataLoader externalLoader) {
		return this.dryRunLevel3(groupList, deliaSrc, externalLoader, options);
	}
	public List<InputFunctionResult> importIntoDatabase(List<ImportGroupSpec> groupList, String deliaSrc, Delia delia) {
		return this.importIntoDatabase(groupList, deliaSrc, delia, options);
	}

	public InputFunctionResult dryRunLevel1(String csvPath, String deliaSrc, String typeName, 
				String inputFunctionName, Options options) {
		Delia delia = DBTestHelper.createNewDelia();
		if (options.logLevel != null) {
			delia.getLog().setLevel(options.logLevel);
		}
		MemDBInterfaceFactory memdb = (MemDBInterfaceFactory) delia.getDBInterface();
		memdb.createSingleMemDB();
		this.session = delia.beginSession(deliaSrc);
		
		importSvc = createDataImportService(options); 
		InputFileLoader loader = options.inputFileLoaderFactory.create();
		loader.init(csvPath);
		this.observer = new SimpleImportMetricObserver();
		importSvc.setMetricsObserver(observer);
		InputFunctionResult result = importSvc.executeImport(inputFunctionName, loader, ImportLevel.ONE);
		return result;
	}
	
	private DataImportService createDataImportService(Options options) {
		importSvc = new DataImportService(session, options.numRowsToImport, 10, options.logDetails);
		importSvc.setUseInsertStatement(options.useInsertStatement);
		importSvc.setImportedValueListener(options.importedValueListener);
		return importSvc;
	}

	public List<InputFunctionResult> dryRunLevel2(List<ImportGroupSpec> groupList, String deliaSrc, Options options) {
		Delia delia = DBTestHelper.createNewDelia();
		this.session = delia.beginSession(deliaSrc);
		
		importSvc = createDataImportService(options); 
		List<GroupPair> groupL = new ArrayList<>();
		for(ImportGroupSpec spec: groupList) {
			InputFileLoader loader = options.inputFileLoaderFactory.create();
			loader.init(spec.csvPath);
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

	public List<InputFunctionResult> dryRunLevel3(List<ImportGroupSpec> groupList, String deliaSrc,
			ExternalDataLoader externalLoader, Options options) {
		Delia delia = DBTestHelper.createNewDelia();
		this.session = delia.beginSession(deliaSrc);
		
		importSvc = createDataImportService(options); 
		List<GroupPair> groupL = new ArrayList<>();
		for(ImportGroupSpec spec: groupList) {
			InputFileLoader loader = options.inputFileLoaderFactory.create();
			loader.init(spec.csvPath);
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

	public List<InputFunctionResult> importIntoDatabase(List<ImportGroupSpec> groupList, String deliaSrc, Delia delia, Options options) {
		this.session = delia.beginSession(deliaSrc);
		
		importSvc = createDataImportService(options); 
		List<GroupPair> groupL = new ArrayList<>();
		for(ImportGroupSpec spec: groupList) {
			InputFileLoader loader = options.inputFileLoaderFactory.create();
			loader.init(spec.csvPath);
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

	public DeliaSession getSession() {
		return session;
	}

	public Options getOptions() {
		return options;
	}
}