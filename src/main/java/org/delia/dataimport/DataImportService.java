package org.delia.dataimport;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.core.ServiceBase;
import org.delia.runner.inputfunction.ImportSpec;
import org.delia.runner.inputfunction.ImportSpecBuilder;
import org.delia.runner.inputfunction.InputFunctionRequest;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.InputFunctionService;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.ProgramSet;
import org.delia.type.DStructType;
import org.delia.util.DeliaExceptionHelper;

public class DataImportService extends ServiceBase {

	private Delia delia;
	private DeliaSession session;
	private int stopAfterErrorThreshold;

	public DataImportService(Delia delia, DeliaSession session, int stopAfterErrorThreshold) {
		super(delia.getFactoryService());
		this.delia = delia;
		this.session = session;
		this.stopAfterErrorThreshold = stopAfterErrorThreshold;
	}

	public InputFunctionResult importIntoDatabase(String inputFnName, LineObjIterator lineObjIter) {
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
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
}