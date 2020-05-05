package org.delia.runner.inputfunction;

import org.delia.core.FactoryService;
import org.delia.log.Log;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class FetchRunnerFacade implements FetchRunner {

	private FetchRunner inner;
	private Log log;

	public FetchRunnerFacade(FactoryService factorySvc, FetchRunner fr) {
		this.inner = fr;
		this.log = factorySvc.getLog();
	}

	@Override
	public QueryResponse load(DRelation drel) {
		log.logDebug("FRFFFFFFFFFFFFFFF1");
		return inner.load(drel);
	}

	@Override
	public boolean queryFKExists(DStructType owningType, String subject, DRelation drel) {
		log.logDebug("FRFFFFFFFFFFFFFFF2");
		return inner.queryFKExists(owningType, subject, drel);
	}

	@Override
	public QueryResponse loadFKOnly(String typeName, String fieldName, DValue keyVal) {
		log.logDebug("FRFFFFFFFFFFFFFFF3");
		return inner.loadFKOnly(typeName, fieldName, keyVal);
	}

}
