package org.delia.dataimport;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.db.DBType;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.GroupPair;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.SimpleImportMetricObserver;

public class CSVImportService  {

	private DeliaSession session;
	private SimpleImportMetricObserver observer;
	private DataImportService importSvc;

	public CSVImportService() {
	}
	
	public InputFunctionResult dryRunLevel1(String csvPath, String deliaSrc, String typeName, String inputFunctionName) {
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
	
	public List<InputFunctionResult> dryRunLevel2(List<ImportGroupSpec> groupList, String deliaSrc) {
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

	public List<InputFunctionResult> dryRunLevel3(List<ImportGroupSpec> groupList, String deliaSrc,
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

	public List<InputFunctionResult> importIntoDatabase(List<ImportGroupSpec> groupList, String deliaSrc, Delia delia) {
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