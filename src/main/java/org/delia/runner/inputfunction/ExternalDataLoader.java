package org.delia.runner.inputfunction;

import org.delia.runner.QueryResponse;
import org.delia.type.DRelation;

public interface ExternalDataLoader {
	
	public QueryResponse queryFKsExist(DRelation drel);
}
