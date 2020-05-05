package org.delia.runner.inputfunction;

import org.delia.api.RunnerInitializer;
import org.delia.runner.DValueIterator;
import org.delia.runner.Runner;

public class ImportRunnerInitializer implements RunnerInitializer {

	private DValueIterator iter;
	public ImportRunnerInitializer(DValueIterator iter) {
		this.iter = iter;
	}
	
	@Override
	public void initialize(Runner runner) {
		runner.setInsertPrebuiltValueIterator(iter);
	}

}
