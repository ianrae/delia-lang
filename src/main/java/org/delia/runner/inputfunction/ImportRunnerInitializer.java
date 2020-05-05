package org.delia.runner.inputfunction;

import org.delia.api.DeliaSession;
import org.delia.api.RunnerInitializer;
import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBHelper;
import org.delia.runner.DValueIterator;
import org.delia.runner.FetchRunner;
import org.delia.runner.Runner;

public class ImportRunnerInitializer implements RunnerInitializer {

	private DValueIterator iter;
	private DeliaSession session;
	private FactoryService factorySvc;
	private ExternalDataLoader externalLoader;
	
	public ImportRunnerInitializer(FactoryService factoryService, DValueIterator iter, DeliaSession session, InputFunctionServiceOptions options) {
		this.factorySvc = factoryService;
		this.iter = iter;
		this.session = session;
		this.externalLoader = options.externalLoader;
	}
	
	@Override
	public void initialize(Runner runner) {
		runner.setInsertPrebuiltValueIterator(iter);
		
		DBAccessContext dbctx = new DBAccessContext(session.getExecutionContext().registry, runner);
		DBExecutor tmpExecutor = session.getDelia().getDBInterface().createExector(dbctx);
		FetchRunner fr = tmpExecutor.createFetchRunner(factorySvc);
		FetchRunnerFacade frfacade = new FetchRunnerFacade(factorySvc, fr, externalLoader);
		runner.setPrebuiltFetchRunnerToUse(frfacade);
		
		try {
			tmpExecutor.close();
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}

}
