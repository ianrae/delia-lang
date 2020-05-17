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
import org.delia.zdb.ZDBExecutor;

public class ImportRunnerInitializer implements RunnerInitializer {

	private DValueIterator iter;
	private DeliaSession session;
	private FactoryService factorySvc;
	private ExternalDataLoader externalLoader;
	private ImportSpec ispec;
	private ImportMetricObserver metricsObserver;
	
	public ImportRunnerInitializer(FactoryService factoryService, DValueIterator iter, DeliaSession session, InputFunctionServiceOptions options, ImportSpec ispec, ImportMetricObserver metricsObserver) {
		this.factorySvc = factoryService;
		this.iter = iter;
		this.session = session;
		this.externalLoader = options.externalLoader;
		this.ispec = ispec;
		this.metricsObserver = metricsObserver;
	}
	
	@Override
	public void initialize(Runner runner) {
		runner.setInsertPrebuiltValueIterator(iter);
		
		DBAccessContext dbctx = new DBAccessContext(session.getExecutionContext().registry, runner);
		ZDBExecutor tmpExecutor = session.getDelia().getDBInterface().createExecutor();
		tmpExecutor.init1(session.getExecutionContext().registry);
		tmpExecutor.init2(session.getDatIdMap(), runner);
		FetchRunner fr = tmpExecutor.createFetchRunner();
		FetchRunnerFacade frfacade = new FetchRunnerFacade(factorySvc, fr, externalLoader, ispec, metricsObserver);
		runner.setPrebuiltFetchRunnerToUse(frfacade);
		
		try {
			tmpExecutor.close();
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}

}
