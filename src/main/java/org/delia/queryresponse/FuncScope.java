package org.delia.queryresponse;

import java.util.List;

import org.delia.runner.QueryResponse;
import org.delia.type.DValue;

public class FuncScope {
	QueryResponse currentResp;
	
	public FuncScope(QueryResponse result) {
		this.currentResp = result;
	}
	
	public List<DValue> getDValList() {
		return currentResp.dvalList;
	}

	public void changeScope(QueryResponse qresResult) {
		this.currentResp = qresResult;
	}
}
