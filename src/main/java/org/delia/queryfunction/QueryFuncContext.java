package org.delia.queryfunction;

import java.util.List;

import org.delia.type.DValue;

public class QueryFuncContext {
	public int currentPgSize = Integer.MAX_VALUE; //all
	public int currentOffset = 0;
	public boolean offsetLimitDirtyFlag = true;
	public FuncScope scope = null; 

	public List<DValue> getDValList() {
		return scope.getDValList();
	}
}
