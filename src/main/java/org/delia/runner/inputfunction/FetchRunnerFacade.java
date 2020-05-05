package org.delia.runner.inputfunction;

import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class FetchRunnerFacade implements FetchRunner {

	private FetchRunner inner;

	public FetchRunnerFacade(FetchRunner fr) {
		this.inner = fr;
	}

	@Override
	public QueryResponse load(DRelation drel, String targetFieldName) {
		
		return inner.load(drel, targetFieldName);
	}

	@Override
	public boolean queryFKExists(DStructType owningType, String subject, DRelation drel) {
		return inner.queryFKExists(owningType, subject, drel);
	}

	@Override
	public QueryResponse loadFKOnly(String typeName, String fieldName, DValue keyVal) {
		return inner.loadFKOnly(typeName, fieldName, keyVal);
	}

}
