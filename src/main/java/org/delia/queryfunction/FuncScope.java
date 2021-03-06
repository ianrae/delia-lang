package org.delia.queryfunction;

import java.util.List;

import org.delia.runner.QueryResponse;
import org.delia.type.DValue;

public class FuncScope {
	private QueryResponse currentResp;
	private boolean hasChanged;
	
	public FuncScope(QueryResponse result) {
		this.currentResp = result;
	}
	
	public List<DValue> getDValList() {
		return currentResp.dvalList;
	}

	public void changeScope(QueryResponse qresResult) {
		this.currentResp = qresResult;
		this.hasChanged = true;
	}

	public boolean hasChanged() {
		return hasChanged;
	}
}
