package org.delia.queryresponse;

import java.util.List;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.db.DBCapabilties;
import org.delia.type.DValue;
import org.delia.util.StringTrail;

public class QueryFuncContext {
	public int currentFnIndex = 0;
	
	public int currentPgSize = Integer.MAX_VALUE; //all
	public int currentOffset = 0;
	public boolean offsetLimitDirtyFlag = true;
//	public StringTrail pendingTrail = new StringTrail();
	public FuncScope scope = null; 

	public QueryFuncExp qfexp;
	public DBCapabilties dbCapabilities;

	
	public List<DValue> getDValList() {
		return scope.getDValList();
	}
}
