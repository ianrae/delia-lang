package org.delia.dao;

import org.delia.api.RunnerInitializer;
import org.delia.runner.DValueIterator;
import org.delia.runner.Runner;

public class DaoRunnerInitializer implements RunnerInitializer {
	protected DValueIterator iter;
	
	public DaoRunnerInitializer(DValueIterator iter) {
		this.iter = iter;
	}
	
	@Override
	public void initialize(Runner runner) {
		runner.setInsertPrebuiltValueIterator(iter);
	}
}