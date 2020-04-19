package org.delia.runner;

import java.util.List;

import org.delia.error.DeliaError;
import org.delia.type.DValue;

/**
 * The results of a database query.

 * @author Ian Rae
 *
 */
public class QueryResponse {
	public boolean ok;
	public DeliaError err;
	public List<DValue> dvalList; 
	public boolean bindFetchFlag; //if true then auto-bind fetch results into dvallist dvals
	
	public boolean emptyResults() {
		return dvalList == null || dvalList.isEmpty();
	}
	public DValue getOne() {
		if (dvalList.size() > 1) {
			throw new RuntimeException("getOne found more than one!!");
		}
		return dvalList.get(0);
	}
}