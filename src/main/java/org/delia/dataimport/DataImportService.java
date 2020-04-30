package org.delia.dataimport;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.core.ServiceBase;
import org.delia.runner.inputfunction.InputFunctionRequest;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.InputFunctionService;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.runner.inputfunction.ProgramSet;

public class DataImportService extends ServiceBase {

	private Delia delia;
	private DeliaSession session;

	public DataImportService(Delia delia, DeliaSession session) {
		super(delia.getFactoryService());
		this.delia = delia;
		this.session = session;
	}

	public InputFunctionResult buildAndRun(String inputFnName, LineObjIterator lineObjIter) {
		InputFunctionService inputFnSvc = new InputFunctionService(delia.getFactoryService());
		ProgramSet progset = inputFnSvc.buildProgram(inputFnName, session);

		InputFunctionRequest request = new InputFunctionRequest();
		request.delia = delia;
		request.progset = progset;
		request.session = session;
		InputFunctionResult result = inputFnSvc.process(request, lineObjIter);
		return result;
	}
}