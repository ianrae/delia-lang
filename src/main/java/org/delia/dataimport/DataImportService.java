package org.delia.dataimport;

import java.util.StringJoiner;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.error.DetailedError;
import org.delia.runner.inputfunction.ImportMetricObserver;
import org.delia.runner.inputfunction.ImportSpec;
import org.delia.runner.inputfunction.ImportSpecBuilder;
import org.delia.runner.inputfunction.InputFunctionRequest;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.InputFunctionService;
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

	public DataImportService(Delia delia, DeliaSession session, int stopAfterErrorThreshold) {
		super(delia.getFactoryService());
		this.delia = delia;
		this.session = session;
		this.stopAfterErrorThreshold = stopAfterErrorThreshold;
	}

	public InputFunctionResult importIntoDatabase(String inputFnName, LineObjIterator lineObjIter) {
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		inputFnSvc.setMetricsObserver(metricsObserver);
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
		InputFunctionResult result = inputFnSvc.process(request, lineObjIter);
		return result;
	}

	public ImportMetricObserver getMetricsObserver() {
		return metricsObserver;
	}

	public void setMetricsObserver(ImportMetricObserver metricsObserver) {
		this.metricsObserver = metricsObserver;
	}
	
	public void dumpImportReport(InputFunctionResult result, SimpleImportMetricObserver observer) {
		int n = result.numRowsProcessed;
		int failed = result.numRowsInserted;
		int succeeded = n - failed;
		String s = String.format("IMPORT %d rows. %d successful, %d failed", n, failed, succeeded);
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
				String ss = String.format("%15s (%2d): %s", ofh.fieldName, ofh.fieldIndex, joiner.toString());
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
				}
				String msg = err.toString();
				log.log("  line %d: %s - %s", err.getLineNum(), fieldName, msg);
			}
		}
		
		
	}
	
}