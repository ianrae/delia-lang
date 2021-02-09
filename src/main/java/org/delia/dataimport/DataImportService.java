package org.delia.dataimport;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.error.DetailedError;
import org.delia.runner.inputfunction.ExternalDataLoader;
import org.delia.runner.inputfunction.GroupPair;
import org.delia.runner.inputfunction.ImportMetricObserver;
import org.delia.runner.inputfunction.ImportSpec;
import org.delia.runner.inputfunction.ImportSpecBuilder;
import org.delia.runner.inputfunction.ImportedValueListener;
import org.delia.runner.inputfunction.InputFunctionRequest;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.InputFunctionService;
import org.delia.runner.inputfunction.InputFunctionServiceOptions;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.OutputFieldHandle;
import org.delia.runner.inputfunction.ProgramSet;
import org.delia.runner.inputfunction.SimpleImportMetricObserver;
import org.delia.runner.inputfunction.ProgramSet.OutputSpec;
import org.delia.util.DeliaExceptionHelper;

public class DataImportService extends ServiceBase {

	private Delia delia;
	private DeliaSession session;
	private int stopAfterErrorThreshold;
	private ImportMetricObserver metricsObserver;
	private ExternalDataLoader externalLoader;
	private int numRowsToImport;
	private boolean logDetails;
	private boolean useInsertStatement;
	private ImportedValueListener importedValueListener;

	public DataImportService(DeliaSession session, int stopAfterErrorThreshold) {
		this(session, Integer.MAX_VALUE, stopAfterErrorThreshold, false);
	}
	public DataImportService(DeliaSession session, int numRowsToImport, int stopAfterErrorThreshold, boolean logDetails) {
		super(session.getDelia().getFactoryService());
		this.delia = session.getDelia();
		this.session = session;
		this.numRowsToImport = numRowsToImport;
		this.stopAfterErrorThreshold = stopAfterErrorThreshold;
		this.logDetails = logDetails;
	}
	
	public List<InputFunctionResult> executeImportGroup(List<GroupPair> groupL, ImportLevel importLevel) {
		List<InputFunctionResult> resultL = new ArrayList<>();
		
		for(GroupPair pair: groupL) {
			InputFunctionResult result = executeImport(pair.inputFnName, pair.iter, importLevel);
			resultL.add(result);
		}
		
		return resultL;
	}

	public InputFunctionResult executeImport(String inputFnName, LineObjIterator lineObjIter, ImportLevel importLevel) {
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		inputFnSvc.setMetricsObserver(metricsObserver);
		inputFnSvc.getOptions().numRowsToImport = this.numRowsToImport;
		inputFnSvc.getOptions().logDetails = logDetails;
		inputFnSvc.getOptions().useInsertStatement = useInsertStatement;
		initImportLevel(inputFnSvc, importLevel);
		log.log("---- import: %s ----", lineObjIter.getFileName());
		ProgramSet progset = inputFnSvc.buildProgram(inputFnName, session);
		if (progset == null) {
			DeliaExceptionHelper.throwError("cant-find-user-fn", "Can't find input fn '%s'", inputFnName);
		}
		
		ImportSpecBuilder ispecBuilder = new ImportSpecBuilder();
		for(ProgramSet.OutputSpec outspec: progset.outputSpecs) {
			outspec.ispec = ispecBuilder.buildSpecFor(progset, outspec.structType);
		}
		
		InputFunctionRequest request = new InputFunctionRequest();
		request.delia = delia;
		request.progset = progset;
		request.session = session;
		request.stopAfterErrorThreshold = stopAfterErrorThreshold;
		request.importedValueListener = importedValueListener;
		InputFunctionResult result = inputFnSvc.process(request, lineObjIter);
		result.filename = lineObjIter.getFileName();
		lineObjIter.close();
		return result;
	}

	private void initImportLevel(InputFunctionService inputFnSvc, ImportLevel importLevel) {
		InputFunctionServiceOptions options = inputFnSvc.getOptions();
		
		switch(importLevel) 
		{
		case ONE:
			options.ignoreRelationErrors = true;
			break;
		case THREE:
			options.externalLoader = this.externalLoader;
			break;
		default:
			break;
		}
	}

	public ImportMetricObserver getMetricsObserver() {
		return metricsObserver;
	}

	public void setMetricsObserver(ImportMetricObserver metricsObserver) {
		this.metricsObserver = metricsObserver;
	}
	
	public void dumpImportReport(InputFunctionResult result, SimpleImportMetricObserver observer) {
		int n = result.numRowsProcessed;
		int succeeded = result.numRowsInserted - result.numFailedRowInserts;
		int failed = n - succeeded;
		String alert = failed == 0 ? "  ***SUCCESS***" : String.format("(%d errors)", result.errors.size());
		log.log("");
		String s = String.format("IMPORT %d rows. %d successful, %d failed        %s  %s", n, succeeded, failed, alert, result.filename);
		log.log(s);
		
		for(OutputSpec ospec : result.progset.outputSpecs) {
			ImportSpec ispec = ospec.ispec;
			log.log("%15s         N   M  I1  I2   D   R", ospec.structType.getName());
			StringJoiner joiner = new StringJoiner(",");
			for(OutputFieldHandle ofh: ispec.ofhList) {
				joiner = new StringJoiner(",");
				for(int x: ofh.arMetrics) {
					joiner.add(String.format("%3d", x));
				}
				String ss = String.format("%15s [%2d]: %s", ofh.fieldName, ofh.fieldIndex, joiner.toString());
				log.log(ss);
			}
			
			//totals
			int[] totals = new int[OutputFieldHandle.NUM_METRICS];
			for(OutputFieldHandle ofh: ispec.ofhList) {
				for(int k = 0; k < OutputFieldHandle.NUM_METRICS; k++) {
					int count = ofh.arMetrics[k];
					totals[k] += count;
				}
			}
			joiner = new StringJoiner(",");
			for(int i = 0; i < totals.length; i++) {
				joiner.add(String.format("%3d", totals[i]));
			}
				
			String ss = String.format("%15s     : %s", "TOTALS", joiner.toString());
			log.log(ss);
		}
		
		if (!result.errors.isEmpty()) {
			int nn = result.errors.size() > 20 ? 20 : result.errors.size();
			log.log("");
			log.log("first %d errors: ", nn);
			for(int i = 0; i < nn; i++) {
				DeliaError err = result.errors.get(i);
				String fieldName = "";
				if (err instanceof DetailedError) {
					DetailedError derr = (DetailedError) err;
					fieldName = derr.getFieldName();
				} else {
					fieldName = err.getArg1();
				}
				String msg = err.toString();
				log.log("  line %d: %s - %s", err.getLineNum(), fieldName, msg);
			}
		}
		
		if (! observer.externalLoadMap.isEmpty()) {
			StringJoiner joiner = new StringJoiner(",");
			for(String typeName: observer.externalLoadMap.keySet()) {
				Integer count = observer.externalLoadMap.get(typeName);
				joiner.add(String.format("%s: %d", typeName, count));
			}
			log.log("Externally loaded records:- %s", joiner.toString());
		}
		
	}

	public void setExternalDataLoader(ExternalDataLoader externalLoader) {
		this.externalLoader = externalLoader;
	}
	public void setUseInsertStatement(boolean useInsertStatement) {
		this.useInsertStatement = useInsertStatement;
	}
	public void setImportedValueListener(ImportedValueListener importedValueListener) {
		this.importedValueListener = importedValueListener;
	}
	
}